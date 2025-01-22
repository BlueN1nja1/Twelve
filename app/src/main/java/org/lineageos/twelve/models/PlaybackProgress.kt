/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Playback progress.
 *
 * @param isPlaying Whether playback is in progress
 * @param durationMs The full duration of the played content
 * @param currentPositionMs The current position in milliseconds. Note that a new value shouldn't be
 *   emitted because of this value being changed since the current position will be handled by UI
 * @param playbackSpeed The current playback speed
 */
data class PlaybackProgress(
    val isPlaying: Boolean,
    val durationMs: Long?,
    val currentPositionMs: Long?,
    val playbackSpeed: Float,
) {
    companion object {
        val EMPTY = PlaybackProgress(false, null, null, 1f)
    }
}
