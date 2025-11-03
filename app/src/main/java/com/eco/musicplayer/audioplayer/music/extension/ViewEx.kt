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
        if (now - view.lastClickTime >= CLICK_THROTTLE_MS) {
            view.lastClickTime = now
            action(view)
        }
    }
}

// Tạo ID cho tag
private object ViewTag {
    val last_click_time = R.id.last_click_time
}