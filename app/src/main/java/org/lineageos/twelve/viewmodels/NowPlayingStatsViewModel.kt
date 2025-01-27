/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.models.AudioOutputMode
import org.lineageos.twelve.models.AudioStreamInformation
import org.lineageos.twelve.models.Encoding
import org.lineageos.twelve.services.InfoAudioProcessor
import org.lineageos.twelve.services.ProxyDefaultAudioTrackBufferSizeProvider

class NowPlayingStatsViewModel(application: Application) : NowPlayingViewModel(application) {
    /**
     * [AudioStreamInformation] parsed from the currently selected audio track returned by the
     * player.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    val sourceAudioStreamInformation = currentTrackFormat
        .mapLatest { currentTrackFormat ->
            currentTrackFormat?.let {
                AudioStreamInformation(
                    it.sampleRate,
                    it.channelCount,
                    it.sampleMimeType?.let { sampleMimeType ->
                        Encoding.fromMedia3Encoding(
                            MimeTypes.getEncoding(
                                MimeTypes.normalizeMimeType(sampleMimeType),
                                it.codecs
                            )
                        )
                    } ?: Encoding.fromMedia3Encoding(it.pcmEncoding),
                )
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * Whether the output is in non-passthrough PCM float mode.
     * This means the audio sink is ignoring all the processors.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val transcodingFloatModeEnabled = combine(
        ProxyDefaultAudioTrackBufferSizeProvider.encodingFlow,
        ProxyDefaultAudioTrackBufferSizeProvider.outputModeFlow,
    ) { encoding, outputMode ->
        outputMode == DefaultAudioSink.OUTPUT_MODE_PCM && encoding == C.ENCODING_PCM_FLOAT
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val transcodingEncoding = ProxyDefaultAudioTrackBufferSizeProvider.encodingFlow
        .mapLatest {
            it?.let { Encoding.fromMedia3Encoding(it) }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val transcodingOutputMode = ProxyDefaultAudioTrackBufferSizeProvider.outputModeFlow
        .mapLatest {
            it?.let { AudioOutputMode.fromMedia3OutputMode(it) }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val transcodingBitrate = ProxyDefaultAudioTrackBufferSizeProvider.bitrateFlow

    /**
     * Whether the output has valid information.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val hasOutputInformation = combine(
        ProxyDefaultAudioTrackBufferSizeProvider.outputModeFlow,
        transcodingFloatModeEnabled,
    ) { outputMode, transcodingFloatModeEnabled ->
        outputMode == DefaultAudioSink.OUTPUT_MODE_PCM && transcodingFloatModeEnabled != true
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * The output audio stream information.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val outputAudioStreamInformation = combine(
        InfoAudioProcessor.audioFormatFlow,
        hasOutputInformation,
    ) { audioFormat, hasOutputInformation ->
        audioFormat?.takeIf { hasOutputInformation != false }?.let {
            AudioStreamInformation(
                it.sampleRate,
                it.channelCount,
                Encoding.fromMedia3Encoding(it.encoding),
            )
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
}
