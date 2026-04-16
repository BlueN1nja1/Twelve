/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Album.
 *
 * @param id The ID
 * @param name The name
 * @param prefix TODO
 * @param basename TODO
 * @param artist The main artist
 * @param artists The artists
 * @param songArtists The song artists
 * @param time The time
 * @param year The year
 * @param tracks The tracks
 * @param songCount The song count
 * @param diskCount The disk count
 * @param type TODO
 * @param genre The genre
 * @param art The artwork URL
 * @param hasArt Whether this album has an artwork available
 * @param flag TODO
 * @param rating The rating
 * @param averageRating The average rating
 * @param mbid MusicBrainz Identifier
 * @param mbidGroup MusicBrainz Identifier group
 */
@Serializable
data class Album(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("prefix") val prefix: String?,
    @SerialName("basename") val basename: String,
    @SerialName("artist") val artist: Artist? = null,
    @SerialName("artists") val artists: List<Artist>? = null,
    @SerialName("songartists") val songArtists: List<Artist>? = null,
    @SerialName("time") val time: Int? = null,
    @SerialName("year") val year: Int? = null,
    @SerialName("tracks") val tracks: List<Song>? = null,
    @SerialName("songcount") val songCount: Int? = null,
    @SerialName("diskcount") val diskCount: Int? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("genre") val genre: List<Genre>? = null,
    @SerialName("art") val art: String? = null,
    @SerialName("has_art") val hasArt: Boolean? = null,
    @SerialName("flag") val flag: Boolean? = null,
    @SerialName("rating") val rating: Int? = null,
    @SerialName("averagerating") val averageRating: Int? = null,
    @SerialName("mbid") val mbid: String? = null,
    @SerialName("mbid_group") val mbidGroup: String? = null,
)
