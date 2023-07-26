package ru.lord.firebase_chat

import android.app.Activity
import android.content.Intent

fun Activity.launchActivityAndFinish(activityClass: Class<*>) = startActivity(
    Intent(this, activityClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
).apply {
    finish()
}
