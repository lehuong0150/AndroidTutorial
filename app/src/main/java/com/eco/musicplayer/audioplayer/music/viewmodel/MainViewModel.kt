package com.eco.musicplayer.audioplayer.music.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eco.musicplayer.audioplayer.music.models.modelactivity.ActivityResult
import com.eco.musicplayer.audioplayer.music.models.modelactivity.ActivityUiState
import com.eco.musicplayer.audioplayer.music.models.modelactivity.BundleData
import com.eco.musicplayer.audioplayer.music.utils.NavigationEvent

class MainViewModel : ViewModel() {

    companion object {
        private const val TAG_LAUNCH_MODE = "LaunchMode"
    }

    private var localInstanceCounter = 0

    private val _uiState = MutableLiveData(ActivityUiState())
    val uiState: LiveData<ActivityUiState> = _uiState

    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent

    private var isShareDialogOpen = false

    private fun nextInstanceNumber(): Int = ++localInstanceCounter

    fun logInstanceCreation(taskId: Int) {
        val instanceId = System.identityHashCode(this)
        val count = localInstanceCounter

        _uiState.value = _uiState.value?.copy(
            taskId = taskId,
            instanceId = instanceId,
            instanceCount = count
        )

        Log.d(TAG_LAUNCH_MODE, "=============================")
        Log.d(TAG_LAUNCH_MODE, "MainActivity instance created")
        Log.d(TAG_LAUNCH_MODE, "Task ID: $taskId")
        Log.d(TAG_LAUNCH_MODE, "ViewModel ID: $instanceId")
        Log.d(TAG_LAUNCH_MODE, "Local instance count: $count")
        Log.d(TAG_LAUNCH_MODE, "=============================")
    }

    fun updateEditTextContent(content: String) {
        _uiState.value = _uiState.value?.copy(
            editTextContent = content,
            toastMessage = content
        )
    }

    fun onActivityResult(result: ActivityResult) {
        _uiState.value = _uiState.value?.copy(
            editTextContent = result.message,
            toastMessage = result.message
        )
    }

    fun onShareClicked(): Boolean {
        if (isShareDialogOpen) return false
        isShareDialogOpen = true
        return true
    }

    fun onShareDialogDismissed() {
        isShareDialogOpen = false
    }

    fun onLaunchModeClicked(mode: String, taskId: Int) {
        when (mode) {
            "Standard" -> openStandardActivity(taskId)
            "SingleTop" -> _navigationEvent.value = NavigationEvent.OpenSingleTopActivity
            "SingleTask" -> _navigationEvent.value = NavigationEvent.OpenSingleTaskActivity
            "SingleInstance" -> _navigationEvent.value = NavigationEvent.OpenSingleInstanceActivity
        }
    }

    private fun openStandardActivity(taskId: Int) {
        val count = nextInstanceNumber()
        val bundleData = BundleData(
            taskId = taskId,
            instanceLabel = "Standard #${count}",
            startTime = System.currentTimeMillis(),
            isForeground = true
        )
        _navigationEvent.value = NavigationEvent.OpenStandardActivity(
            taskId = taskId,
            instanceCount = count,
            bundleData = bundleData,
            method = "STANDARD"
        )
    }

    fun onParcelableClicked(taskId: Int) {
        val count = nextInstanceNumber()
        val bundleData = BundleData(
            taskId = taskId,
            instanceLabel = "Parcelable Demo #${count}",
            startTime = System.currentTimeMillis(),
            isForeground = true
        )
        _navigationEvent.value = NavigationEvent.OpenStandardActivity(
            taskId = taskId,
            instanceCount = count,
            bundleData = bundleData,
            method = "PARCELABLE"
        )
    }

    fun onSerializableClicked(taskId: Int) {
        val count = nextInstanceNumber()
        val bundleData = BundleData(
            taskId = taskId,
            instanceLabel = "Serializable Demo #${count}",
            startTime = System.currentTimeMillis(),
            isForeground = true
        )
        _navigationEvent.value = NavigationEvent.OpenStandardActivity(
            taskId = taskId,
            instanceCount = count,
            bundleData = bundleData,
            method = "SERIALIZABLE"
        )
    }

    fun onBundleManualClicked(taskId: Int) {
        val count = nextInstanceNumber()
        val bundleData = BundleData(
            taskId = taskId,
            instanceLabel = "Bundle Manual #${count}",
            startTime = System.currentTimeMillis(),
            isForeground = true
        )
        _navigationEvent.value = NavigationEvent.OpenStandardActivity(
            taskId = taskId,
            instanceCount = count,
            bundleData = bundleData,
            method = "BUNDLE_MANUAL"
        )
    }

    fun onIntentFlagSelected(mode: String) {
        Log.d(TAG_LAUNCH_MODE, "Intent flag selected: $mode")
        _navigationEvent.value = NavigationEvent.OpenIntentFlagActivity(mode)
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value?.copy(toastMessage = null)
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun restoreEditTextContent(content: String) {
        _uiState.value = _uiState.value?.copy(editTextContent = content)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG_LAUNCH_MODE, "MainViewModel cleared - was serving instance #$localInstanceCounter")
    }
}