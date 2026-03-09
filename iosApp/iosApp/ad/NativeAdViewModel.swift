import Foundation
import SwiftUI
import ComposeApp
import GoogleMobileAds

let NativeAdId = "ca-app-pub-3940256099942544/3986624511"

final class NativeAdViewModel: NSObject, ObservableObject, NativeAdLoaderDelegate, NativeAdDelegate {
    static let shared = NativeAdViewModel()

    @Published var nativeAd: NativeAd?
    private var adLoader: AdLoader!
    static var updateAdState: ((_ state: AdState) -> Void)?

    func loadAd() {
        NativeAdViewModel.updateAdState?(AdState.loading)
        adLoader = AdLoader(
            adUnitID: NativeAdId,
            rootViewController: nil,
            adTypes: [.native], options: nil
        )
        adLoader.delegate = self
        adLoader.load(Request())
    }

    func adLoader(_ adLoader: AdLoader, didReceive nativeAd: NativeAd) {
        self.nativeAd = nativeAd
        nativeAd.delegate = self
        NativeAdViewModel.updateAdState?(AdState.success)
    }

    func adLoader(_ adLoader: AdLoader, didFailToReceiveAdWithError error: Error) {
        print("\(adLoader) failed with error: \(error.localizedDescription)")
        NativeAdViewModel.updateAdState?(AdState.failed)
    }
}
