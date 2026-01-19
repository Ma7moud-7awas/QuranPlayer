import UIKit
import SwiftUI
import ComposeApp
import GoogleMobileAds

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {

    init() {
        MobileAds.shared.start()

        MainViewControllerKt.BannerContainer = { (adWidth: KotlinInt) -> UIViewController in
            return UIHostingController(rootView: AdBannerView(adWidth.intValue))
        }
    }

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}



