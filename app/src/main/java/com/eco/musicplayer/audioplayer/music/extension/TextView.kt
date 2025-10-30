package com.eco.musicplayer.audioplayer.music.extension

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import android.view.View
import com.eco.musicplayer.audioplayer.music.R

// Hàm chung để áp dụng tất cả Style Figma (Gradient, Shadow) cho một TextView
fun TextView.applyFigmaTextStyle(
    colorStartHex: String,
    colorEndHex: String,
    shadowOffsetY: Float,
    targetScale: Float,
    context: android.content.Context
) {
    // 1. Áp dụng Custom Font (Giữ nguyên)
    try {
        val typeface = ResourcesCompat.getFont(context, R.font.funnel_sans_extra_bold)
        this.typeface = typeface
    } catch (e: Exception) {
        // ... Log lỗi nếu font không tìm thấy
    }

    // 2. Cài đặt Kích thước và Màu (Giữ nguyên)
    val baseSize = this.textSize
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseSize * targetScale)

    // Cần phải set Layer Type để Shadow hoạt động
    this.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

    // 3. Cài đặt Drop Shadow (Giữ nguyên)
    val shadowRadius = 6.0f
    val shadowColor = Color.argb(64, 0, 0, 0)
    this.setShadowLayer(shadowRadius, 0f, shadowOffsetY, shadowColor)

    // 4. Cài đặt Gradient (Tối ưu hóa tính toán trong OnLayoutChange)
    val colorStart = Color.parseColor(colorStartHex)
    val colorEnd = Color.parseColor(colorEndHex)

    // Sử dụng OnLayoutChangeListener để tính toán Shader sau khi TextView có kích thước
    this.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View?, left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        ) {
            val textWidth = v?.width?.toFloat() ?: 0f
            val textHeight = v?.height?.toFloat() ?: 0f

            if (textWidth > 0f && textHeight > 0f) {
                // SỬA ĐỔI QUAN TRỌNG: Dùng tọa độ (0, 0) đến (Width, Height)
                // để tạo Gradient Chéo nhẹ (Diagonal) hoặc Dọc, mô phỏng Vector XML
                val shader = LinearGradient(
                    0f, 0f,
                    0f, textHeight, // Đổi từ textWidth sang 0f để ƯU TIÊN GRADIENT DỌC
                    intArrayOf(colorStart, colorEnd),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )

                // Áp dụng Shader
                (v as? TextView)?.paint?.shader = shader
                v?.invalidate()

                // Loại bỏ Listener sau lần đầu tiên để tối ưu hiệu suất
                v?.removeOnLayoutChangeListener(this)
            }
        }
    })

    // Nếu OnLayoutChange không gọi lại, chúng ta thử set lần đầu tiên
    if (this.width > 0 && this.height > 0) {
        val textWidth = this.width.toFloat()
        val textHeight = this.height.toFloat()
        val shader = LinearGradient(
            0f, 0f,
            0f, textHeight,
            intArrayOf(colorStart, colorEnd),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        this.paint.shader = shader
    }
}