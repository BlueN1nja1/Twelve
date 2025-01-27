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
 * An album.
 *
 * @param uri The URI of the album
 * @param title The title of the album
 * @param artistUri The URI of the artist
 * @param artistName The name of the artist
 * @param year The year of the album
 * @param thumbnail The album's thumbnail
 */
data class Album(
    override val uri: Uri,
    val title: String?,
    val artistUri: Uri,
    val artistName: String?,
    val year: Int?,
    val thumbnail: Thumbnail?,
) : MediaItem<Album> {
    override val mediaType = MediaType.ALBUM

    override fun areContentsTheSame(other: Album) = compareValuesBy(
        this, other,
        Album::title,
        Album::artistUri,
        Album::artistName,
        Album::year,
        Album::thumbnail,
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = title,
        mediaId = uri.toString(),
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
        sourceUri = uri,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
    )
}
