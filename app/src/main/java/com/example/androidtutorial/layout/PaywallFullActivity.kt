package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityPaywallFullBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallFullActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallFullBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    // State variables
    private var isYearlySelected = true
    private var isFreeTrialEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaywallFullBinding.inflate(layoutInflater)
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

    // Cấu hình Bottom Sheet behavior

    private fun setupBottomSheet() {
        val bottomSheetView = binding.layoutPwFull

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED

            bottomSheetView.post {
                val screenHeight = resources.displayMetrics.heightPixels
                val percentage = 0.65f
                peekHeight = (screenHeight * percentage).toInt()
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            layoutPwFull.setOnClickListener {
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
        }, 4000)
    }

    // Hiển thị loading state
    private fun showLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.VISIBLE
            pgbLoadWeekly.visibility = View.VISIBLE
            pgbLoadYearly.visibility = View.VISIBLE

            groupWeekly.visibility = View.INVISIBLE
            groupYearly.visibility = View.INVISIBLE
            groupContent.visibility = View.INVISIBLE
        }
    }

    //Hiển thị content state
    private fun showSuccess() {
        with(binding) {
            pgbLoadInfo.visibility = View.INVISIBLE
            pgbLoadWeekly.visibility = View.INVISIBLE
            pgbLoadYearly.visibility = View.INVISIBLE

            groupWeekly.visibility = View.VISIBLE
            groupYearly.visibility = View.VISIBLE
            groupContent.visibility = View.VISIBLE

            // Update UI với state hiện tại
            updateUI()
        }
    }


    //Cập nhật toàn bộ UI dựa trên state hiện tại
    private fun updateUI() {
        updatePlanBackground()
        updateButtonText()
        updateFreeTrialText()
        updateContentText()
    }

    // Cập nhật background của yearly/weekly plans
    private fun updatePlanBackground() {
        with(binding) {
            if (isYearlySelected) {
                // Yearly selected
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_selected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_full_selected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_unselected)
            } else {
                // Weekly selected
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_unselected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_unselected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_full_selected)
            }
        }
    }

    // Cập nhật text của button chính
    private fun updateButtonText() {
        binding.btnTryTree.text = if (isFreeTrialEnabled) {
            getString(R.string.paywall_full_btn_free)
        } else {
            getString(R.string.paywall_full_btn_continue)
        }
    }

    // Cập nhật text free trial (dưới button)
    private fun updateFreeTrialText() {
        binding.txtPwFreeTrial.text = if (isFreeTrialEnabled) {
            getString(R.string.paywall_full_free_trial)
        } else {
            getString(R.string.paywall_full_cancel)
        }
    }

    // Cập nhật content text (auto renew text)
    private fun updateContentText() {
        binding.txtPwContent.text = when {
            // Free trial enabled (Try for free button)
            isFreeTrialEnabled && isYearlySelected ->
                getString(R.string.paywall_full_content_yearly)

            isFreeTrialEnabled && !isYearlySelected ->
                getString(R.string.paywall_full_content_weekly)

            // Free trial disabled (Continue button)
            !isFreeTrialEnabled && isYearlySelected ->
                getString(R.string.paywall_full_content_trial_yearly)

            !isFreeTrialEnabled && !isYearlySelected ->
                getString(R.string.paywall_full_content_trial_weekly)

            else -> ""
        }
    }
}
