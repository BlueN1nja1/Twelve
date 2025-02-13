/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import coil3.load
import coil3.request.ImageRequest
import com.google.android.material.card.MaterialCardView
import org.lineageos.twelve.R
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist

abstract class BaseMediaItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
    @LayoutRes private val layoutResId: Int
) : MaterialCardView(context, attrs, defStyleAttr) {
    // Views
    private val headlineTextView by lazy { findViewById<TextView>(R.id.headlineTextView) }
    private val placeholderImageView by lazy { findViewById<ImageView>(R.id.placeholderImageView) }
    private val subheadTextView by lazy { findViewById<TextView>(R.id.subheadTextView) }
    private val supportingTextView by lazy { findViewById<TextView>(R.id.supportingTextView) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }

    private var headlineText: CharSequence?
        get() = headlineTextView.text
        set(value) {
            headlineTextView.setTextAndUpdateVisibility(value)
        }

    private var subheadText: CharSequence?
        get() = subheadTextView.text
        set(value) {
            subheadTextView.setTextAndUpdateVisibility(value)
        }

    private var supportingText: CharSequence?
        get() = supportingTextView.text
        set(value) {
            supportingTextView.setTextAndUpdateVisibility(value)
        }

    init {
        setCardBackgroundColor(Color.TRANSPARENT)
        cardElevation = 0f
        strokeWidth = 0

        inflate(context, layoutResId, this)
    }

    final override fun setCardBackgroundColor(color: Int) {
        super.setCardBackgroundColor(color)
    }

    fun setItem(item: MediaItem<*>) {
        loadThumbnailImage(
            item.thumbnail,
            when (item.mediaType) {
                MediaType.ALBUM -> R.drawable.ic_album
                MediaType.ARTIST -> R.drawable.ic_person
                MediaType.AUDIO -> R.drawable.ic_music_note
                MediaType.GENRE -> R.drawable.ic_genres
                MediaType.PLAYLIST -> R.drawable.ic_playlist_play
            }
        )

        when (item) {
            is Album -> {
                item.title?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.album_unknown)
                subheadText = item.artistName
                supportingText = item.year?.toString()
            }

            is Artist -> {
                item.name?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.artist_unknown)
                subheadText = null
                supportingText = null
            }

            is Audio -> {
                headlineText = item.title
                subheadText = item.artistName
                supportingText = item.albumTitle
            }

            is Genre -> {
                item.name?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.genre_unknown)
                subheadText = null
                supportingText = null
            }

            is Playlist -> {
                headlineText = item.name
                subheadText = null
                supportingText = null
            }
        }
    }

    private fun loadThumbnailImage(
        data: Any?,
        @DrawableRes placeholder: Int? = null,
        builder: ImageRequest.Builder.() -> Unit = {
            listener(
                onCancel = {
                    placeholder?.let {
                        placeholderImageView.setImageResource(it)
                        placeholderImageView.isVisible = true
                    }
                    thumbnailImageView.isVisible = false
                },
                onError = { _, _ ->
                    placeholder?.let {
                        placeholderImageView.setImageResource(it)
                        placeholderImageView.isVisible = true
                    }
                    thumbnailImageView.isVisible = false
                },
                onSuccess = { _, _ ->
                    placeholderImageView.isVisible = false
                    thumbnailImageView.isVisible = true
                },
            )
        }
    ) = thumbnailImageView.load(data, builder = builder)

    private fun setHeadlineText(@StringRes resId: Int) =
        headlineTextView.setTextAndUpdateVisibility(resId)

    private fun setSubheadText(@StringRes resId: Int) =
        subheadTextView.setTextAndUpdateVisibility(resId)

    private fun setSupportingText(@StringRes resId: Int) =
        supportingTextView.setTextAndUpdateVisibility(resId)

    // TextView utils

    private fun TextView.setTextAndUpdateVisibility(text: CharSequence?) {
        this.text = text.also {
            isVisible = it != null
        }
    }

    private fun TextView.setTextAndUpdateVisibility(@StringRes resId: Int) =
        setTextAndUpdateVisibility(resources.getText(resId))
}
