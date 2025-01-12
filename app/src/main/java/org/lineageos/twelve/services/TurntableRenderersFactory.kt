/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
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
class TurntableRenderersFactory(
    context: Context,
    enableAudioFloatOutput: Boolean,
) : DefaultRenderersFactory(context) {
    init {
        setEnableAudioFloatOutput(enableAudioFloatOutput)
        setEnableAudioTrackPlaybackParams(true)
        setEnableDecoderFallback(true)
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
