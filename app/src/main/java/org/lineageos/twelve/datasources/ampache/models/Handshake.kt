/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Handshake.
 *
 * @param auth The authentication token
 * @param api The API version
 * @param sessionExpire The session expiration date
 * @param update The last update date
 * @param add The last add date
 * @param clean The last clean date
 * @param songs The number of songs
 * @param albums The number of albums
 * @param artists The number of artists
 * @param genres The number of genres
 * @param playlists The number of playlists
 * @param searches The number of searches
 * @param playlistsSearches The number of playlists searches
 * @param users The number of users
 * @param catalogs The number of catalogs
 * @param videos The number of videos
 * @param podcasts The number of podcasts
 * @param podcastEpisodes The number of podcast episodes
 * @param shares The number of shares
 * @param licenses The number of licenses
 * @param liveStreams The number of live streams
 * @param labels The number of labels
 */
@Serializable
data class Handshake(
    @SerialName("auth") val auth: String,
    @SerialName("streamtoken") val streamToken: String? = null,
    @SerialName("api") val api: String,
    @SerialName("session_expire") val sessionExpire: InstantAsIso8061String,
    @SerialName("update") val update: InstantAsIso8061String,
    @SerialName("add") val add: InstantAsIso8061String,
    @SerialName("clean") val clean: InstantAsIso8061String,
    @SerialName("max_song") val maxSong: Int?,
    @SerialName("max_album") val maxAlbum: Int?,
    @SerialName("max_artist") val maxArtist: Int?,
    @SerialName("max_video") val maxVideo: Int?,
    @SerialName("max_podcast") val maxPodcast: Int?,
    @SerialName("max_podcast_episode") val maxPodcastEpisode: Int?,
    @SerialName("songs") val songs: Int,
    @SerialName("albums") val albums: Int,
    @SerialName("artists") val artists: Int,
    @SerialName("genres") val genres: Int,
    @SerialName("playlists") val playlists: Int,
    @SerialName("searches") val searches: Int,
    @SerialName("playlists_searches") val playlistsSearches: Int,
    @SerialName("users") val users: Int? = null,
    @SerialName("catalogs") val catalogs: Int,
    @SerialName("videos") val videos: Int,
    @SerialName("podcasts") val podcasts: Int,
    @SerialName("podcast_episodes") val podcastEpisodes: Int,
    @SerialName("shares") val shares: Int,
    @SerialName("licenses") val licenses: Int,
    @SerialName("live_streams") val liveStreams: Int,
    @SerialName("labels") val labels: Int,
    @SerialName("username") val username: String,
)
