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

        MainViewControllerKt.AdBannerContainer = { (adWidth: KotlinInt) -> UIViewController in
            return UIHostingController(rootView: AdBannerView(adWidth.intValue))
        }

        MainViewControllerKt.loadNativeAd = { (updateSate) -> Void in
            NativeAdViewModel.updateAdState = { state in
                updateSate(state)
            }

            NativeAdViewModel.shared.loadAd()
        }

        MainViewControllerKt.AdNativeContainer = { () -> UIViewController in
            return (UIHostingController(rootView: AdNativeView(
                nativeViewModel: NativeAdViewModel.shared
            )))
        }
    }

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}



