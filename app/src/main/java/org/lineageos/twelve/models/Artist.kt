/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.ext.buildMediaItem
import org.lineageos.twelve.ext.toByteArray

/**
 * An artist.
 *
 * @param uri The URI of the artist
 * @param name The name of the artist
 * @param thumbnail The artist's thumbnail
 */
data class Artist(
    override val uri: Uri,
    val name: String?,
    val thumbnail: Thumbnail?,
) : MediaItem<Artist> {
    override val mediaType = MediaType.ARTIST

    override fun areContentsTheSame(other: Artist) = compareValuesBy(
        this, other,
        Artist::name,
        Artist::thumbnail,
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = name,
        mediaId = uri.toString(),
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
        sourceUri = uri,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
    )
}
