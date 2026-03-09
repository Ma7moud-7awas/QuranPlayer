import Foundation
import SwiftUI
import GoogleMobileAds

let BannerAdId = "ca-app-pub-3940256099942544/2435281174"

struct AdBannerView: UIViewRepresentable {
    typealias UIViewType = BannerView
    let adWidth: Int

    init() {
        self.adWidth = 350
    }
    
    init(_ adWidth: Int) {
        self.adWidth = adWidth
    }
    
    func makeUIView(context: Context) -> BannerView {
        let banner = BannerView(adSize: currentOrientationAnchoredAdaptiveBanner(width: CGFloat(adWidth)))

        banner.adUnitID = BannerAdId
        banner.load(Request())
        banner.delegate = context.coordinator
        return banner
    }
    
    func updateUIView(_ uiView: BannerView, context: Context) {}
    
    func makeCoordinator() -> BannerCoordinator {
       return BannerCoordinator(self)
     }
    
    class BannerCoordinator: NSObject, BannerViewDelegate {

        let parent: AdBannerView

        init(_ parent: AdBannerView) {
          self.parent = parent
        }

        func bannerViewDidReceiveAd(_ bannerView: BannerView) {
          print("DID RECEIVE AD.")
        }

        func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
          print("FAILED TO RECEIVE AD: \(error.localizedDescription)")
        }
      }
}
