/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.channels.awaitClose
import org.lineageos.twelve.models.PlaybackProgress
import org.lineageos.twelve.models.PlaybackState
import org.lineageos.twelve.models.QueueItem
import org.lineageos.twelve.models.RepeatMode

@OptIn(UnstableApi::class)
fun Player.mediaMetadataFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            trySend(mediaMetadata)
        }
    }

    addListener(listener)
    trySend(mediaMetadata)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.mediaItemFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            trySend(mediaItem)
        }
    }

    addListener(listener)
    trySend(currentMediaItem)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.playbackStateFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            trySend(typedPlaybackState)
        }
    }

    addListener(listener)
    trySend(typedPlaybackState)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.isPlayingFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            trySend(isPlaying)
        }
    }

    addListener(listener)
    trySend(isPlaying)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.shuffleModeFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            trySend(shuffleModeEnabled)
        }
    }

    addListener(listener)
    trySend(shuffleModeEnabled)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.repeatModeFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onRepeatModeChanged(repeatMode: Int) {
            trySend(typedRepeatMode)
        }
    }

    addListener(listener)
    trySend(typedRepeatMode)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.playbackParametersFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            trySend(playbackParameters)
        }
    }

    addListener(listener)
    trySend(playbackParameters)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.availableCommandsFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            trySend(availableCommands)
        }
    }

    addListener(listener)
    trySend(availableCommands)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.tracksFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            trySend(tracks)
        }
    }

    addListener(listener)
    trySend(currentTracks)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.queueFlow() = conflatedCallbackFlow {
    val emitQueue = {
        val currentMediaItemIndex = currentMediaItemIndex

        trySend(
            mediaItems.mapIndexed { index, mediaItem ->
                QueueItem(mediaItem, index == currentMediaItemIndex)
            }
        )
    }

    val listener = object : Player.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            emitQueue()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            emitQueue()
        }
    }

    addListener(listener)
    emitQueue()

    awaitClose {
        removeListener(listener)
    }
}

fun Player.playbackProgressFlow() = conflatedCallbackFlow {
    val emitPlaybackProgress = {
        val durationMs = duration.takeIf { it != C.TIME_UNSET }

        trySend(
            PlaybackProgress(
                isPlaying = isPlaying,
                durationMs = durationMs,
                currentPositionMs = currentPosition
                    .takeIf { durationMs != null }
                    ?.coerceAtMost(durationMs ?: 0),
                playbackSpeed = playbackParameters.speed,
            )
        )
    }

    val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            when {
                events.containsAny(
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED,
                ) -> emitPlaybackProgress()
            }
        }
    }

    addListener(listener)
    emitPlaybackProgress()

    awaitClose {
        removeListener(listener)
    }
}

var Player.typedRepeatMode: RepeatMode
    get() = when (repeatMode) {
        Player.REPEAT_MODE_OFF -> RepeatMode.NONE
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> throw Exception("Unknown repeat mode")
    }
    set(value) {
        repeatMode = when (value) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

val Player.typedPlaybackState: PlaybackState
    get() = when (playbackState) {
        Player.STATE_IDLE -> PlaybackState.IDLE
        Player.STATE_BUFFERING -> PlaybackState.BUFFERING
        Player.STATE_READY -> PlaybackState.READY
        Player.STATE_ENDED -> PlaybackState.ENDED
        else -> throw Exception("Unknown playback state")
    }

val Player.mediaItems: List<MediaItem>
    get() = (0 until mediaItemCount).map {
        getMediaItemAt(it)
    }

@OptIn(UnstableApi::class)
fun Player.setOffloadEnabled(enabled: Boolean) {
    trackSelectionParameters = trackSelectionParameters.buildUpon()
        .setAudioOffloadPreferences(
            TrackSelectionParameters.AudioOffloadPreferences
                .Builder()
                .setAudioOffloadMode(
                    if (enabled) {
                        TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
                    } else {
                        TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
                    }
                )
                .build()
        ).build()
}
