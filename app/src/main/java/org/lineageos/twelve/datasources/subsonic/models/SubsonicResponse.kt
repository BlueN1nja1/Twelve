/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

typealias TODO = (Nothing?)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class SubsonicResponse(
    val musicFolders: TODO = null,
    val indexes: TODO = null,
    val directory: TODO = null,
    val genres: Genres? = null,
    val artists: ArtistsID3? = null,
    val artist: ArtistWithAlbumsID3? = null,
    val album: AlbumWithSongsID3? = null,
    val song: Child? = null,
    val videos: TODO = null,
    val videoInfo: TODO = null,
    val nowPlaying: TODO = null,
    val searchResult: TODO = null,
    val searchResult2: TODO = null,
    val searchResult3: SearchResult3? = null,
    val playlists: Playlists? = null,
    val playlist: PlaylistWithSongs? = null,
    val jukeboxStatus: TODO = null,
    val jukeboxPlaylist: TODO = null,
    val license: License? = null,
    val users: TODO = null,
    val user: TODO = null,
    val chatMessages: TODO = null,
    val albumList: AlbumList? = null,
    val albumList2: AlbumList2? = null,
    val randomSongs: Songs? = null,
    val songsByGenre: Songs? = null,
    val lyrics: TODO = null,
    val podcasts: TODO = null,
    val newestPodcasts: TODO = null,
    val internetRadioStations: TODO = null,
    val bookmarks: TODO = null,
    val playQueue: TODO = null,
    val shares: TODO = null,
    val starred: TODO = null,
    val starred2: TODO = null,
    val albumInfo: TODO = null,
    val artistInfo: TODO = null,
    val artistInfo2: TODO = null,
    val similarSongs: TODO = null,
    val similarSongs2: TODO = null,
    val topSongs: TODO = null,
    val scanStatus: TODO = null,
    val error: Error? = null,

    val status: ResponseStatus,
    val version: Version,

    // OpenSubsonic
    val type: String? = null,
    val serverVersion: String? = null,
    val openSubsonic: Boolean? = null,
)
