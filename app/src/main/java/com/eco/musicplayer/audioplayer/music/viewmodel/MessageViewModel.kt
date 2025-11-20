package com.eco.musicplayer.audioplayer.music.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eco.musicplayer.audioplayer.music.models.event.MessageEvent

class MessageViewModel : ViewModel(){

    private val _messageToFragment = MutableLiveData<MessageEvent>()
    val messageToFragment: LiveData<MessageEvent> get() = _messageToFragment

    private val _messageToActivity = MutableLiveData<MessageEvent>()
    val messageToActivity: LiveData<MessageEvent> get() = _messageToActivity

    fun sendToFragment(text:String){
        _messageToFragment.value = MessageEvent(text)
    }

    fun sendToActivity(text: String){
        _messageToActivity.value = MessageEvent(text)
    }
}