package com.eco.musicplayer.audioplayer.music.paywall

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity

open class FullscreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFullscreenMode()
    }

    private fun applyFullscreenMode() {
        configureSystemBars()
        makeSystemBarsTransparent()
    }

    private fun configureSystemBars() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                setupModernFullscreen()
            }

            else -> {
                setupLegacyFullscreen()
            }
        }
    }

    private fun setupModernFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)

            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars())

                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setupLegacyFullscreen() {
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        window.decorView.systemUiVisibility = flags
    }

    private fun makeSystemBarsTransparent() {
        window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }
}