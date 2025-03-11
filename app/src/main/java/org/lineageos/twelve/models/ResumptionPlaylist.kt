/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * A resumption playlist.
 *
 * @param mediaItemIds The list of audio media item IDs
 * @param startIndex The start index, in the range [0..mediaItemIds.size)
 * @param startPositionMs The playback position in milliseconds
 */
data class ResumptionPlaylist(
    val mediaItemIds: List<String>,
    val startIndex: Int = 0,
    val startPositionMs: Long = 0L,
) {
    init {
        require(mediaItemIds.isEmpty() || startIndex in mediaItemIds.indices) {
            "Invalid start index"
        }
    }
}
