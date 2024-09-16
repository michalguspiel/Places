package com.erdees.places.util

import com.erdees.places.BuildConfig

object BuildUtil {
    fun getBuildVersion(): Int {
        return android.os.Build.VERSION.SDK_INT
    }

    fun getClientID(): String {
        return BuildConfig.CLIENT_ID
    }

    fun getClientSecret(): String {
        return BuildConfig.CLIENT_SECRET
    }
}