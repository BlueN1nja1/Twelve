/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Stats.
 *
 * @param totalCount The total count
 * @param md5 The md5
 * @param album The albums
 * @param artist The artists
 * @param playlist The playlists
 * @param song The songs
 */
@Serializable
data class Stats(
    @SerialName("total_count") val totalCount: Int? = null,
    @SerialName("md5") val md5: String? = null,
    @SerialName("album") val album: List<Album>? = null,
    @SerialName("artist") val artist: List<Artist>? = null,
    @SerialName("playlist") val playlist: List<Playlist>? = null,
    @SerialName("song") val song: List<Song>? = null,
)
