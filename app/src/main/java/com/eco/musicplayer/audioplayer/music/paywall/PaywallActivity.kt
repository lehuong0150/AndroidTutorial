package com.eco.musicplayer.audioplayer.music.paywall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallBinding
import com.eco.musicplayer.audioplayer.music.extension.applyFigmaTextStyle
import com.eco.musicplayer.audioplayer.music.extension.getLifetimeId
import com.eco.musicplayer.audioplayer.music.extension.getOfferIdForProduct
import com.eco.musicplayer.audioplayer.music.extension.getSubscriptionIds
import com.eco.musicplayer.audioplayer.music.extension.hasSubscription
import com.eco.musicplayer.audioplayer.music.remoteConfig.RemoteConfig
import com.eco.musicplayer.audioplayer.music.remoteConfig.RemoteConfigHelper

class PaywallActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PaywallActivity"
        const val EXTRA_SHOW_TEST_UI = "extra_show_test_ui"

        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
        const val EXTRA_SUBSCRIPTION_OFFER_ID = "extra_subscription_offer_id"
        const val EXTRA_LIFETIME_ID = "extra_lifetime_id"
        const val EXTRA_SELECTION_POSITION = "extra_selection_position"
    }
    private var keyRemoteConfig ="pricing_config"
    private lateinit var binding: ActivityPaywallBinding
    private val remoteConfig = RemoteConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val showTestUI = intent.getBooleanExtra(EXTRA_SHOW_TEST_UI, false)

        if (showTestUI) {
            Log.d(TAG, "Test mode: showing manual selection UI")
            setupUI()
            setupClickListeners()
        } else {
            Log.d(TAG, "Production mode: auto-routing to paywall")
            fetchConfigAndRoute()
        }
    }


    private fun fetchConfigAndRoute() {
        remoteConfig.fetchAndActivate {
            Log.d(TAG, "Remote Config loaded successfully")
                runOnUiThread {
                    autoRouteToPaywall()
            }
        }
    }

    private fun autoRouteToPaywall() {
        val config = RemoteConfigHelper.getPaywallConfig(keyRemoteConfig)
        val uiType = config.uiType ?: "5"

        val targetActivity = when (uiType) {
            "1" -> PaywallSaleActivity::class.java
            "2" -> DialogWeeklyActivity::class.java
            "3" -> DialogYearlyActivity::class.java
            "4" -> PaywallOnboardingActivity::class.java
            "5" -> UnlockActivity::class.java
            "6" -> PaywallBottomSheetActivity::class.java
            "7" -> PaywallFullActivity::class.java
            else -> PaywallBottomSheetActivity::class.java
        }

        val intent = Intent(this, targetActivity)
        //config.logProducts(TAG)
        if (config.hasSubscription()) {
            val subscriptionIds = config.getSubscriptionIds()
            val offerIds = subscriptionIds.map { subId ->
                config.getOfferIdForProduct(subId)
            }

            intent.putStringArrayListExtra(
                EXTRA_SUBSCRIPTION_ID,
                ArrayList(subscriptionIds)
            )
            intent.putStringArrayListExtra(
                EXTRA_SUBSCRIPTION_OFFER_ID,
                ArrayList(offerIds)
            )

            Log.d(TAG, "Subscriptions: ${subscriptionIds.zip(offerIds)}")
        }

        config.getLifetimeId()?.let { lifetimeId ->
            intent.putExtra(EXTRA_LIFETIME_ID, lifetimeId)
        }

        config.selectionPosition?.let {
            intent.putExtra(EXTRA_SELECTION_POSITION, it)
        }

        startActivity(intent)
    }

    private fun setupUI() {
        binding.txtPercent.applyFigmaTextStyle(
            colorStartHex = "#F3F3FC",
            colorEndHex = "#A2B1DA",
            shadowOffsetY = 3.0f,
            targetScale = 1.0f,
            context = this
        )

        binding.txtOff.applyFigmaTextStyle(
            colorStartHex = "#EEEEF2",
            colorEndHex = "#C8D0E7",
            shadowOffsetY = 2.0f,
            targetScale = 0.72f,
            context = this
        )

        binding.txtPercent.text = getString(R.string.dialog_weekly_30, 30)
    }

    private fun setupClickListeners() = with(binding) {
        btnPayWall1.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallSaleActivity::class.java))
        }

        btnPayWall2.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, DialogWeeklyActivity::class.java))
        }

        btnPayWall3.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, DialogYearlyActivity::class.java))
        }

        btnPayWall4.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallOnboardingActivity::class.java))
        }

        btnPayWall5.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, UnlockActivity::class.java))
        }

        btnPayWall6.setOnClickListener {
            fetchConfigAndRoute()
            startActivity(Intent(this@PaywallActivity, PaywallBottomSheetActivity::class.java))
        }

        btnPayWall7.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallFullActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteConfig.destroy()
    }

    override fun onRestart() {
        super.onRestart()
        setupClickListeners()
    }
}