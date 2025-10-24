package com.eco.musicplayer.audioplayer.music.layout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallSaleBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallSaleBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        setupBottomSheet()
        setupClickListeners()
        showLoading()

        binding.root.postDelayed({
            val isSuccess = false
            if (isSuccess) showSuccess() else showFailed()
        }, 2000)
    }

    private fun setupBottomSheet() {
        val bottomSheetView = binding.layoutContent
        val topLayout = binding.layoutGif

        val screenHeight = resources.displayMetrics.heightPixels

        val topRatio = 0.68f
        val bottomRatio = 0.36f

        topLayout?.let {
            val params = it.layoutParams
            params.height = (screenHeight * topRatio).toInt()
            it.layoutParams = params
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED

            bottomSheetView.post {
                peekHeight = (screenHeight * bottomRatio).toInt()
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        layoutContent.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        txtTryAgain.setOnClickListener {
            showLoading()
            root.postDelayed({
                showSuccess() // hoặc showFailed()
            }, 2000)
        }

        btnClaimOffer.setOnClickListener {
            // handleClaimOffer()
        }
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun showLoading() = with(binding) {
        groupContent.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        pgbLoadInfo.visibility = View.VISIBLE
        btnClaimOffer.isEnabled = false
        btnClaimOffer.text= ""
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
    }

    private fun showSuccess() = with(binding) {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        pgbLoadInfo.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        btnClaimOffer.visibility = View.VISIBLE
        btnClaimOffer.text= getString(R.string.paywall_sale_btn_offer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showFailed() = with(binding) {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.INVISIBLE
        btnClaimOffer.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onBackPressed() {
        bottomSheetBehavior.run {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                super.onBackPressed()
            }
        }
    }
}
