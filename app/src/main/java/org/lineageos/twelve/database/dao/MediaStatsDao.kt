/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.LocalMediaStats

@Dao
interface MediaStatsDao {

    /**
     * Delete an entry.
     */
    @Query("DELETE FROM LocalMediaStats WHERE media_uri IN (:mediaUris)")
    suspend fun delete(mediaUris: List<Uri>)

    /**
     * Delete all entries.
     */
    @Query("DELETE FROM LocalMediaStats")
    suspend fun deleteAll()

    /**
     * Increase the play count of an entry by 1.
     */
    @Query("INSERT OR REPLACE INTO LocalMediaStats (media_uri, play_count) VALUES (:mediaUri, COALESCE((SELECT play_count + 1 FROM LocalMediaStats WHERE media_uri = :mediaUri), 1))")
    suspend fun increasePlayCount(mediaUri: Uri)

    /**
     * Set favorite status of an entry.
     */
    @Query("INSERT OR REPLACE INTO LocalMediaStats (media_uri, favorite) VALUES (:mediaUri, :isFavorite)")
    suspend fun setFavorite(mediaUri: Uri, isFavorite: Boolean)

    /**
     * Fetch all entries.
     */
    @Query("SELECT * FROM LocalMediaStats")
    suspend fun getAll(): List<LocalMediaStats>

    /**
     * Fetch all entries sorted by play count.
     */
    @Query("SELECT * FROM LocalMediaStats ORDER BY play_count DESC LIMIT :limit")
    fun getAllByPlayCount(limit: Int): Flow<List<LocalMediaStats>>

    /**
     * Fetch whether the given entry is marked as favorite.
     */
    @Query("SELECT favorite FROM LocalMediaStats WHERE media_uri = :mediaUri")
    fun isFavorite(mediaUri: Uri): Flow<Boolean>
}
