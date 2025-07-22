/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.media.AudioDeviceInfo
import android.media.AudioRouting
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * @see AudioRouting.getRoutedDevice
 */
fun AudioRouting.routedDeviceFlow() = callbackFlow<AudioDeviceInfo?> {
    val callback = AudioRouting.OnRoutingChangedListener {
        trySend(it.routedDevice)
    }

    addOnRoutingChangedListener(callback, Handler(Looper.getMainLooper()))
    trySend(routedDevice)

    awaitClose {
        removeOnRoutingChangedListener(callback)
    }
}
