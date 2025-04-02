/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.mediastore

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

/**
 * A [MediaStore.Audio] item URI.
 */
data class MediaStoreAudioUri(
    val volumeName: String,
    val type: Type,
    val id: Long,
) {
    enum class Type(val value: String) {
        ALBUMS("albums"),
        ARTISTS("artists"),
        GENRES("genres"),
        MEDIA("media");

        companion object {
            fun fromValue(value: String): Type? = entries.firstOrNull { it.value == value }
        }
    }

    fun toUri(): Uri = Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(MediaStore.AUTHORITY)
        .appendPath(AUDIO_PATH)
        .appendPath(volumeName)
        .appendPath(type.value)
        .appendPath(id.toString())
        .build()

    companion object {
        private const val AUDIO_PATH = "audio"

        /**
         * Create a [MediaStoreAudioUri] from a [Uri].
         *
         * @param uri The [Uri] to parse
         * @return The [MediaStoreAudioUri] or null if the [Uri] is invalid
         */
        fun from(uri: Uri): MediaStoreAudioUri? {
            if (uri.scheme != ContentResolver.SCHEME_CONTENT) {
                return null
            }

            if (uri.authority != MediaStore.AUTHORITY) {
                return null
            }

            val pathSegments = uri.pathSegments
            if (pathSegments.size != 4) {
                return null
            }

            val (volumeName, audio, type, id) = pathSegments

            if (audio != AUDIO_PATH) {
                return null
            }

            return MediaStoreAudioUri(
                volumeName = volumeName,
                type = Type.fromValue(type) ?: return null,
                id = id.toLongOrNull() ?: return null,
            )
        }
    }
}
