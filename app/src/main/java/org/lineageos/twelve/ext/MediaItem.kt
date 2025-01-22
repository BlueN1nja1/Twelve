/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi

@androidx.annotation.OptIn(UnstableApi::class)
fun buildMediaItem(
    title: String?,
    mediaId: String,
    isPlayable: Boolean,
    isBrowsable: Boolean,
    mediaType: @MediaMetadata.MediaType Int?,
    album: String? = null,
    artist: String? = null,
    genre: String? = null,
    sourceUri: Uri? = null,
    mimeType: String? = null,
    artworkData: ByteArray? = null,
    artworkType: @MediaMetadata.PictureType Int? = null,
    artworkUri: Uri? = null,
    discNumber: Int? = null,
    trackNumber: Int? = null,
    durationMs: Long? = null,
    subtitle: String? = null,
): MediaItem {
    val metadata =
        MediaMetadata.Builder()
            .setAlbumTitle(album)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setArtist(artist)
            .setGenre(genre)
            .setIsBrowsable(isBrowsable)
            .setIsPlayable(isPlayable)
            .setArtworkData(artworkData, artworkType)
            .setArtworkUri(artworkUri)
            .setMediaType(mediaType)
            .setDiscNumber(discNumber)
            .setTrackNumber(trackNumber)
            .setDurationMs(durationMs)
            .build()

    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(metadata)
        .setUri(sourceUri)
        .setMimeType(mimeType)
        .build()
}

suspend fun MediaItem.toThumbnail(context: Context) = mediaMetadata.toThumbnail(context)
