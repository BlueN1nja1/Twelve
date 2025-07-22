/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.provider.MediaStore
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import org.lineageos.twelve.models.MediaType

object MimeUtils {
    @androidx.annotation.OptIn(UnstableApi::class)
    fun mimeTypeToDisplayName(mimeType: String) =
        when (val it = MimeTypes.normalizeMimeType(mimeType)) {
            MimeTypes.AUDIO_MPEG -> "MP3"

            else -> it.takeIf { it.contains('/') }
                ?.substringAfterLast('/')
                ?.uppercase()
        }

    fun mimeTypeToMediaType(mimeType: String) = when {
        mimeType.startsWith("audio/") -> MediaType.AUDIO

        else -> when (mimeType) {
            "application/itunes",
            "application/ogg",
            "application/vnd.apple.mpegurl",
            "application/vnd.ms-sstr+xml",
            "application/x-mpegurl",
            "application/x-ogg" -> MediaType.AUDIO

            MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> MediaType.AUDIO

            MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE -> MediaType.ALBUM

            MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> MediaType.ARTIST

            MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE -> MediaType.GENRE

            @Suppress("DEPRECATION")
            MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE -> MediaType.PLAYLIST

            else -> null
        }
    }
}
