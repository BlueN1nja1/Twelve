/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.models.ResumptionPlaylist

/**
 * Manages the playlist used when the user wants to resume playback from the last queue.
 */
class ResumptionPlaylistRepository(val database: TwelveDatabase) {
    /**
     * Get the last resumption playlist or an empty one.
     */
    suspend fun getResumptionPlaylist() =
        database.getResumptionPlaylistDao().getResumptionPlaylistWithItems()?.let {
            ResumptionPlaylist(
                it.items.sortedBy { item ->
                    item.playlistIndex
                }.map { item ->
                    item.mediaId
                },
                it.resumptionPlaylist.startIndex,
                it.resumptionPlaylist.startPositionMs,
            )
        } ?: ResumptionPlaylist(emptyList())

    /**
     * Clear the resumption playlist.
     */
    suspend fun clearResumptionPlaylist() =
        database.getResumptionPlaylistDao().clearResumptionPlaylist()

    /**
     * When the user changes the queue, create a new resumption playlist.
     *
     * @param mediaIds The list of audio media item IDs
     * @param startIndex The start index
     * @param startPositionMs The playback position in milliseconds
     */
    suspend fun onMediaItemsChanged(
        mediaIds: List<String>,
        startIndex: Int,
        startPositionMs: Long,
    ) = database.getResumptionPlaylistDao().createResumptionPlaylist(
        startIndex,
        startPositionMs,
        mediaIds,
    )

    suspend fun onPlaybackPositionChanged(
        startIndex: Int,
        startPositionMs: Long,
    ) = database.getResumptionPlaylistDao().updateResumptionPlaylist(
        startIndex,
        startPositionMs,
    )
}
