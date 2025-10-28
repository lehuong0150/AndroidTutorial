package com.eco.musicplayer.audioplayer.music.extension

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ReplacementSpan
import android.widget.TextView
import com.eco.musicplayer.audioplayer.music.R

// Giả định R.string.dialog_weekly_30 là chuỗi có format như "<xliff:g example="30" id="percent">%1$d</xliff:g>%% OFF"
fun TextView.setDiscountWithGradient(percent: Int) {
    val fullText = context.getString(R.string.dialog_weekly_30, percent)
    val spannableString = SpannableString(fullText)

    val offIndex = fullText.indexOf("OFF", ignoreCase = true)
    if (offIndex == -1) {
        this.text = fullText
        return
    }

    // --- 1. Phần chính (Ví dụ: "30%") ---
    spannableString.setSpan(
        object : ReplacementSpan() {
            override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
                // Thêm độ dày giả (Fake Bold) khi đo kích thước
                val originalFakeBold = paint.isFakeBoldText
                paint.isFakeBoldText = true // Áp dụng độ dày
                val width = paint.measureText(text, start, end).toInt()
                paint.isFakeBoldText = originalFakeBold // Phục hồi
                return width
            }

            override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
                // Lưu trạng thái gốc
                val originalShader = paint.shader
                val originalFakeBold = paint.isFakeBoldText

                // 🔑 Thay đổi chính: Thêm độ dày giả (Fake Bold)
                paint.isFakeBoldText = true

                val textToDraw = text?.subSequence(start, end).toString()
                val textWidth = paint.measureText(textToDraw)

                // ... (Các thiết lập Gradient giữ nguyên) ...
                val colorStart = Color.parseColor("#F3F3FC")
                val colorEnd = Color.parseColor("#A2B1DA")

                val shader = LinearGradient(
                    x, 0f,
                    x + textWidth, 0f,
                    intArrayOf(colorStart, colorEnd),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )

                paint.shader = shader
                canvas.drawText(textToDraw, x, y.toFloat(), paint)

                // Phục hồi trạng thái
                paint.shader = originalShader
                paint.isFakeBoldText = originalFakeBold
            }
        },
        0,
        offIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // --- 2. Chữ "OFF" (Gradient nhạt, font nhỏ hơn và căn chỉnh dọc) ---
    spannableString.setSpan(
        object : ReplacementSpan() {
            private val scale = 0.6f

            override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
                val originalTextSize = paint.textSize
                val originalFakeBold = paint.isFakeBoldText // Lưu trạng thái

                paint.textSize = originalTextSize * scale
                paint.isFakeBoldText = true // Áp dụng độ dày

                val width = paint.measureText(text, start, end).toInt()

                paint.textSize = originalTextSize
                paint.isFakeBoldText = originalFakeBold // Phục hồi
                return width
            }

            override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
                // Lưu trạng thái gốc
                val originalShader = paint.shader
                val originalTextSize = paint.textSize
                val originalFakeBold = paint.isFakeBoldText

                // 🔑 Thay đổi chính: Thêm độ dày giả (Fake Bold)
                paint.isFakeBoldText = true

                // Giảm font size cho phần này
                paint.textSize = originalTextSize * scale

                val textToDraw = text?.subSequence(start, end).toString()
                val textWidth = paint.measureText(textToDraw)

                // ... (Các thiết lập Gradient giữ nguyên) ...
                val colorStart = Color.parseColor("#EEEEF2")
                val colorEnd = Color.parseColor("#C8D0E7")

                val shader = LinearGradient(
                    x, 0f,
                    x + textWidth, 0f,
                    intArrayOf(colorStart, colorEnd),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader

                // Tính toán căn chỉnh dọc
                val verticalOffset = (originalTextSize - (originalTextSize * scale)) * 0.4f

                canvas.drawText(textToDraw, x, y.toFloat() - verticalOffset, paint)

                // Phục hồi Paint state
                paint.shader = originalShader
                paint.textSize = originalTextSize
                paint.isFakeBoldText = originalFakeBold // Phục hồi độ dày
            }
        },
        offIndex,
        fullText.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    this.text = spannableString
}