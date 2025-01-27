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
 * A user-defined playlist.
 *
 * @param uri The URI of the playlist
 * @param name The name of the playlist
 * @param thumbnail The thumbnail of the playlist
 */
data class Playlist(
    override val uri: Uri,
    val name: String,
    val thumbnail: Thumbnail? = null,
) : MediaItem<Playlist> {
    override val mediaType = MediaType.PLAYLIST

    override fun areContentsTheSame(other: Playlist) = compareValuesBy(
        this,
        other,
        Playlist::name,
        Playlist::thumbnail,
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = name,
        mediaId = uri.toString(),
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
        sourceUri = uri,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
    )
}
