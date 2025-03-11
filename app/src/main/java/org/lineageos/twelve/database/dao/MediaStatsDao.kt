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
    @Query("DELETE FROM LocalMediaStats WHERE audio_uri IN (:mediaUris)")
    suspend fun delete(mediaUris: List<Uri>)

    /**
     * Delete all entries.
     */
    @Query("DELETE FROM LocalMediaStats")
    suspend fun deleteAll()

    /**
     * Increase the play count of an entry by 1.
     */
    @Query(
        """
            INSERT OR REPLACE
            INTO LocalMediaStats (audio_uri, play_count)
            VALUES (
                :audioUri,
                COALESCE(
                    (SELECT play_count + 1 FROM LocalMediaStats WHERE audio_uri = :audioUri), 1
                )
            )
        """
    )
    suspend fun increasePlayCount(audioUri: Uri)

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
}
