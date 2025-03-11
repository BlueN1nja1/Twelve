/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

@Dao
@Suppress("FunctionName")
interface FavoriteDao {
    /**
     * Get all the favorite items.
     */
    @Query(
        """
            SELECT audio_uri
            FROM favorite
        """
    )
    fun getAll(): Flow<List<Uri>>

    /**
     * Check whether this item is a favorite.
     */
    @Query(
        """
            SELECT audio_uri
            FROM favorite
            WHERE audio_uri = :audioUri
        """
    )
    suspend fun _contains(audioUri: Uri): Uri?

    /**
     * Check whether this item is a favorite.
     */
    suspend fun contains(audioUri: Uri): Boolean = _contains(audioUri) != null

    /**
     * Check whether this item is a favorite.
     */
    @Query(
        """
            SELECT audio_uri
            FROM favorite
            WHERE audio_uri = :audioUri
        """
    )
    fun _containsFlow(audioUri: Uri): Flow<Uri?>

    /**
     * Check whether this item is a favorite.
     */
    fun containsFlow(audioUri: Uri): Flow<Boolean> = _containsFlow(audioUri).map { it != null }

    /**
     * Add this item to favorites.
     */
    @Query(
        """
            INSERT INTO favorite (audio_uri, added_at)
            VALUES (:audioUri, :addedAt)
        """
    )
    suspend fun add(audioUri: Uri, addedAt: Instant = Instant.now())

    /**
     * Remove this item from favorites.
     */
    @Query(
        """
            DELETE
            FROM favorite
            WHERE audio_uri = :audioUri
        """
    )
    suspend fun remove(audioUri: Uri)
}
