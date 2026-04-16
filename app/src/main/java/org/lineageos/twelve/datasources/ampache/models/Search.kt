/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Search.
 *
 * @param search The search information
 */
@Serializable
data class Search(
    @SerialName("search") val search: Information,
) {
    /**
     * Information.
     *
     * @param albumArtist The list of album artists
     * @param album The list of albums
     * @param artist The list of artists
     * @param genre The list of genres
     * @param playlist The list of playlists
     * @param songArtist The list of song artists
     * @param song The list of songs
     * @param user The list of users
     */
    @Serializable
    data class Information(
        @SerialName("album_artist") val albumArtist: List<Artist>? = null,
        @SerialName("album") val album: List<Album>? = null,
        @SerialName("artist") val artist: List<Artist>? = null,
        @SerialName("genre") val genre: List<Genre>? = null,
        @SerialName("playlist") val playlist: List<Playlist>? = null,
        @SerialName("song_artist") val songArtist: List<Artist>? = null,
        @SerialName("song") val song: List<Song>? = null,
        @SerialName("user") val user: List<User>? = null,
    )
}
