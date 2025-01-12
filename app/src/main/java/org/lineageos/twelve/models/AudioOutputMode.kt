/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * Audio output mode.
 */
@androidx.annotation.OptIn(UnstableApi::class)
enum class AudioOutputMode(val media3OutputMode: @DefaultAudioSink.OutputMode Int) {
    /**
     * The audio sink plays PCM audio.
     */
    PCM(DefaultAudioSink.OUTPUT_MODE_PCM),

    /**
     * The audio sink plays encoded audio in offload.
     */
    OFFLOAD(DefaultAudioSink.OUTPUT_MODE_OFFLOAD),

    /**
     * The audio sink plays encoded audio in passthrough.
     */
    PASSTHROUGH(DefaultAudioSink.OUTPUT_MODE_PASSTHROUGH);

    companion object {
        fun fromMedia3OutputMode(
            media3OutputMode: @DefaultAudioSink.OutputMode Int,
        ) = entries.firstOrNull {
            it.media3OutputMode == media3OutputMode
        } ?: throw Exception("Unknown output mode: $media3OutputMode")
    }
}
