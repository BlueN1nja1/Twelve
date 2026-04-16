/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ping.
 */
@Serializable
data class Ping(
    @SerialName("session_expire") val sessionExpire: InstantAsIso8061String? = null,
    @SerialName("server") val server: String,
    @SerialName("version") val version: String,
    @SerialName("compatible") val compatible: String,
    @SerialName("auth") val auth: String? = null,
    @SerialName("api") val api: String? = null,
    @SerialName("update") val update: InstantAsIso8061String? = null,
    @SerialName("add") val add: InstantAsIso8061String? = null,
    @SerialName("clean") val clean: InstantAsIso8061String? = null,
    @SerialName("max_song") val maxSong: Int? = null,
    @SerialName("max_album") val maxAlbum: Int? = null,
    @SerialName("max_artist") val maxArtist: Int? = null,
    @SerialName("max_video") val maxVideo: Int? = null,
    @SerialName("max_podcast") val maxPodcast: Int? = null,
    @SerialName("max_podcast_episode") val maxPodcastEpisode: Int? = null,
    @SerialName("songs") val songs: Int? = null,
    @SerialName("albums") val albums: Int? = null,
    @SerialName("artists") val artists: Int? = null,
    @SerialName("genres") val genres: Int? = null,
    @SerialName("playlists") val playlists: Int? = null,
    @SerialName("searches") val searches: Int? = null,
    @SerialName("playlists_searches") val playlistsSearches: Int? = null,
    @SerialName("users") val users: Int? = null,
    @SerialName("catalogs") val catalogs: Int? = null,
    @SerialName("videos") val videos: Int? = null,
    @SerialName("podcasts") val podcasts: Int? = null,
    @SerialName("podcast_episodes") val podcastEpisodes: Int? = null,
    @SerialName("shares") val shares: Int? = null,
    @SerialName("licenses") val licenses: Int? = null,
    @SerialName("live_streams") val liveStreams: Int? = null,
    @SerialName("labels") val labels: Int? = null,
    @SerialName("username") val username: String? = null,
)
