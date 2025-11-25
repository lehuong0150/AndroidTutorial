package com.eco.musicplayer.audioplayer.music.ads.banner.admob

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import com.eco.musicplayer.audioplayer.music.models.ads.banner.BannerType

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
                val height = heightDp ?: 250
                updateLayoutParams {
                    this.height = (height * resources.displayMetrics.density).toInt()
                }
            }
        }

        doOnPreDraw {
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