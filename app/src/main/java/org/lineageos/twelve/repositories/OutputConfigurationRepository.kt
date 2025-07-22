/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lineageos.twelve.models.OutputConfiguration
import org.lineageos.twelve.utils.OutputConfigurationUtils.toModel

/**
 * Repository holding current output configuration info.
 */
class OutputConfigurationRepository {
    private val _audioFormat = MutableStateFlow<AudioFormat?>(null)
    val audioFormat = _audioFormat.asStateFlow()

    private val _device = MutableStateFlow<OutputConfiguration.Device?>(null)
    val device = _device.asStateFlow()

    fun updateAudioFormat(format: AudioFormat?) {
        _audioFormat.value = format
    }

    fun updateAudioDeviceInfo(audioDeviceInfo: AudioDeviceInfo?) {
        _device.value = audioDeviceInfo?.toModel()
    }
}
