/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.content.res.Resources
import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.buildMediaItem
import org.lineageos.twelve.ext.toByteArray

/**
 * A user-defined playlist.
 *
 * @param name The name of the playlist
 */
data class Playlist(
    override val uri: Uri,
    override val thumbnail: Thumbnail?,
    val name: String?,
) : MediaItem<Playlist> {
    override val mediaType = MediaType.PLAYLIST

    override fun areContentsTheSame(other: Playlist) = compareValuesBy(
        this, other,
        Playlist::thumbnail,
        Playlist::name,
    ) == 0

    override fun toMedia3MediaItem(resources: Resources) = buildMediaItem(
        title = name ?: resources.getString(R.string.playlist_unknown),
        mediaId = uri.toString(),
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
        sourceUri = uri,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
    )

    class Builder(uri: Uri) : MediaItem.Builder<Builder, Playlist>(uri) {
        private var name: String? = null

        /**
         * @see Playlist.name
         */
        fun setName(name: String?) = this.also {
            this.name = name
        }

        override fun build() = Playlist(
            uri = uri,
            thumbnail = thumbnail,
            name = name,
        )
    }
}
