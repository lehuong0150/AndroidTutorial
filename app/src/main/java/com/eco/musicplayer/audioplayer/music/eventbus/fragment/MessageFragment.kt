package com.eco.musicplayer.audioplayer.music.eventbus.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentMessageBinding
import com.eco.musicplayer.audioplayer.music.models.event.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MessageFragment : Fragment() {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        //Fragment gui den message den Activity
        binding.btnSendToActivity.setOnClickListener {
            EventBus.getDefault().post(MessageEvent(getString(R.string.hello_blank_fragment)))
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent){
        binding.tvMessage.text = getString(R.string.received_message, event.message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Giai phong bo nho
        _binding = null
    }
}