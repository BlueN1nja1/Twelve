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
 * A music genre.
 * TODO: Maybe make it an enum class and follow https://en.wikipedia.org/wiki/List_of_ID3v1_genres
 *
 * @param name The name of the genre. Can be null
 */
data class Genre(
    override val uri: Uri,
    override val thumbnail: Thumbnail?,
    val name: String?,
) : MediaItem<Genre> {
    override val mediaType = MediaType.GENRE

    override fun areContentsTheSame(other: Genre) = compareValuesBy(
        this, other,
        Genre::thumbnail,
        Genre::name,
    ) == 0

    override fun toMedia3MediaItem(resources: Resources) = buildMediaItem(
        title = name ?: resources.getString(R.string.genre_unknown),
        mediaId = uri.toString(),
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
        sourceUri = uri,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
    )

    class Builder(uri: Uri) : MediaItem.Builder<Builder, Genre>(uri) {
        private var name: String? = null

        /**
         * @see Genre.name
         */
        fun setName(name: String?) = this.also {
            this.name = name
        }

        override fun build() = Genre(
            uri = uri,
            thumbnail = thumbnail,
            name = name,
        )
    }
}
