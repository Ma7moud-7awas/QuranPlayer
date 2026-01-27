package com.m7.quranplayer.ads.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.m7.quranplayer.ads.AdState

actual fun loadAd(onAdStateChanged: (AdState) -> Unit) {
    AdHolder.instance.loadNativeAd(onAdStateChanged)
}

@Composable
actual fun AdNative(modifier: Modifier) {
    AdHolder.instance.nativeAd?.let { ad ->
        NativeAdView(ad = ad) {
            Column(modifier.wrapContentHeight(Alignment.Top)) {
                AdAttribution("Ad")

                Row {
                    icon?.let { icon ->
                        AdIconView(
                            Modifier
                                .size(60.dp)
                                .padding(5.dp)
                        ) {
                            icon.drawable?.toBitmap()?.let { bitmap ->
                                Image(bitmap = bitmap.asImageBitmap(), "Icon")
                            }
                        }
                    }

                    Column {
                        headline?.let {
                            AdHeadlineView(Modifier.padding(horizontal = 5.dp)) {
                                Text(text = it, style = MaterialTheme.typography.titleLarge)
                            }
                        }

                        starRating?.let {
                            AdStarRatingView {
                                Text(
                                    text = "$it",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                body?.let {
                    AdBodyView {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }

                AdMediaView(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Row(
                    Modifier
                        .align(Alignment.End)
                        .padding(5.dp)
                ) {
                    price?.let {
                        AdPriceView(
                            Modifier
                                .padding(5.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(text = it)
                        }
                    }

                    store?.let {
                        AdStoreView(
                            Modifier
                                .padding(5.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(text = it)
                        }
                    }

                    callToAction?.let { callToAction ->
                        AdCallToActionView(Modifier.padding(5.dp)) { AdButton(text = callToAction) }
                    }
                }
            }
        }
    }
}