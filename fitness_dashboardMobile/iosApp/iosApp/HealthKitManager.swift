import Foundation
import HealthKit
import SwiftUI
import ComposeApp

@MainActor
final class HealthKitManager: ObservableObject {
    private let healthStore = HKHealthStore()
    private let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount)
    private let bodyMassType = HKQuantityType.quantityType(forIdentifier: .bodyMass)
    private let sleepType = HKObjectType.categoryType(forIdentifier: .sleepAnalysis)
    private let workoutType = HKObjectType.workoutType()
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

        guard let stepType, let sleepType, let bodyMassType else {
            HealthBridgeKt.updateHealthError(message: "HealthKit 数据类型初始化失败。")
            return
        }

        HealthBridgeKt.updateHealthLoading(message: "正在请求 Apple 健康读取权限...")

        let readTypes: Set<HKObjectType> = [stepType, sleepType, bodyMassType, workoutType]
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
        guard let stepType, let sleepType, let bodyMassType else {
            HealthBridgeKt.updateHealthError(message: "HealthKit 数据类型初始化失败。")
            return
        }

        HealthBridgeKt.updateHealthLoading(message: "正在读取今日步数、睡眠、训练和体重数据...")

        let group = DispatchGroup()

        var stepCount = 0
        var hasStepData = false
        var sleepHours = 0.0
        var hasSleepData = false
        var todayWeightKilograms = 0.0
        var hasTodayWeight = false
        var weightHistoryRaw = ""
        var workoutSummary = TodayWorkoutSummary.empty
        var hasWorkoutData = false
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

        group.enter()
        fetchTodayLatestBodyMass(for: bodyMassType) { result in
            switch result {
            case .success(let value):
                todayWeightKilograms = value
                hasTodayWeight = true
            case .failure(let error):
                if self.isAuthorizationDeniedError(error) {
                    hasAuthorizationDenied = true
                } else {
                    failureMessage = error.localizedDescription
                }
            case .noData:
                hasTodayWeight = false
            }
            group.leave()
        }

        group.enter()
        fetchRecentBodyMassHistory(for: bodyMassType) { result in
            switch result {
            case .success(let encodedHistory):
                weightHistoryRaw = encodedHistory
            case .failure(let error):
                if self.isAuthorizationDeniedError(error) {
                    hasAuthorizationDenied = true
                }
                weightHistoryRaw = ""
            case .noData:
                weightHistoryRaw = ""
            }
            group.leave()
        }

        group.enter()
        fetchTodayWorkoutSummary { result in
            switch result {
            case .success(let summary):
                workoutSummary = summary
                hasWorkoutData = true
            case .failure(let error):
                if self.isAuthorizationDeniedError(error) {
                    hasAuthorizationDenied = true
                } else {
                    failureMessage = error.localizedDescription
                }
            case .noData:
                hasWorkoutData = false
            }
            group.leave()
        }

        group.notify(queue: .main) {
            if hasAuthorizationDenied && !hasStepData && !hasSleepData && !hasTodayWeight && !hasWorkoutData {
                HealthBridgeKt.updateHealthDenied(message: "未获得 Apple 健康读取权限，请前往系统设置开启步数、睡眠、训练和体重读取权限。")
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
            if !hasStepData && !hasSleepData && !hasTodayWeight && !hasWorkoutData {
                message = "Apple 健康暂无今日步数、睡眠、训练或体重数据，请检查系统授权与健康 App 中的数据记录。"
            } else {
                message = "Apple 健康数据已更新"
            }

            HealthBridgeKt.updateHealthAuthorized(
                todaySteps: Int32(stepCount),
                hasTodaySteps: hasStepData,
                sleepDurationHours: sleepHours,
                hasSleepDuration: hasSleepData,
                todayWeightKilograms: todayWeightKilograms,
                hasTodayWeight: hasTodayWeight,
                weightHistoryRaw: weightHistoryRaw,
                workoutType: workoutSummary.latestWorkout.typeLabel,
                workoutDurationMinutes: workoutSummary.latestWorkout.durationMinutes,
                hasWorkout: hasWorkoutData,
                workoutStartDateIso: workoutSummary.latestWorkout.startDateIso,
                workoutEndDateIso: workoutSummary.latestWorkout.endDateIso,
                workoutCaloriesKilocalories: workoutSummary.latestWorkout.caloriesKilocalories,
                hasWorkoutCalories: workoutSummary.latestWorkout.hasCalories,
                workoutDistanceKilometers: workoutSummary.latestWorkout.distanceKilometers,
                hasWorkoutDistance: workoutSummary.latestWorkout.hasDistance,
                scorePrimaryWorkoutType: workoutSummary.primaryWorkout.typeLabel,
                scorePrimaryWorkoutDurationMinutes: workoutSummary.primaryWorkout.durationMinutes,
                scoreAdditionalWorkoutDurationMinutes: workoutSummary.additionalWorkoutDurationMinutes,
                scoreAdditionalWorkoutsRaw: workoutSummary.additionalWorkoutsRaw,
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

    private func fetchTodayLatestBodyMass(
        for type: HKQuantityType,
        completion: @escaping (HealthFetchResult<Double>) -> Void
    ) {
        let now = Date()
        let startOfDay = Calendar.current.startOfDay(for: now)
        let predicate = HKQuery.predicateForSamples(withStart: startOfDay, end: now, options: .strictStartDate)
        let sortDescriptors = [NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)]

        let query = HKSampleQuery(
            sampleType: type,
            predicate: predicate,
            limit: 1,
            sortDescriptors: sortDescriptors
        ) { _, samples, error in
            if let error {
                completion(.failure(error))
                return
            }

            guard let sample = (samples as? [HKQuantitySample])?.first else {
                completion(.noData)
                return
            }

            let value = sample.quantity.doubleValue(for: HKUnit.gramUnit(with: .kilo))
            guard value > 0 else {
                completion(.noData)
                return
            }

            completion(.success(value))
        }

        healthStore.execute(query)
    }

    private func fetchRecentBodyMassHistory(
        for type: HKQuantityType,
        completion: @escaping (HealthFetchResult<String>) -> Void
    ) {
        let now = Date()
        let calendar = Calendar.current
        let endDate = now
        let startOfToday = calendar.startOfDay(for: now)

        guard let startDate = calendar.date(byAdding: .day, value: -29, to: startOfToday) else {
            completion(.noData)
            return
        }

        let predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: .strictStartDate)
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

            guard let quantitySamples = samples as? [HKQuantitySample], !quantitySamples.isEmpty else {
                completion(.noData)
                return
            }

            let formatter = DateFormatter()
            formatter.calendar = calendar
            formatter.locale = Locale(identifier: "en_US_POSIX")
            formatter.timeZone = calendar.timeZone
            formatter.dateFormat = "yyyy-MM-dd"

            var latestWeightByDay: [String: Double] = [:]
            for sample in quantitySamples {
                let kilograms = sample.quantity.doubleValue(for: HKUnit.gramUnit(with: .kilo))
                guard kilograms > 0 else { continue }

                let dateKey = formatter.string(from: sample.endDate)
                if latestWeightByDay[dateKey] == nil {
                    latestWeightByDay[dateKey] = kilograms
                }
            }

            let encoded = latestWeightByDay.keys
                .sorted()
                .compactMap { dayKey in
                    latestWeightByDay[dayKey].map { "\(dayKey)\t\($0)" }
                }
                .joined(separator: "\n")

            if encoded.isEmpty {
                completion(.noData)
            } else {
                completion(.success(encoded))
            }
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

    private func fetchTodayWorkoutSummary(
        completion: @escaping (HealthFetchResult<TodayWorkoutSummary>) -> Void
    ) {
        let now = Date()
        let startOfDay = Calendar.current.startOfDay(for: now)
        let predicate = HKQuery.predicateForSamples(withStart: startOfDay, end: now, options: .strictStartDate)
        let sortDescriptors = [NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)]

        let query = HKSampleQuery(
            sampleType: workoutType,
            predicate: predicate,
            limit: HKObjectQueryNoLimit,
            sortDescriptors: sortDescriptors
        ) { _, samples, error in
            if let error {
                completion(.failure(error))
                return
            }

            guard let workouts = samples as? [HKWorkout], !workouts.isEmpty else {
                completion(.noData)
                return
            }

            let latestWorkout = workouts[0]
            let primaryWorkout = workouts.max { lhs, rhs in
                if lhs.duration == rhs.duration {
                    return lhs.endDate < rhs.endDate
                }

                return lhs.duration < rhs.duration
            } ?? latestWorkout
            let totalWorkoutDurationMinutes = workouts.reduce(0.0) { partialResult, workout in
                partialResult + workout.duration / 60.0
            }
            let additionalWorkoutDurationMinutes = max(
                totalWorkoutDurationMinutes - (primaryWorkout.duration / 60.0),
                0.0
            )
            let additionalWorkoutSnapshots = workouts
                .filter { $0.uuid != primaryWorkout.uuid }
                .map { Self.workoutSnapshot(from: $0) }

            completion(
                .success(
                    TodayWorkoutSummary(
                        latestWorkout: Self.workoutSnapshot(from: latestWorkout),
                        primaryWorkout: Self.workoutSnapshot(from: primaryWorkout),
                        additionalWorkoutDurationMinutes: additionalWorkoutDurationMinutes,
                        additionalWorkoutsRaw: Self.encodeWorkoutSnapshots(additionalWorkoutSnapshots)
                    )
                )
            )
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

    nonisolated private static func workoutLabel(for type: HKWorkoutActivityType) -> String {
        switch type {
        case .traditionalStrengthTraining:
            return "传统力量训练"
        case .functionalStrengthTraining:
            return "功能性力量训练"
        case .running:
            return "跑步"
        case .walking:
            return "步行"
        case .cycling:
            return "骑行"
        case .swimming:
            return "游泳"
        case .highIntensityIntervalTraining:
            return "HIIT"
        case .mixedCardio:
            return "混合有氧"
        case .rowing:
            return "划船"
        case .yoga:
            return "瑜伽"
        case .hiking:
            return "徒步"
        case .elliptical:
            return "椭圆机"
        default:
            return "其他训练"
        }
    }

    nonisolated private static func workoutSnapshot(from workout: HKWorkout) -> TodayWorkoutSnapshot {
        let calories = workout.totalEnergyBurned?.doubleValue(for: HKUnit.kilocalorie())
        let distanceInKilometers = workout.totalDistance?.doubleValue(for: HKUnit.meter()) ?? 0.0

        return TodayWorkoutSnapshot(
            typeLabel: workoutLabel(for: workout.workoutActivityType),
            durationMinutes: workout.duration / 60.0,
            startDateIso: iso8601String(from: workout.startDate),
            endDateIso: iso8601String(from: workout.endDate),
            caloriesKilocalories: calories ?? 0.0,
            hasCalories: calories != nil,
            distanceKilometers: distanceInKilometers / 1000.0,
            hasDistance: workout.totalDistance != nil
        )
    }

    nonisolated private static func encodeWorkoutSnapshots(_ workouts: [TodayWorkoutSnapshot]) -> String {
        return workouts.map { workout in
            "\(workout.typeLabel)\t\(workout.durationMinutes)"
        }.joined(separator: "\n")
    }

    nonisolated private static func iso8601String(from date: Date) -> String {
        let formatter = ISO8601DateFormatter()
        return formatter.string(from: date)
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

private struct TodayWorkoutSnapshot {
    let typeLabel: String
    let durationMinutes: Double
    let startDateIso: String
    let endDateIso: String
    let caloriesKilocalories: Double
    let hasCalories: Bool
    let distanceKilometers: Double
    let hasDistance: Bool

    static let empty = TodayWorkoutSnapshot(
        typeLabel: "",
        durationMinutes: 0.0,
        startDateIso: "",
        endDateIso: "",
        caloriesKilocalories: 0.0,
        hasCalories: false,
        distanceKilometers: 0.0,
        hasDistance: false
    )
}

private struct TodayWorkoutSummary {
    let latestWorkout: TodayWorkoutSnapshot
    let primaryWorkout: TodayWorkoutSnapshot
    let additionalWorkoutDurationMinutes: Double
    let additionalWorkoutsRaw: String

    static let empty = TodayWorkoutSummary(
        latestWorkout: .empty,
        primaryWorkout: .empty,
        additionalWorkoutDurationMinutes: 0.0,
        additionalWorkoutsRaw: ""
    )
}
