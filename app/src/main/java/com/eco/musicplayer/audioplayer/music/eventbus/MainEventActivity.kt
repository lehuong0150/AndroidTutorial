package com.eco.musicplayer.audioplayer.music.eventbus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainEventBinding
import com.eco.musicplayer.audioplayer.music.eventbus.fragment.MessageFragment
import com.eco.musicplayer.audioplayer.music.viewmodel.MessageViewModel
import com.eco.musicplayer.audioplayer.music.models.event.MessageEvent
import com.eco.musicplayer.audioplayer.music.service.MainServiceActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainEventBinding
    private val viewModel: MessageViewModel by viewModels()

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, MessageFragment())
                .commit()
        }

        binding.btnSendToFragment.setOnClickListener {
            val message = binding.edMessage.text.toString()
            if (message.isNotEmpty()) {
                EventBus.getDefault().post(MessageEvent(message))
                viewModel.sendToFragment(message)
                binding.edMessage.text?.clear()
            }
        }

        viewModel.messageToActivity.observe(this) { message ->
            binding.tvMessage.setText(getString(R.string.message_from, message))
        }

        binding.btnDemoService.setOnClickListener {
            val intent = Intent(this, MainServiceActivity::class.java)
            startActivity(intent)
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
        binding.edMessage.setText(getString(R.string.message_from, event.message))
    }
}