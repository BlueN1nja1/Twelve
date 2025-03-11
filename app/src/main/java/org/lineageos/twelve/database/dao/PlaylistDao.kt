/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.Playlist
import org.lineageos.twelve.database.entities.PlaylistWithBoolean
import org.lineageos.twelve.database.entities.PlaylistWithItems

@Dao
@Suppress("FunctionName")
interface PlaylistDao {
    /**
     * Create a new playlist.
     */
    @Query("INSERT INTO Playlist (name, created_at) VALUES (:name, :createdAt)")
    suspend fun create(name: String, createdAt: Long = System.currentTimeMillis()): Long

    /**
     * Rename a playlist.
     */
    @Query("UPDATE Playlist SET name = :name WHERE playlist_id = :playlistId")
    suspend fun rename(playlistId: Long, name: String)

    /**
     * Delete a playlist.
     */
    @Query("DELETE FROM Playlist WHERE playlist_id = :playlistId")
    suspend fun delete(playlistId: Long)

    /**
     * Fetch all playlists.
     */
    @Query("SELECT * FROM Playlist")
    fun getAll(): Flow<List<Playlist>>

    /**
     * Fetch a playlist by its ID.
     */
    @Query("SELECT * FROM Playlist WHERE playlist_id = :playlistId")
    fun getById(playlistId: Long): Flow<Playlist?>

    /**
     * Fetch a playlist with its associated items.
     */
    @Query("SELECT * FROM Playlist WHERE playlist_id = :playlistId")
    @Transaction
    fun getPlaylistWithItems(playlistId: Long): Flow<PlaylistWithItems?>

    @Query(
        """
            SELECT Playlist.*,
                   (CASE WHEN PlaylistItemCrossRef.audio_uri IS NOT NULL THEN 1 ELSE 0 END) AS value
            FROM Playlist
            LEFT JOIN PlaylistItemCrossRef ON
                    Playlist.playlist_id = PlaylistItemCrossRef.playlist_id
                    AND PlaylistItemCrossRef.audio_uri = :audioUri
        """
    )
    fun _getPlaylistsWithItemStatus(audioUri: Uri): Flow<List<PlaylistWithBoolean>>
}
