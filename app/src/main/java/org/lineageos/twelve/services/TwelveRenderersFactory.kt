/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.content.Context
import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.DefaultMediaCodecAdapterFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.metadata.MetadataRenderer
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.video.VideoRendererEventListener

@OptIn(UnstableApi::class)
class TwelveRenderersFactory(
    private val context: Context,
    private val enableAudioFloatOutput: Boolean,
) : RenderersFactory {
    override fun createRenderers(
        eventHandler: Handler,
        videoRendererEventListener: VideoRendererEventListener,
        audioRendererEventListener: AudioRendererEventListener,
        textRendererOutput: TextOutput,
        metadataRendererOutput: MetadataOutput
    ) = arrayOf<Renderer>(
        MediaCodecAudioRenderer(
            context,
            DefaultMediaCodecAdapterFactory(context),
            MediaCodecSelector.DEFAULT,
            true,
            eventHandler,
            audioRendererEventListener,
            DefaultAudioSink.Builder(context)
                .setEnableFloatOutput(enableAudioFloatOutput)
                .setEnableAudioTrackPlaybackParams(true)
                .setAudioProcessors(arrayOf(InfoAudioProcessor()))
                .setAudioTrackBufferSizeProvider(ProxyDefaultAudioTrackBufferSizeProvider)
                .setAudioOffloadSupportProvider(DefaultAudioOffloadSupportProvider(context))
                .build()

        ),
        MetadataRenderer(metadataRendererOutput, eventHandler.looper),
    )
}
