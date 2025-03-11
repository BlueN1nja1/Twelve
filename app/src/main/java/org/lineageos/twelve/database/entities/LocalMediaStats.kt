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

/**
 * Database entity for local media stats
 *
 * @param audioUri The [Uri] of the audio
 * @param playCount The number of times the media has been played
 */
@Entity(
    indices = [
        Index(value = ["audio_uri"], unique = true),
    ],
)
data class LocalMediaStats(
    @PrimaryKey @ColumnInfo(name = "audio_uri") val audioUri: Uri,
    @ColumnInfo(name = "play_count", defaultValue = "1") val playCount: Long,
)
