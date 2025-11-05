package com.eco.musicplayer.audioplayer.music.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eco.musicplayer.audioplayer.music.models.modelActivity.ActivityResult
import com.eco.musicplayer.audioplayer.music.models.modelActivity.ActivityUiState
import com.eco.musicplayer.audioplayer.music.models.modelActivity.BundleData
import com.eco.musicplayer.audioplayer.music.models.modelActivity.InstanceInfo
import com.eco.musicplayer.audioplayer.music.models.modelActivity.LaunchModeInfo
import com.eco.musicplayer.audioplayer.music.utils.NavigationEvent

class MainViewModel : ViewModel() {
    companion object {
        var instanceCount = 0
        private const val TAG_LIFECYCLE = "LifecycleMainActivity"
        private const val TAG_LAUNCH_MODE = "LaunchMode"
    }

    private val _uiState = MutableLiveData<ActivityUiState>()
    val uiState: LiveData<ActivityUiState> = _uiState

    private val _navigationEvent = MutableLiveData<NavigationEvent?>()
    val navigationEvent: LiveData<NavigationEvent?> = _navigationEvent

    private var currentState = ActivityUiState()
    private var chosenAction: String? = null
    private var isShareDialogOpen = false

    init {
        _uiState.value = currentState
    }

    fun updateEditTextContent(content: String) {
        currentState = currentState.copy(
            editTextContent = content,
            toastMessage = content
        )
        _uiState.value = currentState
    }

    fun onActivityResult(result: ActivityResult) {
        currentState = currentState.copy(
            editTextContent = result.message,
            toastMessage = result.message
        )
        _uiState.value = currentState
    }

    fun logInstanceCreation(taskId: Int) {
        instanceCount++
        val instanceId = System.identityHashCode(this)

        val instanceInfo = InstanceInfo(
            taskId = taskId,
            instanceId = instanceId,
            totalInstances = instanceCount
        )

        currentState = currentState.copy(
            taskId = instanceInfo.taskId,
            instanceId = instanceInfo.instanceId,
            instanceCount = instanceInfo.totalInstances
        )
        _uiState.value = currentState

        Log.d(TAG_LAUNCH_MODE, "-----------------------------")
        Log.d(TAG_LAUNCH_MODE, "MainActivity created")
        Log.d(TAG_LAUNCH_MODE, "Task ID: ${instanceInfo.taskId}")
        Log.d(TAG_LAUNCH_MODE, "Instance ID: ${instanceInfo.instanceId}")
        Log.d(TAG_LAUNCH_MODE, "Total instances: ${instanceInfo.totalInstances}")
    }

    fun onFinishClicked() {
        chosenAction = "Finish"
    }

    fun onRotationClicked() {
        chosenAction = "Rotation"
    }

    fun onShareClicked(): Boolean {
        if (isShareDialogOpen) {
            return false
        }

        isShareDialogOpen = true
        chosenAction = "Share"
        return true
    }

    fun onShareDialogDismissed() {
        isShareDialogOpen = false
    }

    fun onSecondActivityClicked() {
        chosenAction = "SecondActivity"
    }

    fun onLaunchModeClicked(mode: String, taskId: Int) {
        val launchModeInfo = LaunchModeInfo(
            mode = mode,
            taskId = taskId,
            instanceCount = instanceCount + 1
        )

        Log.d(TAG_LAUNCH_MODE, "Launch mode clicked: ${launchModeInfo.mode}")

        when (launchModeInfo.mode) {
            "Standard" -> {
                val count = ++instanceCount
                val bundleData = BundleData(
                    taskId = -1,
                    instanceLabel = "Standard via Flag #${count}",
                    startTime = System.currentTimeMillis(),
                    isForeground = true
                )
                _navigationEvent.value = NavigationEvent.OpenStandardActivity(
                    taskId = -1,
                    instanceCount = count,
                    bundleData = bundleData,
                    method = "STANDARD_VIA_FLAG"
                )
            }

            "SingleTop" -> _navigationEvent.value = NavigationEvent.OpenSingleTopActivity
            "SingleTask" -> _navigationEvent.value = NavigationEvent.OpenSingleTaskActivity
            "SingleInstance" -> _navigationEvent.value = NavigationEvent.OpenSingleInstanceActivity
            else -> Log.w(TAG_LAUNCH_MODE, "Unknown launch mode: ${launchModeInfo.mode}")
        }
    }

    fun onParcelableClicked(taskId: Int) {
        val count = ++instanceCount
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
        val count = ++instanceCount
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
        val count = ++instanceCount
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
        currentState = currentState.copy(toastMessage = null)
        _uiState.value = currentState
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun restoreEditTextContent(content: String) {
        currentState = currentState.copy(editTextContent = content)
        _uiState.value = currentState
    }

    fun onActivityDestroy() {
        instanceCount--
        Log.d(TAG_LAUNCH_MODE, "Activity destroyed. Remaining instances: $instanceCount")
    }

    override fun onCleared() {
        super.onCleared()
        instanceCount--
        Log.d(TAG_LAUNCH_MODE, "ViewModel cleared - Remaining instances: $instanceCount")
    }
}