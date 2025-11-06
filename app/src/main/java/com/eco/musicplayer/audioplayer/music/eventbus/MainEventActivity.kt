package com.eco.musicplayer.audioplayer.music.eventbus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainEventBinding
import com.eco.musicplayer.audioplayer.music.models.event.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, MessageFragment())
                .commit()
        }
        binding.btnSendToFragment.setOnClickListener {
            val message = binding.edMessage.text.toString()
            //post event
            EventBus.getDefault().post(MessageEvent(message))
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    //Activity nhan message tu Fragment
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageFromFragment(event: MessageEvent) {
        binding.edMessage.setText(getString(R.string.message, event.message))
    }
}