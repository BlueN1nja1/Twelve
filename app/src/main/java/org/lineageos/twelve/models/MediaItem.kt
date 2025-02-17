/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.content.res.Resources
import android.net.Uri

/**
 * A media item.
 */
sealed interface MediaItem<T : MediaItem<T>> : UniqueItem<T> {
    /**
     * The media type.
     */
    val mediaType: MediaType

    /**
     * A [Uri] identifying this media item.
     */
    val uri: Uri

    /**
     * A [Thumbnail] for this media item. Note that this field may be null even if it has a valid
     * thumbnail to avoid memory clogging, for example when multiple child items uses the thumbnail
     * of their parent.
     */
    val thumbnail: Thumbnail?

    override fun areItemsTheSame(other: T) = this.uri == other.uri

    /**
     * Convert this item to a media item.
     * @param resources The resources used to retrieve the fallback resources.
     */
    fun toMedia3MediaItem(resources: Resources): androidx.media3.common.MediaItem

    /**
     * Builder for a [MediaItem].
     *
     * [Code inspiration](https://dev.to/glefloch/self-referencing-generics-wait-what--1amb)
     *
     * @param B The builder class
     * @param T The [MediaItem] type
     * @param uri The [Uri] of the media item
     */
    abstract class Builder<B : Builder<B, T>, T : MediaItem<T>>(
        protected val uri: Uri,
    ) {
        protected var thumbnail: Thumbnail? = null

        /**
         * @see MediaItem.thumbnail
         */
        fun setThumbnail(thumbnail: Thumbnail?) = self.also {
            this.thumbnail = thumbnail
        }

        /**
         * Build the [MediaItem].
         */
        abstract fun build(): T

        private val self: B
            @Suppress("UNCHECKED_CAST")
            get() = this as B
    }
}
