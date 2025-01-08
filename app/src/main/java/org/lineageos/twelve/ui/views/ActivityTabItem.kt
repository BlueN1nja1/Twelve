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
import org.lineageos.twelve.models.Playlist

class ActivityTabItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
) : MaterialCardView(context, attrs, defStyleAttr) {
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

        inflate(context, R.layout.item_activity_tab, this)
    }

    fun setItem(item: MediaItem<*>) {
        when (item) {
            is Album -> {
                item.title?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.album_unknown)
                subheadText = item.artistName
                supportingText = item.year?.toString()

                loadThumbnailImage(item.thumbnail, R.drawable.ic_album)
            }

            is Artist -> {
                item.name?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.artist_unknown)
                subheadText = null
                supportingText = null

                loadThumbnailImage(item.thumbnail, R.drawable.ic_person)
            }

            is Audio -> {
                headlineText = item.title
                subheadText = item.artistName
                supportingText = item.albumTitle

                setPlaceholderImage(R.drawable.ic_music_note)
            }

            is Genre -> {
                item.name?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.genre_unknown)
                subheadText = null
                supportingText = null

                setPlaceholderImage(R.drawable.ic_genres)
            }

            is Playlist -> {
                headlineText = item.name
                subheadText = null
                supportingText = null

                setPlaceholderImage(R.drawable.ic_playlist_play)
            }
        }
    }

    private fun setPlaceholderImage(@DrawableRes placeholder: Int) {
        placeholderImageView.setImageResource(placeholder)
        placeholderImageView.isVisible = true
        thumbnailImageView.isVisible = false
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
