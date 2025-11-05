package com.eco.musicplayer.audioplayer.music

import android.os.SystemClock
import android.view.View

private const val CLICK_THROTTLE_MS = 500L

private var View.lastClickTime: Long
    get() = getTag(R.id.last_click_time) as? Long ?: 0L
    set(value) = setTag(R.id.last_click_time, value)

fun View.setOnClickListenerDebounced(action: (View) -> Unit) {
    setOnClickListener { view ->
        val now = SystemClock.elapsedRealtime()

        // Kiểm tra TRƯỚC khi thực hiện bất kỳ thao tác nào
        if (now - view.lastClickTime < CLICK_THROTTLE_MS) {
            return@setOnClickListener // Block ngay, không làm gì cả
        }

        // Update thời gian NGAY LẬP TỨC trước khi execute action
        view.lastClickTime = now

        // Thực hiện action
        action(view)
    }
}