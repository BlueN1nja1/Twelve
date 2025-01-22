/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.Context
import androidx.media3.common.MediaMetadata
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import org.lineageos.twelve.models.Thumbnail

suspend fun MediaMetadata.toThumbnail(context: Context) = Thumbnail.Builder()
    .setBitmap(
        artworkData?.let {
            val imageRequest = ImageRequest.Builder(context)
                .data(it)
                .allowHardware(false)
                .build()

            context.imageLoader.execute(imageRequest).image?.toBitmap()
        }
    )
    .setUri(artworkUri)
    .setType(Thumbnail.Type.fromMedia3Value(artworkDataType))
    .build()
