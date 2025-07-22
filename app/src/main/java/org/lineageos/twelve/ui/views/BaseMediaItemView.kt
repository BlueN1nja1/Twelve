/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import coil3.load
import com.google.android.material.card.MaterialCardView
import org.lineageos.twelve.R
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.Thumbnail

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

    private var isDimmed: Boolean
        get() = !headlineTextView.isEnabled
        set(value) = setViewsProperty(View::setEnabled, !value)

    init {
        setCardBackgroundColor(
            resources.getColorStateList(R.color.list_item_background, context.theme)
        )
        cardElevation = 0f
        strokeWidth = 0

        inflate(context, layoutResId, this)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        setViewsProperty(View::setEnabled, enabled)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        setViewsProperty(View::setSelected, selected)
    }

    final override fun setCardBackgroundColor(color: ColorStateList?) {
        super.setCardBackgroundColor(color)
    }

    fun setItem(item: MediaItem<*>) {
        loadThumbnailImage(
            item.thumbnail,
            when (item) {
                is Album -> R.drawable.ic_album
                is Artist -> R.drawable.ic_person
                is Audio -> R.drawable.ic_music_note
                is Genre -> R.drawable.ic_genres
                is Playlist -> when (item.type) {
                    Playlist.Type.PLAYLIST -> R.drawable.ic_playlist_play
                    Playlist.Type.FAVORITES -> R.drawable.ic_favorite
                }
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
                headlineText = item.name ?: resources.getString(
                    when (item.type) {
                        Playlist.Type.PLAYLIST -> R.string.playlist_unknown
                        Playlist.Type.FAVORITES -> R.string.favorites_playlist
                    }
                )
                subheadText = null
                supportingText = null
            }
        }

        isDimmed = when (item) {
            is Audio -> item.playbackUri == null
            else -> false
        }
    }

    private fun loadThumbnailImage(data: Thumbnail?, @DrawableRes placeholder: Int) {
        placeholderImageView.setImageResource(placeholder)

        thumbnailImageView.load(
            data,
            builder = {
                listener(
                    onCancel = {
                        placeholderImageView.isVisible = true
                        thumbnailImageView.isVisible = false
                    },
                    onError = { _, _ ->
                        placeholderImageView.isVisible = true
                        thumbnailImageView.isVisible = false
                    },
                    onSuccess = { _, _ ->
                        placeholderImageView.isVisible = false
                        thumbnailImageView.isVisible = true
                    },
                )
            }
        )
    }

    private fun setHeadlineText(@StringRes resId: Int) =
        headlineTextView.setTextAndUpdateVisibility(resId)

    private fun setSubheadText(@StringRes resId: Int) =
        subheadTextView.setTextAndUpdateVisibility(resId)

    private fun setSupportingText(@StringRes resId: Int) =
        supportingTextView.setTextAndUpdateVisibility(resId)

    private inline fun <T> setViewsProperty(
        setter: View.(T) -> Unit,
        value: T,
    ) {
        headlineTextView.setter(value)
        placeholderImageView.setter(value)
        subheadTextView.setter(value)
        supportingTextView.setter(value)
    }

    // TextView utils

    private fun TextView.setTextAndUpdateVisibility(text: CharSequence?) {
        this.text = text.also {
            isVisible = it != null
        }
    }

    private fun TextView.setTextAndUpdateVisibility(@StringRes resId: Int) =
        setTextAndUpdateVisibility(resources.getText(resId))
}
