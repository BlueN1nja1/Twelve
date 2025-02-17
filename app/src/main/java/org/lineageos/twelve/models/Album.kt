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
 * An album.
 *
 * @param title The title of the album
 * @param artistUri The URI of the artist
 * @param artistName The name of the artist
 * @param year The year of the album
 */
data class Album(
    override val uri: Uri,
    override val thumbnail: Thumbnail?,
    val title: String?,
    val artistUri: Uri?,
    val artistName: String?,
    val year: Int?,
) : MediaItem<Album> {
    override val mediaType = MediaType.ALBUM

    override fun areContentsTheSame(other: Album) = compareValuesBy(
        this, other,
        Album::thumbnail,
        Album::title,
        Album::artistUri,
        Album::artistName,
        Album::year,
    ) == 0

    override fun toMedia3MediaItem(resources: Resources) = buildMediaItem(
        title = title ?: resources.getString(R.string.album_unknown),
        mediaId = uri.toString(),
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
        sourceUri = uri,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
    )

    class Builder(uri: Uri) : MediaItem.Builder<Builder, Album>(uri) {
        private var title: String? = null
        private var artistUri: Uri? = null
        private var artistName: String? = null
        private var year: Int? = null

        /**
         * @see Album.title
         */
        fun setTitle(title: String?) = this.also {
            this.title = title
        }

        /**
         * @see Album.artistUri
         */
        fun setArtistUri(artistUri: Uri?) = this.also {
            this.artistUri = artistUri
        }

        /**
         * @see Album.artistName
         */
        fun setArtistName(artistName: String?) = this.also {
            this.artistName = artistName
        }

        /**
         * @see Album.year
         */
        fun setYear(year: Int?) = this.also {
            this.year = year
        }

        override fun build() = Album(
            uri = uri,
            thumbnail = thumbnail,
            title = title,
            artistUri = artistUri,
            artistName = artistName,
            year = year,
        )
    }
}
