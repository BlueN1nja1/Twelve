/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.MediaSource
import org.lineageos.twelve.ext.buildMediaItem

/**
 * An audio.
 *
 * @param uri The URI of the audio
 * @param playbackUri A URI that is understood by Media3 to play the audio. If required, this can be
 *   equal to [uri] and a proper [MediaSource.Factory] can be implemented
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
 */
data class Audio(
    override val uri: Uri,
    val playbackUri: Uri,
    val mimeType: String,
    val title: String,
    val type: Type,
    val durationMs: Long,
    val artistUri: Uri,
    val artistName: String?,
    val albumUri: Uri,
    val albumTitle: String?,
    val discNumber: Int?,
    val trackNumber: Int?,
    val genreUri: Uri?,
    val genreName: String?,
    val year: Int?,
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
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = title,
        mediaId = uri.toString(),
        isPlayable = true,
        isBrowsable = false,
        mediaType = type.media3MediaType,
        album = albumTitle,
        artist = artistName,
        genre = genreName,
        sourceUri = playbackUri,
        mimeType = mimeType,
        discNumber = discNumber,
        trackNumber = trackNumber,
        durationMs = durationMs,
    )
}
