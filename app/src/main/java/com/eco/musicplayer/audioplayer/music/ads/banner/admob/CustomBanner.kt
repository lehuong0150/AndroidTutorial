package com.eco.musicplayer.audioplayer.music.ads.banner.admob

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import com.eco.musicplayer.audioplayer.music.models.ads.banner.BannerType
import com.google.android.gms.ads.AdSize

class CustomBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var admobBanner: AdmobBanner? = null

    companion object {
        private const val TAG = "CustomBanner"
    }

    fun loadBanner(
        activity: Activity,
        adUnitId: String,
        type: BannerType,
        heightDp: Int? = null
    ) {
        Log.d(TAG, "Loading banner type: $type")

        admobBanner = AdmobBanner(activity)

        when (type) {
            BannerType.COLLAPSIBLE -> {
                updateLayoutParams {
                    height = (52 * resources.displayMetrics.density).toInt()
                }
            }

            BannerType.ADAPTIVE -> {
                updateLayoutParams {
                    height = (52 * resources.displayMetrics.density).toInt()
                }
            }

            BannerType.INLINE -> {
                val height = heightDp ?: 50
                updateLayoutParams {
                    this.height = (height * resources.displayMetrics.density).toInt()
                }
            }
        }

        doOnPreDraw {
//            val displayMetrics = resources.displayMetrics
//            val widthPixels = this.width  // width của CustomBanner sau preDraw
//            if (widthPixels <= 0) {
//                Log.e(TAG, "Banner width not ready")
//                return@doOnPreDraw
//            }
//
//            val adWidthDp = (widthPixels / displayMetrics.density).toInt()
//            when (type) {
//                BannerType.INLINE -> {
//                    val inlineAdSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context,adWidthDp)
//                    val expectedHeightPx = inlineAdSize.getHeightInPixels(context)
//                    if (expectedHeightPx > 0) {
//                        updateLayoutParams {
//                            height = expectedHeightPx
//                        }
//                    }
//                }
//
//                else -> {}
//            }
            admobBanner?.loadAd(this, adUnitId, type)
        }
    }

    fun destroy() {
        Log.d(TAG, "Destroying banner")
        admobBanner?.destroy()
        removeAllViews()
        admobBanner = null
    }
}