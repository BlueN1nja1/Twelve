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
import java.time.Instant

/**
 * A table representing the favorite user items.
 *
 * @param audioUri The [Uri] of the audio
 * @param addedAt The date and time of when this item was added to the favorites
 */
@Entity(
    indices = [
        Index(value = ["audio_uri"], unique = true),
    ],
)
data class Favorite(
    @PrimaryKey @ColumnInfo(name = "audio_uri", defaultValue = "") val audioUri: Uri,
    @ColumnInfo(name = "added_at") val addedAt: Instant,
)
