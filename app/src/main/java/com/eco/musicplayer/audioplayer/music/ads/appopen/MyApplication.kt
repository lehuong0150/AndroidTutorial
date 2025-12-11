package com.eco.musicplayer.audioplayer.music.ads.appopen

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null
    private var isFromBackground = false
    private var isFirstLaunch = true

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        appOpenAdManager = AppOpenAdManager(this)
//        appOpenAdManager.loadAd()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity == activity) currentActivity = null
            }

            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                if (isFirstLaunch && activity.javaClass.simpleName == "MainRewardAdActivity") {
                    android.util.Log.d("MyApplication", "MainActivity created - scheduling ad show")
                    isFirstLaunch = false

                    activity.window.decorView.post {
                        android.util.Log.d("MyApplication", "Showing first launch ad")
                        appOpenAdManager.showAdIfAvailable(activity)
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {
                if (!appOpenAdManager.isShowingAd) {
                    currentActivity = activity
                }
            }

            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
        })

        //để show ad khi app vào foreground
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
//                currentActivity?.let {
//                    appOpenAdManager.showAdIfAvailable(it)
//                }
                if (isFromBackground) {
                    currentActivity?.let {
                        appOpenAdManager.showAdIfAvailable(it)
                    }
                }
                isFromBackground = true
            }
        })
    }

    fun getAppOpenAdManager(): AppOpenAdManager {
        return appOpenAdManager
    }
}

