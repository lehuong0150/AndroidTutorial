//package com.eco.musicplayer.audioplayer.music.repository
//
//import com.eco.musicplayer.audioplayer.music.utils.NavigationEvent
//
//class ActivityRepository {
//
//    private var instanceCount = 0
//    private val instances = mutableListOf<ActivityInstance>()
//
//    fun createInstance(taskId: Int): ActivityInstance {
//        instanceCount++
//        val instance = ActivityInstance(
//            id = System.identityHashCode(this),
//            taskId = taskId,
//            createdTime = System.currentTimeMillis(),
//            label = "Instance #$instanceCount"
//        )
//        instances.add(instance)
//        return instance
//    }
//
//    fun getInstanceCount(): Int = instanceCount
//
//    fun destroyInstance() {
//        if (instanceCount > 0) {
//            instanceCount--
//        }
//    }
//
//    fun getAllInstances(): List<ActivityInstance> = instances.toList()
//
//    fun clearAllInstances() {
//        instances.clear()
//        instanceCount = 0
//    }
//
//    fun createNavigationEvent(mode: String, taskId: Int): NavigationEvent {
//        return when (mode) {
//            "Standard" -> {
//                val instance = createInstance(taskId)
//                NavigationEvent.OpenStandardActivity(
//                    taskId = instance.taskId,
//                    instanceCount = instanceCount
//                )
//            }
//
//            "SingleTop" -> NavigationEvent.OpenSingleTopActivity
//            "SingleTask" -> NavigationEvent.OpenSingleTaskActivity
//            "SingleInstance" -> NavigationEvent.OpenSingleInstanceActivity
//            else -> NavigationEvent.OpenStandardActivity(taskId, instanceCount)
//        }
//    }
//
//    fun validateEditText(content: String): Result<String> {
//        return when {
//            content.isBlank() -> Result.failure(
//                IllegalArgumentException("Content cannot be empty")
//            )
//
//            content.length > 500 -> Result.failure(
//                IllegalArgumentException("Content too long (max 500 chars)")
//            )
//
//            else -> Result.success(content.trim())
//        }
//    }
//}