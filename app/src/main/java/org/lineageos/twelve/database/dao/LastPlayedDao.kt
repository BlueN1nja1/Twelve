/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LastPlayedDao {

    @Query("INSERT OR REPLACE INTO LastPlayed (data_source, media_uri) VALUES (:dataSource, :mediaUri)")
    suspend fun set(dataSource: String, mediaUri: Uri): Long

    @Query("SELECT media_uri FROM LASTPLAYED WHERE data_source = :dataSource")
    fun get(dataSource: String): Flow<Uri?>
}
