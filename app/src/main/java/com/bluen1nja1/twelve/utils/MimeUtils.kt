/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.utils

import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.bluen1nja1.twelve.models.MediaType

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
            "application/x-ogg",
            "vnd.android.cursor.item/audio" -> MediaType.AUDIO

            "vnd.android.cursor.item/album" -> MediaType.ALBUM

            "vnd.android.cursor.item/artist" -> MediaType.ARTIST

            "vnd.android.cursor.item/playlist" -> MediaType.PLAYLIST

            else -> null
        }
    }
}
