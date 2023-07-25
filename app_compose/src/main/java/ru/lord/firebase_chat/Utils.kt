package ru.lord.firebase_chat

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.DrawableRes

fun Resources.getUriById(@DrawableRes resourceId: Int): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(getResourcePackageName(resourceId))
    .appendPath(getResourceTypeName(resourceId))
    .appendPath(getResourceEntryName(resourceId))
    .build()
