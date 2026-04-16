/*
 * SPDX-FileCopyrightText: 2025-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.media.AudioDeviceInfo
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lineageos.twelve.models.OutputConfiguration
import org.lineageos.twelve.utils.OutputConfigurationUtils.toModel

/**
 * Repository holding current output configuration info.
 */
class OutputConfigurationRepository {
    private val _format = MutableStateFlow<Format?>(null)
    val format = _format.asStateFlow()

    private val _device = MutableStateFlow<OutputConfiguration.Device?>(null)
    val device = _device.asStateFlow()

    private val _audioTrackConfig = MutableStateFlow<AudioSink.AudioTrackConfig?>(null)
    val audioTrackConfig = _audioTrackConfig.asStateFlow()

    fun updateFormat(format: Format?) {
        _format.value = format
    }

    fun updateAudioDeviceInfo(audioDeviceInfo: AudioDeviceInfo?) {
        _device.value = audioDeviceInfo?.toModel()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    fun updateAudioTrackConfig(audioTrackConfig: AudioSink.AudioTrackConfig?) {
        _audioTrackConfig.value = audioTrackConfig
    }
}
