/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.media.AudioTrack
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import java.nio.ByteBuffer

/**
 * An [AudioSink] implementation that wraps a [DefaultAudioSink] and exposes the [AudioTrack] used
 * by the [DefaultAudioSink].
 */
@androidx.media3.common.util.UnstableApi
class TwelveAudioSink(
    private val defaultAudioSink: DefaultAudioSink,
    private val onAudioTrackUpdate: (AudioTrack?) -> Unit,
) : AudioSink by defaultAudioSink {
    private val audioTrackField = DefaultAudioSink::class.java.getDeclaredField(
        "audioTrack"
    ).apply {
        isAccessible = true
    }

    override fun handleBuffer(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        encodedAccessUnitCount: Int,
    ) = try {
        defaultAudioSink.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
    } finally {
        onAudioTrackUpdate(audioTrackField.get(defaultAudioSink) as? AudioTrack)
    }

    override fun flush() = try {
        defaultAudioSink.flush()
    } finally {
        onAudioTrackUpdate(null)
    }
}
