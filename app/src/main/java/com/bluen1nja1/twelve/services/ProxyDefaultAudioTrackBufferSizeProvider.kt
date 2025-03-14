/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.services

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@androidx.annotation.OptIn(UnstableApi::class)
object ProxyDefaultAudioTrackBufferSizeProvider : DefaultAudioSink.AudioTrackBufferSizeProvider {
    private val delegate = DefaultAudioSink.AudioTrackBufferSizeProvider.DEFAULT

    private val _encodingFlow = MutableStateFlow<@C.Encoding Int?>(null)
    val encodingFlow = _encodingFlow.asStateFlow()

    private val _outputModeFlow = MutableStateFlow<@DefaultAudioSink.OutputMode Int?>(null)
    val outputModeFlow = _outputModeFlow.asStateFlow()

    private val _bitrateFlow = MutableStateFlow<Int?>(null)
    val bitrateFlow = _bitrateFlow.asStateFlow()

    override fun getBufferSizeInBytes(
        minBufferSizeInBytes: Int,
        encoding: @C.Encoding Int,
        outputMode: @DefaultAudioSink.OutputMode Int,
        pcmFrameSize: Int,
        sampleRate: Int,
        bitrate: Int,
        maxAudioTrackPlaybackSpeed: Double
    ) = delegate.getBufferSizeInBytes(
        minBufferSizeInBytes,
        encoding,
        outputMode,
        pcmFrameSize,
        sampleRate,
        bitrate,
        maxAudioTrackPlaybackSpeed
    ).also {
        _encodingFlow.value = encoding
        _outputModeFlow.value = outputMode
        _bitrateFlow.value = bitrate.takeIf { it != Format.NO_VALUE }
    }
}
