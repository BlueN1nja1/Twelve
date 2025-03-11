/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
@Suppress("FunctionName")
interface PlaylistItemCrossRefDao {
    /**
     * Add an item to a playlist (creates a cross-reference).
     */
    @Transaction
    @Query(
        """
            INSERT INTO PlaylistItemCrossRef (playlist_id, audio_uri, last_modified)
            VALUES (:playlistId, :audioUri, :lastModified)
        """
    )
    suspend fun _addItemToPlaylist(
        playlistId: Long,
        audioUri: Uri,
        lastModified: Long = System.currentTimeMillis()
    )

    /**
     * Remove an item from a playlist (deletes the cross-reference).
     */
    @Query("DELETE FROM PlaylistItemCrossRef WHERE playlist_id = :playlistId AND audio_uri = :audioUri")
    suspend fun _removeItemFromPlaylist(playlistId: Long, audioUri: Uri)
}
