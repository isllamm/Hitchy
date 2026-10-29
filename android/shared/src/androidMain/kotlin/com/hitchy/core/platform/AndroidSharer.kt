package com.hitchy.core.platform

import android.content.Context
import android.content.Intent

class AndroidSharer(
    private val context: Context,
    private val activityProvider: ActivityProvider? = null,
) : Sharer {

    override fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooserIntent = Intent.createChooser(sendIntent, null)
        val currentActivity = activityProvider?.currentActivity
        if (currentActivity != null) {
            currentActivity.startActivity(chooserIntent)
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        }
    }
}
