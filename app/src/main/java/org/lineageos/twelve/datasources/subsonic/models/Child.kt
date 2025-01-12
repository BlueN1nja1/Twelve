/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Child(
    val id: String,
    val parent: String? = null,
    val isDir: Boolean,
    val title: String,
    val album: String? = null,
    val artist: String? = null,
    val track: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val coverArt: String? = null,
    val size: Long? = null,
    val contentType: String? = null,
    val suffix: String? = null,
    val transcodedContentType: String? = null,
    val transcodedSuffix: String? = null,
    val duration: Int? = null,
    val bitRate: Int? = null,
    val path: String? = null,
    val isVideo: Boolean? = null,
    @IntRange(from = 1, to = 5) val userRating: Int? = null,
    @FloatRange(from = 0.0, to = 5.0) val averageRating: Double? = null,
    val playCount: Long? = null,
    val discNumber: Int? = null,
    val created: InstantAsString? = null,
    val starred: InstantAsString? = null,
    val albumId: String? = null,
    val artistId: String? = null,
    val type: MediaType? = null,
    val bookmarkPosition: Long? = null,
    val originalWidth: Int? = null,
    val originalHeight: Int? = null,

    // OpenSubsonic
    val bitDepth: Int? = null,
    val samplingRate: Int? = null,
    val channelCount: Int? = null,
    val mediaType: String? = null,
    val played: String? = null,
    val bpm: Int? = null,
    val comment: String? = null,
    val sortName: String? = null,
    val musicBrainzId: String? = null,
    val genres: List<ItemGenre>? = null,
    val artists: List<ArtistID3>? = null,
    val displayArtist: String? = null,
    val albumArtists: List<ArtistID3>? = null,
    val displayAlbumArtist: String? = null,
    val contributors: List<Contributor>? = null,
    val displayComposer: String? = null,
    val moods: List<String>? = null,
    val replayGain: ReplayGain? = null,
)
