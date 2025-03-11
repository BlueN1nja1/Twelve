/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Playlist entity.
 *
 * @param id The playlist ID
 * @param name The playlist name
 * @param createdAt The creation date of the playlist
 */
@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "playlist_id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
