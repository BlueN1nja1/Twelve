/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * An item representing a playlist with a boolean value representing whether or not the requested
 * audio is in the playlist.
 *
 * @param playlist The playlist
 * @param value Whether or not the requested audio is in the playlist
 */
data class PlaylistWithBoolean(
    @Embedded val playlist: Playlist,
    @ColumnInfo(name = "value") val value: Boolean
)
