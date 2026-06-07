import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startKoinIfNeeded()
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
        }
    }
}
