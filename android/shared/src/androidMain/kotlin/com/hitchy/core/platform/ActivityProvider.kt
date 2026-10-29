package com.hitchy.core.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

interface ActivityProvider {
    val currentActivity: Activity?
}

class AndroidActivityProvider :
    ActivityProvider,
    Application.ActivityLifecycleCallbacks {
    private var activityRef: WeakReference<Activity>? = null

    override val currentActivity: Activity?
        get() = activityRef?.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef?.clear()
            activityRef = null
        }
    }
}
