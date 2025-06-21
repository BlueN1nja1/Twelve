/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink

@OptIn(UnstableApi::class)
class TwelveRenderersFactory(
    context: Context,
    enableAudioFloatOutput: Boolean,
) : DefaultRenderersFactory(context) {
    init {
        setEnableAudioFloatOutput(enableAudioFloatOutput)
        setEnableAudioTrackPlaybackParams(true)
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ) = DefaultAudioSink.Builder(context)
        .setEnableFloatOutput(enableFloatOutput)
        .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
        .setAudioProcessors(arrayOf(InfoAudioProcessor()))
        .setAudioTrackBufferSizeProvider(ProxyDefaultAudioTrackBufferSizeProvider)
        .setAudioOffloadSupportProvider(DefaultAudioOffloadSupportProvider(context))
        .build()
}
