/*
 * SPDX-FileCopyrightText: 2024-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.recyclerview

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import org.lineageos.twelve.ext.toPx

/**
 * GridLayoutManager that uses a proper span count based on the display orientation and DPI.
 * @param context Context.
 * @param targetSpanCount Target span count, also minimum if there's not enough space,
 * thumbnails will be resized accordingly.
 * @param thumbnailPaddingDp Padding applied to thumbnails.
 */
class DisplayAwareGridLayoutManager @JvmOverloads constructor(
    context: Context,
    targetSpanCount: Int,
    thumbnailPaddingDp: Int = 8,
) : GridLayoutManager(
    context,
    getSpanCount(context, targetSpanCount, thumbnailPaddingDp),
) {
    companion object {
        /**
         * Maximum thumbnail size, useful for high density screens.
         */
        private const val MAX_THUMBNAIL_SIZE = 256

        private enum class Orientation {
            VERTICAL,
            HORIZONTAL,
        }

        private fun getSpanCount(
            context: Context,
            targetSpanCount: Int,
            thumbnailPaddingDp: Int,
        ): Int {
            val displayMetrics = context.resources.displayMetrics

            // Account for thumbnail padding
            val paddingSizePx = displayMetrics.toPx(thumbnailPaddingDp) * targetSpanCount
            val availableHeight = displayMetrics.heightPixels - paddingSizePx
            val availableWidth = displayMetrics.widthPixels - paddingSizePx

            val orientation = when {
                availableWidth > availableHeight -> Orientation.HORIZONTAL
                else -> Orientation.VERTICAL
            }

            val columnsSpace = when (orientation) {
                Orientation.HORIZONTAL -> availableHeight
                Orientation.VERTICAL -> availableWidth
            }

            val thumbnailSize = (columnsSpace / targetSpanCount)
                .coerceAtMost(displayMetrics.toPx(MAX_THUMBNAIL_SIZE))

            return (availableWidth / thumbnailSize).coerceAtLeast(targetSpanCount)
        }
    }
}
