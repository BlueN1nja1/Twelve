/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import android.net.Uri
import androidx.room.Embedded
import androidx.room.Relation

/**
 * [Playlist] with item [Uri]s.
 *
 * @param playlist The [Playlist] entity
 * @param items The list of songs
 */
data class PlaylistWithItems(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlist_id",
        entity = PlaylistItemCrossRef::class,
        entityColumn = "playlist_id",
        projection = ["audio_uri"],
    ) val items: List<Uri>,
)
