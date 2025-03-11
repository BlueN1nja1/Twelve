/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.content.res.Resources
import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.MediaSource
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.buildMediaItem
import org.lineageos.twelve.ext.toByteArray

/**
 * An audio.
 *
 * @param playbackUri A URI that is understood by Media3 to play the audio. If required, this can be
 *   equal to [uri] and a proper [MediaSource.Factory] can be implemented. If this field is null,
 *   it means that currently this audio cannot be played
 * @param mimeType The MIME type of the audio
 * @param title The title of the audio
 * @param type The type of the audio
 * @param durationMs The duration of the audio in milliseconds
 * @param artistUri The URI of the artist of the audio
 * @param artistName The name of the artist of the audio
 * @param albumUri The URI of the album of the audio
 * @param albumTitle The title of the album of the audio
 * @param discNumber The number of the disc where the album is present, starts from 1
 * @param trackNumber The track number of the audio within the disc, starts from 1
 * @param genreUri The URI of the genre of the audio
 * @param genreName The name of the genre of the audio
 * @param year The year of release of the audio
 * @param isFavorite Whether this audio is a favorite
 */
data class Audio(
    override val uri: Uri,
    override val thumbnail: Thumbnail?,
    val playbackUri: Uri?,
    val mimeType: String?,
    val title: String?,
    val type: Type,
    val durationMs: Long?,
    val artistUri: Uri?,
    val artistName: String?,
    val albumUri: Uri?,
    val albumTitle: String?,
    val discNumber: Int?,
    val trackNumber: Int?,
    val genreUri: Uri?,
    val genreName: String?,
    val year: Int?,
    val isFavorite: Boolean,
) : MediaItem<Audio> {
    enum class Type(
        val media3MediaType: @MediaMetadata.MediaType Int,
    ) {
        /**
         * Music.
         */
        MUSIC(MediaMetadata.MEDIA_TYPE_MUSIC),

        /**
         * Podcast.
         */
        PODCAST(MediaMetadata.MEDIA_TYPE_PODCAST),

        /**
         * Audiobook.
         */
        AUDIOBOOK(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK),

        /**
         * Recording.
         */
        RECORDING(MediaMetadata.MEDIA_TYPE_MUSIC),
    }

    override val mediaType = MediaType.AUDIO

    override fun areContentsTheSame(other: Audio) = compareValuesBy(
        this, other,
        Audio::thumbnail,
        Audio::playbackUri,
        Audio::mimeType,
        Audio::title,
        Audio::type,
        Audio::durationMs,
        Audio::artistUri,
        Audio::artistName,
        Audio::albumUri,
        Audio::albumTitle,
        Audio::discNumber,
        Audio::trackNumber,
        Audio::genreUri,
        Audio::genreName,
        Audio::year,
        Audio::isFavorite,
    ) == 0

    override fun toMedia3MediaItem(resources: Resources) = buildMediaItem(
        title = title ?: resources.getString(R.string.audio_unknown),
        mediaId = uri.toString(),
        isPlayable = playbackUri != null,
        isBrowsable = false,
        mediaType = type.media3MediaType,
        album = albumTitle,
        artist = artistName,
        genre = genreName,
        sourceUri = playbackUri,
        mimeType = mimeType,
        artworkData = thumbnail?.bitmap?.toByteArray(),
        artworkType = thumbnail?.type?.media3Value,
        artworkUri = thumbnail?.uri,
        discNumber = discNumber,
        trackNumber = trackNumber,
        durationMs = durationMs,
        isFavorite = isFavorite,
    )

    class Builder(uri: Uri) : MediaItem.Builder<Builder, Audio>(uri) {
        private var playbackUri: Uri? = null
        private var mimeType: String? = null
        private var title: String? = null
        private var type: Type = Type.MUSIC
        private var durationMs: Long? = null
        private var artistUri: Uri? = null
        private var artistName: String? = null
        private var albumUri: Uri? = null
        private var albumTitle: String? = null
        private var discNumber: Int? = null
        private var trackNumber: Int? = null
        private var genreUri: Uri? = null
        private var genreName: String? = null
        private var year: Int? = null
        private var isFavorite: Boolean = false

        /**
         * @see Audio.playbackUri
         */
        fun setPlaybackUri(playbackUri: Uri?) = this.also {
            this.playbackUri = playbackUri
        }

        /**
         * @see Audio.mimeType
         */
        fun setMimeType(mimeType: String?) = this.also {
            this.mimeType = mimeType
        }

        /**
         * @see Audio.title
         */
        fun setTitle(title: String?) = this.also {
            this.title = title
        }

        /**
         * @see Audio.type
         */
        fun setType(type: Type) = this.also {
            this.type = type
        }

        /**
         * @see Audio.durationMs
         */
        fun setDurationMs(durationMs: Long?) = this.also {
            this.durationMs = durationMs
        }

        /**
         * @see Audio.artistUri
         */
        fun setArtistUri(artistUri: Uri?) = this.also {
            this.artistUri = artistUri
        }

        /**
         * @see Audio.artistName
         */
        fun setArtistName(artistName: String?) = this.also {
            this.artistName = artistName
        }

        /**
         * @see Audio.albumUri
         */
        fun setAlbumUri(albumUri: Uri?) = this.also {
            this.albumUri = albumUri
        }

        /**
         * @see Audio.albumTitle
         */
        fun setAlbumTitle(albumTitle: String?) = this.also {
            this.albumTitle = albumTitle
        }

        /**
         * @see Audio.discNumber
         */
        fun setDiscNumber(discNumber: Int?) = this.also {
            this.discNumber = discNumber
        }

        /**
         * @see Audio.trackNumber
         */
        fun setTrackNumber(trackNumber: Int?) = this.also {
            this.trackNumber = trackNumber
        }

        /**
         * @see Audio.genreUri
         */
        fun setGenreUri(genreUri: Uri?) = this.also {
            this.genreUri = genreUri
        }

        /**
         * @see Audio.genreName
         */
        fun setGenreName(genreName: String?) = this.also {
            this.genreName = genreName
        }

        /**
         * @see Audio.year
         */
        fun setYear(year: Int?) = this.also {
            this.year = year
        }

        /**
         * @see Audio.isFavorite
         */
        fun setIsFavorite(isFavorite: Boolean) = this.also {
            this.isFavorite = isFavorite
        }

        override fun build() = Audio(
            uri = uri,
            thumbnail = thumbnail,
            playbackUri = playbackUri,
            mimeType = mimeType,
            title = title,
            type = type,
            durationMs = durationMs,
            artistUri = artistUri,
            artistName = artistName,
            albumUri = albumUri,
            albumTitle = albumTitle,
            discNumber = discNumber,
            trackNumber = trackNumber,
            genreUri = genreUri,
            genreName = genreName,
            year = year,
            isFavorite = isFavorite,
        )
    }
}
