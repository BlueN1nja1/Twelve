/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Artist.
 *
 * @param id The ID
 * @param name The name
 * @param prefix The prefix
 * @param basename The basename
 * @param albums The albums
 * @param albumCount The album count
 * @param songs The songs
 * @param songCount The song count
 * @param genre The genre
 * @param art The artwork URL
 * @param hasArt Whether this artist has an artwork available
 * @param flag The flag
 * @param rating The rating
 * @param averageRating The average rating
 * @param mbid MusicBrainz Identifier
 * @param summary The summary
 * @param time The time
 * @param yearFormed The year when the band was formed
 * @param placeFormed The place where the band was formed
 */
@Serializable
data class Artist(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("prefix") val prefix: String?,
    @SerialName("basename") val basename: String,
    @SerialName("albums") val albums: List<Album>? = null,
    @SerialName("albumcount") val albumCount: Int? = null,
    @SerialName("songs") val songs: List<Song>? = null,
    @SerialName("songcount") val songCount: Int? = null,
    @SerialName("genre") val genre: List<Genre>? = null,
    @SerialName("art") val art: String? = null,
    @SerialName("has_art") val hasArt: Boolean? = null,
    @SerialName("flag") val flag: Boolean? = null,
    @SerialName("rating") val rating: Int? = null,
    @SerialName("averagerating") val averageRating: Double? = null,
    @SerialName("mbid") val mbid: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("time") val time: Int? = null,
    @SerialName("yearformed") val yearFormed: Int? = null,
    @SerialName("placeformed") val placeFormed: String? = null,
)
