/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Genre.
 *
 * @param id The ID
 * @param name The name
 * @param albums The albums
 * @param artists The artists
 * @param songs The songs
 * @param videos The videos
 * @param playlists The playlists
 * @param liveStreams The live streams
 * @param isHidden Whether this genre is hidden
 * @param merge TODO
 */
@Serializable
data class Genre(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("albums") val albums: Int? = null,
    @SerialName("artists") val artists: Int? = null,
    @SerialName("songs") val songs: Int? = null,
    @SerialName("videos") val videos: Int? = null,
    @SerialName("playlists") val playlists: Int? = null,
    @SerialName("live_streams") val liveStreams: Int? = null,
    @SerialName("is_hidden") val isHidden: Boolean? = null,
    @SerialName("merge") val merge: List<String>? = null,
)
