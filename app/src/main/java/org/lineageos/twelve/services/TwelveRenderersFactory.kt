/*
 * SPDX-FileCopyrightText: 2024-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.content.Context
import android.media.AudioDeviceInfo
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.AudioTrackAudioOutputProvider
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink

@OptIn(UnstableApi::class)
class TwelveRenderersFactory(
    context: Context,
    enableAudioFloatOutput: Boolean,
    private val onAudioDeviceInfoChanged: (AudioDeviceInfo?) -> Unit,
    private val onAudioTrackConfigChanged: (AudioSink.AudioTrackConfig?) -> Unit,
) : DefaultRenderersFactory(context) {
    init {
        setEnableAudioFloatOutput(enableAudioFloatOutput)
        setEnableAudioOutputPlaybackParameters(true)
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ) = TwelveAudioSink(
        DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioOutputPlaybackParameters(enableAudioTrackPlaybackParams)
            .setAudioOutputProvider(
                AudioTrackAudioOutputProvider.Builder(context)
                    .setAudioOffloadSupportProvider(DefaultAudioOffloadSupportProvider(context))
                    .build()
            )
            .build(),
        onAudioDeviceInfoChanged,
        onAudioTrackConfigChanged,
    )
}
