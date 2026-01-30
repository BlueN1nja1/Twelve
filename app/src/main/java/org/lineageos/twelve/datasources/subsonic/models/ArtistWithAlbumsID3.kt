/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistWithAlbumsID3(
    // ArtistID3 start
    val id: String,
    val name: String,
    val coverArt: String? = null,
    val artistImageUrl: UriAsString? = null,
    val albumCount: Int,
    val starred: InstantAsString? = null,

    // OpenSubsonic
    val sortName: String? = null,
    // ArtistID3 end

    val album: List<AlbumID3>,
) {
    fun toArtistID3() = ArtistID3(
        id = id,
        name = name,
        coverArt = coverArt,
        artistImageUrl = artistImageUrl,
        albumCount = albumCount,
        starred = starred,
        sortName = sortName,
    )
}
