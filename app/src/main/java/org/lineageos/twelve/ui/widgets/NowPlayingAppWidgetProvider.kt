/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.widgets

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.guava.await
import org.lineageos.twelve.MainActivity
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.typedPlaybackState
import org.lineageos.twelve.models.PlaybackState
import org.lineageos.twelve.services.PlaybackService

class NowPlayingAppWidgetProvider : BaseAppWidgetProvider<NowPlayingAppWidgetProvider>(Companion) {
    companion object : AppWidgetUpdater<NowPlayingAppWidgetProvider>(
        NowPlayingAppWidgetProvider::class,
        R.layout.app_widget_now_playing,
    ) {
        override suspend fun RemoteViews.update(context: Context) {
            withMediaController(context) { mediaController ->
                val mediaMetadata = mediaController.mediaMetadata

                val openNowPlayingPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        putExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, true)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                setOnClickPendingIntent(R.id.linearLayout, openNowPlayingPendingIntent)

                setTextViewText(R.id.titleTextView, mediaMetadata.title ?: "")
                setTextViewText(R.id.artistNameTextView, mediaMetadata.artist ?: "")

                setOnClickPendingIntent(
                    R.id.playPauseImageButton,
                    PendingIntent.getForegroundService(
                        context,
                        0,
                        Intent(context, PlaybackService::class.java).apply {
                            action = PlaybackService.ACTION_TOGGLE_PLAY_PAUSE
                        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                setImageViewResource(
                    R.id.playPauseImageButton,
                    when (mediaController.playWhenReady) {
                        true -> R.drawable.ic_pause
                        false -> R.drawable.ic_play_arrow
                    }
                )

                setViewVisibility(
                    R.id.bufferingProgressBar, when (mediaController.typedPlaybackState) {
                        PlaybackState.BUFFERING -> View.VISIBLE
                        else -> View.GONE
                    }
                )

                when (mediaController.typedPlaybackState) {
                    PlaybackState.BUFFERING -> {
                        // Do nothing
                    }

                    else -> mediaMetadata.artworkData?.let {
                        fetchImage(context, it, R.id.thumbnailImageView)
                    } ?: mediaMetadata.artworkUri?.let {
                        fetchImage(context, it, R.id.thumbnailImageView)
                    } ?: run {
                        setImageViewResource(R.id.thumbnailImageView, R.drawable.ic_music_note)
                    }
                }
            }
        }

        private suspend fun withMediaController(
            context: Context,
            block: suspend (MediaController) -> Unit,
        ) {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, PlaybackService::class.java)
            )

            val mediaController = MediaController.Builder(
                context.applicationContext,
                sessionToken
            )
                .buildAsync()
                .await()

            block(mediaController)

            mediaController.release()
        }
    }
}
