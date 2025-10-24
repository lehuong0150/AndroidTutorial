package com.eco.musicplayer.audioplayer.music.layout

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallBottomSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var isYearlySelected = true
    private var isFreeTrialEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaywallBottomSheetBinding.inflate(layoutInflater)
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
        setupInitialState()
    }

    //Cấu hình Bottom Sheet behavior
    private fun setupBottomSheet() {
        val bottomSheetView = binding.layoutPwBottomSheet

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED

            bottomSheetView.post {
                val screenHeight = resources.displayMetrics.heightPixels
                val percentage = 0.68f
                peekHeight = (screenHeight * percentage).toInt()
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            layoutPwBottomSheet.setOnClickListener {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            layoutPwYearly.setOnClickListener {
                if (!isYearlySelected) {
                    isYearlySelected = true
                    updateUI()
                }
            }

            layoutPwWeekly.setOnClickListener {
                if (isYearlySelected) {
                    isYearlySelected = false
                    updateUI()
                }
            }

            btnTryTree.setOnClickListener {
                isFreeTrialEnabled = !isFreeTrialEnabled
                updateUI()
            }

            btnClose.setOnClickListener {
                finish()
            }
        }
    }

    private fun setupInitialState() {
        showLoading()

        binding.root.postDelayed({
            showSuccess()
        }, 2000)
    }

    private fun showLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.VISIBLE
            pgbLoadWeekly.visibility = View.VISIBLE
            pgbLoadYearly.visibility = View.VISIBLE

            groupWeekly.visibility = View.INVISIBLE
            groupYearly.visibility = View.INVISIBLE
            groupContent.visibility = View.INVISIBLE

            btnTryTree.isEnabled = false
            btnTryTree.text = ""
            btnTryTree.setBackgroundResource(R.drawable.bg_pw_loading)
        }
    }

    private fun showSuccess() {
        with(binding) {
            pgbLoadInfo.visibility = View.INVISIBLE
            pgbLoadWeekly.visibility = View.INVISIBLE
            pgbLoadYearly.visibility = View.INVISIBLE

            groupWeekly.visibility = View.VISIBLE
            groupYearly.visibility = View.VISIBLE
            groupContent.visibility = View.VISIBLE

            btnTryTree.isEnabled = true
            btnTryTree.text = getString(R.string.paywall_bottom_sheet_btn_free)
            btnTryTree.setBackgroundResource(R.drawable.btn_pw_bottom_sheet_free)
            updateUI()
        }
    }

    // Cập nhật toàn bộ UI dựa trên state hiện tại
    private fun updateUI() {
        updatePlanBackground()
        updateButtonText()
        updateFreeTrialText()
        updateContentText()
    }

    //Cập nhật background của yearly/weekly plans
    private fun updatePlanBackground() {
        with(binding) {
            if (isYearlySelected) {
                // Yearly selected
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_selected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_selected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_unselected)
            } else {
                // Weekly selected
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_unselected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_yearly_unselected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_selected)
            }
        }
    }

    // Cập nhật text của button chính
    private fun updateButtonText() {
        binding.btnTryTree.text = if (isFreeTrialEnabled) {
            getString(R.string.paywall_bottom_sheet_btn_free)
        } else {
            getString(R.string.paywall_full_btn_continue)
        }
    }

    //Cập nhật text free trial (dưới button)
    private fun updateFreeTrialText() {
        binding.txtPwFreeTrial.text = if (isFreeTrialEnabled) {
            getString(R.string.paywall_bottom_sheet_free_trial)
        } else {
            getString(R.string.paywall_bottom_sheet_cancel)
        }
    }

    //Cập nhật content text (auto renew text)
    private fun updateContentText() {
        binding.txtPwContent.text = when {
            isFreeTrialEnabled && isYearlySelected ->
                getString(R.string.paywall_bottom_sheet_content_yearly)

            isFreeTrialEnabled && !isYearlySelected ->
                getString(R.string.paywall_bottom_sheet_content_weekly)

            // Free trial disabled (Continue button)
            !isFreeTrialEnabled && isYearlySelected ->
                getString(R.string.paywall_bottom_sheet_content_trial_yearly)

            !isFreeTrialEnabled && !isYearlySelected ->
                getString(R.string.paywall_bottom_sheet_content_trial_weekly)

            else -> ""
        }
    }
}
