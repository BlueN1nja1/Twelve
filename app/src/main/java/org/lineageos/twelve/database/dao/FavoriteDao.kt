/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.database.entities.Item
import java.time.Instant

@Dao
@Suppress("FunctionName")
abstract class FavoriteDao(database: TwelveDatabase) {
    private val itemDao = database.getItemDao()

    /**
     * Get all the favorite items.
     */
    @Query(
        """
            SELECT item.*
            FROM favorite
                LEFT JOIN item ON item.item_id = favorite.item_id
        """
    )
    abstract fun getAll(): Flow<List<Item>>

    /**
     * Check whether this item is a favorite.
     */
    @Query(
        """
            SELECT favorite.item_id
            FROM favorite
                LEFT JOIN Item ON item.item_id = favorite.item_id
            WHERE audio_uri = :audioUri
        """
    )
    abstract suspend fun _contains(audioUri: Uri): Long?

    /**
     * Check whether this item is a favorite.
     */
    suspend fun contains(audioUri: Uri): Boolean = _contains(audioUri) != null

    /**
     * Check whether this item is a favorite.
     */
    @Query(
        """
            SELECT favorite.item_id
            FROM favorite
                LEFT JOIN Item ON item.item_id = favorite.item_id
            WHERE audio_uri = :audioUri
        """
    )
    abstract fun _containsFlow(audioUri: Uri): Flow<Long?>

    /**
     * Check whether this item is a favorite.
     */
    fun containsFlow(audioUri: Uri): Flow<Boolean> = _containsFlow(audioUri).map { it != null }

    /**
     * Add this item to favorites.
     */
    @Transaction
    open suspend fun add(audioUri: Uri, addedAt: Instant = Instant.now()) {
        val item = itemDao.getOrInsert(audioUri)
        _add(item.id, addedAt)
    }

    /**
     * Remove this item from favorites.
     */
    @Query(
        """
            DELETE
            FROM favorite
            WHERE favorite.item_id IN (
                SELECT item.item_id
                FROM favorite
                    LEFT JOIN item ON item.item_id = favorite.item_id
                WHERE audio_uri = :audioUri
            )
        """
    )
    abstract suspend fun remove(audioUri: Uri)

    @Query(
        """
            INSERT INTO favorite (item_id, added_at)
            VALUES (:itemId, :addedAt)
        """
    )
    abstract suspend fun _add(itemId: Long, addedAt: Instant)
}
