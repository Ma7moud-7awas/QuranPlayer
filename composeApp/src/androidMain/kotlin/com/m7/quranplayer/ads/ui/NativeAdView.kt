package com.m7.quranplayer.ads.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

private val LocalNativeAdView = staticCompositionLocalOf<NativeAdView?> { null }

@Composable
fun NativeAdView(
    ad: NativeAd,
    modifier: Modifier = Modifier,
    adContent: @Composable NativeAd.() -> Unit,
) {
    val localContext = LocalContext.current
    val nativeAdView = remember {
        NativeAdView(localContext).apply { id = View.generateViewId() }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            nativeAdView.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                addView(
                    ComposeView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        setContent {
                            CompositionLocalProvider(LocalNativeAdView provides nativeAdView) {
                                ad.adContent()
                            }
                        }
                    }
                )
            }
        }
    )
    SideEffect { nativeAdView.setNativeAd(ad) }
}

@Composable
fun AdAttribution(text: String = "Ad", modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(ButtonDefaults.buttonColors().containerColor)
            .padding(horizontal = 3.dp)
    ) {
        Text(
            text = text,
            color = ButtonDefaults.buttonColors().contentColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun AdAdvertiserView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                setContent(content)
                nativeAdView.advertiserView = this
            }
        },
        modifier = modifier,
        update = { view -> view.setContent(content) },
    )
}

@Composable
fun AdIconView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LocalNativeAdView.current?.let { nativeAdView ->
        val localContext = LocalContext.current
        val localComposeView = remember {
            ComposeView(localContext).apply { id = View.generateViewId() }
        }

        AndroidView(
            factory = {
                nativeAdView.iconView = localComposeView
                localComposeView.apply { setContent(content) }
            },
            modifier = modifier,
        )
    }
}

@Composable
fun AdHeadlineView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView =
        remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.headlineView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

@Composable
fun AdStarRatingView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView =
        remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.starRatingView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

@Composable
fun AdBodyView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current
        ?: throw IllegalStateException("NativeAdView is null")
    val localContext = LocalContext.current
    val localComposeView =
        remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.bodyView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

@Composable
fun AdMediaView(modifier: Modifier = Modifier, scaleType: ImageView.ScaleType? = null) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    AndroidView(
        factory = { MediaView(localContext) },
        update = { view ->
            nativeAdView.mediaView = view
            scaleType?.let { type -> view.setImageScaleType(type) }
        },
        modifier = modifier,
    )
}

@Composable
fun AdChoicesView(modifier: Modifier = Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    AndroidView(
        factory = {
            AdChoicesView(localContext).apply {
                minimumWidth = 15
                minimumHeight = 15
            }
        },
        update = { view -> nativeAdView.adChoicesView = view },
        modifier = modifier,
    )
}

@Composable
fun AdPriceView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView =
        remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.priceView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

@Composable
fun AdStoreView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView =
        remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.storeView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

@Composable
fun AdCallToActionView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView =
        remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.callToActionView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

// Note: The Jetpack Compose button implements a click handler which overrides the native
// ad click handler, causing issues. Use the NativeAdButton which does not implement a
// click handler. To handle native ad clicks, use the NativeAd AdListener onAdClicked
// callback.
@Composable
fun AdButton(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(ButtonDefaults.shape)
                .background(ButtonDefaults.buttonColors().containerColor)
    ) {
        Text(
            text = text,
            color = ButtonDefaults.buttonColors().contentColor,
            modifier = Modifier.padding(
                horizontal = ButtonDefaults.ContentPadding.calculateStartPadding(
                    LocalLayoutDirection.current
                ),
                vertical = 5.dp
            )
        )
    }
}

@Composable
operator fun PaddingValues.div(by: Int): PaddingValues =
    LocalLayoutDirection.current.let { direction ->
        PaddingValues(
            start = calculateStartPadding(direction) / by,
            top = calculateTopPadding() / by,
            end = calculateEndPadding(direction) / by,
            bottom = calculateBottomPadding() / by
        )
    }