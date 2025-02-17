/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import org.lineageos.twelve.database.entities.ResumptionPlaylistWithMediaItems

@Dao
@Suppress("FunctionName")
interface ResumptionPlaylistDao {
    /**
     * Clear all resumption playlists.
     */
    @Query("DELETE FROM ResumptionPlaylist")
    suspend fun clearResumptionPlaylist()

    /**
     * Insert a resumption playlist.
     */
    @Query(
        """
            INSERT INTO ResumptionPlaylist (start_index, start_position_ms)
            VALUES (:startIndex, :startPositionMs)
        """
    )
    suspend fun _createResumptionPlaylist(startIndex: Int, startPositionMs: Long): Long

    /**
     * Add an item to a resumption playlist.
     */
    @Query(
        """
            INSERT INTO ResumptionItem (playlist_index, resumption_playlist_id, media_id)
            VALUES (:index, :resumptionPlaylistId, :mediaItem)
        """
    )
    suspend fun _addItemToResumptionPlaylist(
        index: Long,
        resumptionPlaylistId: Long,
        mediaItem: String
    )

    /**
     * Creates a new resumption playlist given a list of media items.
     */
    @Transaction
    suspend fun createResumptionPlaylist(
        startIndex: Int,
        startPositionMs: Long,
        mediaItems: List<String>
    ) {
        clearResumptionPlaylist()

        val id = _createResumptionPlaylist(startIndex, startPositionMs)

        mediaItems.forEachIndexed { index, it ->
            _addItemToResumptionPlaylist(index.toLong(), id, it)
        }
    }

    /**
     * Get resumption playlist with items
     */
    @Transaction
    @Query("SELECT * FROM ResumptionPlaylist LIMIT 1")
    suspend fun getResumptionPlaylistWithItems(): ResumptionPlaylistWithMediaItems?

    /**
     * Update resumption playlist.
     */
    @Query("UPDATE ResumptionPlaylist SET start_index = :currentMediaItemIndex, start_position_ms = :currentPosition")
    suspend fun updateResumptionPlaylist(currentMediaItemIndex: Int, currentPosition: Long): Int
}
