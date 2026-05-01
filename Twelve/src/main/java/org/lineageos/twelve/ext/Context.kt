/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun Context.permissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.permissionsGranted(permissions: Array<String>) = permissions.all {
    permissionGranted(it)
}

fun Context.permissionsStatus(permissions: Array<String>) = permissions.partition {
    permissionGranted(it)
}

/**
 * Flow of permissions granted/denied.
 */
fun Context.permissionsFlow(lifecycle: Lifecycle, permissions: Array<String>) =
    lifecycle.eventFlow(Lifecycle.Event.ON_RESUME)
        .onStart { emit(Unit) }
        .map {
            permissionsStatus(permissions)
        }
