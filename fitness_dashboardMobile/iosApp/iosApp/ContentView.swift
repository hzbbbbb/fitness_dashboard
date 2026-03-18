import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @Environment(\.scenePhase) private var scenePhase
    @StateObject private var healthKitManager = HealthKitManager()

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .task {
                healthKitManager.bootstrapIfNeeded()
            }
            .onChange(of: scenePhase) { _, newPhase in
                if newPhase == .active {
                    healthKitManager.refreshIfPossible()
                }
            }
    }
}
