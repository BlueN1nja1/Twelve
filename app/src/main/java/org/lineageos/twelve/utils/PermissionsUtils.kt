/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.Manifest
import android.os.Build

/**
 * App's permissions utils.
 */
object PermissionsUtils {
    /**
     * Permissions required to run the app
     */
    val mainPermissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    val visualizerPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
    )
}
