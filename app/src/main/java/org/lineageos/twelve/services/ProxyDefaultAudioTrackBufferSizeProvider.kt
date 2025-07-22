/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

@androidx.annotation.OptIn(UnstableApi::class)
object ProxyDefaultAudioTrackBufferSizeProvider : DefaultAudioSink.AudioTrackBufferSizeProvider {
    private val delegate = DefaultAudioSink.AudioTrackBufferSizeProvider.DEFAULT

    private val encodingStateFlow = MutableStateFlow<@C.Encoding Int?>(null)
    private val outputModeStateFlow = MutableStateFlow<@DefaultAudioSink.OutputMode Int?>(null)
    private val bitrateBpsStateFlow = MutableStateFlow<Int?>(null)

    data class TranscodingData(
        val encoding: @C.Encoding Int?,
        val outputMode: @DefaultAudioSink.OutputMode Int?,
        val bitrateBps: Int?,
    )

    val transcodingData = combine(
        encodingStateFlow,
        outputModeStateFlow,
        bitrateBpsStateFlow,
    ) { encoding, outputMode, bitrateBps ->
        TranscodingData(
            encoding = encoding,
            outputMode = outputMode,
            bitrateBps = bitrateBps,
        )
    }

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
        encodingStateFlow.value = encoding
        outputModeStateFlow.value = outputMode
        bitrateBpsStateFlow.value = bitrate
    }
}
