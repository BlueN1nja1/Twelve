/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.ui.views

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.bluen1nja1.twelve.R
import com.bluen1nja1.twelve.ext.loadThumbnail
import com.bluen1nja1.twelve.ext.slideDown
import com.bluen1nja1.twelve.ext.slideUp
import com.bluen1nja1.twelve.models.Thumbnail
import kotlin.reflect.safeCast

class NowPlayingBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val artistNameTextView by lazy { findViewById<TextView>(R.id.artistNameTextView) }
    private val albumTitleTextView by lazy { findViewById<TextView>(R.id.albumTitleTextView) }
    private val circularProgressIndicator by lazy { findViewById<CircularProgressIndicator>(R.id.circularProgressIndicator) }
    private val materialCardView by lazy { findViewById<MaterialCardView>(R.id.materialCardView) }
    private val playPauseMaterialButton by lazy { findViewById<MaterialButton>(R.id.playPauseMaterialButton) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }
    private val titleTextView by lazy { findViewById<TextView>(R.id.titleTextView) }

    private var isBottomNavigationBar = false

    init {
        inflate(context, R.layout.now_playing_bar, this)

        context.obtainStyledAttributes(attrs, R.styleable.NowPlayingBar, 0, 0).apply {
            try {
                isBottomNavigationBar = getBoolean(
                    R.styleable.NowPlayingBar_isBottomNavigationBar,
                    false
                )
            } finally {
                recycle()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }

            materialCardView.setContentPadding(
                0,
                0,
                0,
                when (isBottomNavigationBar) {
                    true -> insets.bottom
                    false -> 0
                }
            )

            windowInsets
        }

        circularProgressIndicator.min = 0
    }

    fun setOnPlayPauseClickListener(l: OnClickListener?) =
        playPauseMaterialButton.setOnClickListener(l)

    fun setOnNowPlayingClickListener(l: OnClickListener?) {
        materialCardView.setOnClickListener(l)
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        playPauseMaterialButton.setIconResource(
            when (isPlaying) {
                true -> R.drawable.avd_play_to_pause
                false -> R.drawable.avd_pause_to_play
            }
        )
        AnimatedVectorDrawable::class.safeCast(playPauseMaterialButton.icon)?.start()
    }

    fun updateMediaItem(mediaItem: MediaItem?) {
        if (mediaItem != null) {
            slideUp()
        } else {
            slideDown()
        }
    }

    fun updateMediaMetadata(mediaMetadata: MediaMetadata) {
        val audioTitle = mediaMetadata.displayTitle
            ?: mediaMetadata.title
            ?: context.getString(R.string.unknown)
        if (titleTextView.text != audioTitle) {
            titleTextView.text = audioTitle
        }

        val artistName = mediaMetadata.artist
            ?: context.getString(R.string.artist_unknown)
        if (artistNameTextView.text != artistName) {
            artistNameTextView.text = artistName
        }

        val albumTitle = mediaMetadata.albumTitle
            ?: context.getString(R.string.album_unknown)
        if (albumTitleTextView.text != albumTitle) {
            albumTitleTextView.text = albumTitle
        }
    }

    fun updateMediaArtwork(artwork: Thumbnail?) {
        thumbnailImageView.loadThumbnail(artwork, placeholder = R.drawable.ic_music_note)
    }

    fun updateDurationCurrentPositionMs(durationMs: Long?, currentPositionMs: Long?) {
        val currentPositionSecs = currentPositionMs?.let { it / 1000 }?.toInt() ?: 0
        val durationSecs = durationMs?.let { it / 1000 }?.toInt()?.takeIf { it != 0 } ?: 1

        circularProgressIndicator.max = durationSecs
        circularProgressIndicator.setProgressCompat(currentPositionSecs, true)
    }
}
