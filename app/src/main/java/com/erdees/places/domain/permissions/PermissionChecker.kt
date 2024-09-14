package com.erdees.places.domain.permissions

import android.content.Context
import androidx.core.content.ContextCompat


/**
 * Interface for checking permissions. This abstraction enables easier testing of the permission.
 */
interface PermissionChecker {
    fun checkSelfPermission(context: Context, name: String): Int
}

class PermissionCheckerImpl : PermissionChecker {
    override fun checkSelfPermission(context: Context, name: String): Int {
        return ContextCompat.checkSelfPermission(context, name)
    }
}