import Foundation
import HealthKit
import SwiftUI
import ComposeApp

@MainActor
final class HealthKitManager: ObservableObject {
    private let healthStore = HKHealthStore()
    private let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount)
    private let sleepType = HKObjectType.categoryType(forIdentifier: .sleepAnalysis)
    private var hasBootstrapped = false

    func bootstrapIfNeeded() {
        guard !hasBootstrapped else { return }
        hasBootstrapped = true
        requestAuthorizationAndRefresh()
    }

    func refreshIfPossible() {
        guard hasBootstrapped else {
            bootstrapIfNeeded()
            return
        }
        guard HKHealthStore.isHealthDataAvailable() else {
            HealthBridgeKt.updateHealthUnavailable(message: "当前设备不支持 Apple 健康，健康摘要不可用。")
            return
        }
        refreshHealthData()
    }

    private func requestAuthorizationAndRefresh() {
        guard HKHealthStore.isHealthDataAvailable() else {
            HealthBridgeKt.updateHealthUnavailable(message: "当前设备不支持 Apple 健康，健康摘要不可用。")
            return
        }

        guard let stepType, let sleepType else {
            HealthBridgeKt.updateHealthError(message: "HealthKit 数据类型初始化失败。")
            return
        }

        HealthBridgeKt.updateHealthLoading(message: "正在请求 Apple 健康读取权限...")

        let readTypes: Set<HKObjectType> = [stepType, sleepType]
        healthStore.requestAuthorization(toShare: nil, read: readTypes) { [weak self] success, error in
            DispatchQueue.main.async {
                guard let self else { return }

                if let error {
                    HealthBridgeKt.updateHealthError(message: "Apple 健康授权失败：\(error.localizedDescription)")
                    return
                }

                if !success {
                    HealthBridgeKt.updateHealthDenied(message: "未能完成 Apple 健康授权，请稍后重试或前往系统设置检查权限。")
                    return
                }

                self.refreshHealthData()
            }
        }
    }

    private func refreshHealthData() {
        guard let stepType, let sleepType else {
            HealthBridgeKt.updateHealthError(message: "HealthKit 数据类型初始化失败。")
            return
        }

        HealthBridgeKt.updateHealthLoading(message: "正在读取今日步数和睡眠时长...")

        let group = DispatchGroup()

        var stepCount = 0
        var hasStepData = false
        var sleepHours = 0.0
        var hasSleepData = false
        var failureMessage: String?
        var hasAuthorizationDenied = false

        group.enter()
        fetchTodayStepCount(for: stepType) { result in
            switch result {
            case .success(let value):
                stepCount = value
                hasStepData = true
            case .failure(let error):
                if self.isAuthorizationDeniedError(error) {
                    hasAuthorizationDenied = true
                } else {
                    failureMessage = error.localizedDescription
                }
            case .noData:
                hasStepData = false
            }
            group.leave()
        }

        group.enter()
        fetchOvernightSleepDuration(for: sleepType) { result in
            switch result {
            case .success(let value):
                sleepHours = value
                hasSleepData = true
            case .failure(let error):
                if self.isAuthorizationDeniedError(error) {
                    hasAuthorizationDenied = true
                } else {
                    failureMessage = error.localizedDescription
                }
            case .noData:
                hasSleepData = false
            }
            group.leave()
        }

        group.notify(queue: .main) {
            if hasAuthorizationDenied && !hasStepData && !hasSleepData {
                HealthBridgeKt.updateHealthDenied(message: "未获得 Apple 健康读取权限，请前往系统设置开启步数和睡眠读取权限。")
                return
            }

            if let failureMessage {
                HealthBridgeKt.updateHealthError(message: "读取 Apple 健康数据失败：\(failureMessage)")
                return
            }

            let formatter = DateFormatter()
            formatter.locale = Locale(identifier: "zh_CN")
            formatter.dateFormat = "HH:mm"

            let message: String
            if !hasStepData && !hasSleepData {
                message = "Apple 健康暂无今日步数或睡眠数据，请检查系统授权与健康 App 中的数据记录。"
            } else {
                message = "Apple 健康数据已更新"
            }

            HealthBridgeKt.updateHealthAuthorized(
                todaySteps: Int32(stepCount),
                hasTodaySteps: hasStepData,
                sleepDurationHours: sleepHours,
                hasSleepDuration: hasSleepData,
                lastUpdatedAt: formatter.string(from: Date()),
                statusMessage: message
            )
        }
    }

    private func fetchTodayStepCount(
        for type: HKQuantityType,
        completion: @escaping (HealthFetchResult<Int>) -> Void
    ) {
        let now = Date()
        let startOfDay = Calendar.current.startOfDay(for: now)
        let predicate = HKQuery.predicateForSamples(withStart: startOfDay, end: now, options: .strictStartDate)

        let query = HKStatisticsQuery(
            quantityType: type,
            quantitySamplePredicate: predicate,
            options: .cumulativeSum
        ) { _, result, error in
            if let error {
                completion(.failure(error))
                return
            }

            guard let sum = result?.sumQuantity() else {
                completion(.noData)
                return
            }

            completion(.success(Int(sum.doubleValue(for: HKUnit.count()))))
        }

        healthStore.execute(query)
    }

    private func fetchOvernightSleepDuration(
        for type: HKCategoryType,
        completion: @escaping (HealthFetchResult<Double>) -> Void
    ) {
        let now = Date()
        let calendar = Calendar.current
        let startOfToday = calendar.startOfDay(for: now)

        guard
            let start = calendar.date(byAdding: .hour, value: -6, to: startOfToday),
            let end = calendar.date(byAdding: .hour, value: 12, to: startOfToday)
        else {
            completion(.noData)
            return
        }

        let predicate = HKQuery.predicateForSamples(withStart: start, end: now, options: [])
        let sortDescriptors = [NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)]

        let query = HKSampleQuery(
            sampleType: type,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: sortDescriptors
        ) { _, samples, error in
            if let error {
                completion(.failure(error))
                return
            }

            guard let sleepSamples = (samples as? [HKCategorySample])?.filter({ self.isAsleepSample($0) }), !sleepSamples.isEmpty else {
                completion(.noData)
                return
            }

            let mergedIntervals = self.mergeSleepIntervals(
                from: sleepSamples,
                boundedBy: start ... min(end, now)
            )

            guard !mergedIntervals.isEmpty else {
                completion(.noData)
                return
            }

            let totalDuration = mergedIntervals.reduce(0.0) { partialResult, interval in
                partialResult + interval.upperBound.timeIntervalSince(interval.lowerBound)
            }

            completion(.success(totalDuration / 3600.0))
        }

        healthStore.execute(query)
    }

    private func mergeSleepIntervals(
        from samples: [HKCategorySample],
        boundedBy range: ClosedRange<Date>
    ) -> [ClosedRange<Date>] {
        let intervals = samples
            .map { sample in
                max(sample.startDate, range.lowerBound) ... min(sample.endDate, range.upperBound)
            }
            .filter { $0.lowerBound < $0.upperBound }
            .sorted { $0.lowerBound < $1.lowerBound }

        guard var current = intervals.first else {
            return []
        }

        var merged: [ClosedRange<Date>] = []

        for interval in intervals.dropFirst() {
            if interval.lowerBound <= current.upperBound {
                current = current.lowerBound ... max(current.upperBound, interval.upperBound)
            } else {
                merged.append(current)
                current = interval
            }
        }

        merged.append(current)
        return merged
    }

    private func isAuthorizationDeniedError(_ error: Error) -> Bool {
        let nsError = error as NSError
        return nsError.domain == HKErrorDomain
            && nsError.code == HKError.errorAuthorizationDenied.rawValue
    }

    private func isAsleepSample(_ sample: HKCategorySample) -> Bool {
        if #available(iOS 16.0, *) {
            return sample.value == HKCategoryValueSleepAnalysis.asleepUnspecified.rawValue
                || sample.value == HKCategoryValueSleepAnalysis.asleepCore.rawValue
                || sample.value == HKCategoryValueSleepAnalysis.asleepDeep.rawValue
                || sample.value == HKCategoryValueSleepAnalysis.asleepREM.rawValue
        } else {
            return sample.value == HKCategoryValueSleepAnalysis.asleep.rawValue
        }
    }
}

private enum HealthFetchResult<Value> {
    case success(Value)
    case noData
    case failure(Error)
}
