/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["play_count"]),
    ],
)
data class LocalMediaStats(
    @PrimaryKey @ColumnInfo(name = "media_uri") val mediaUri: Uri,
    @ColumnInfo(name = "play_count", defaultValue = "1") val playCount: Long,
    @ColumnInfo(name = "favorite", defaultValue = "false") val favorite: Boolean,
)
