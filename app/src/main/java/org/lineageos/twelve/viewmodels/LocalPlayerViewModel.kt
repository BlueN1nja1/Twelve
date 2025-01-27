/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.availableCommandsFlow
import org.lineageos.twelve.ext.isPlayingFlow
import org.lineageos.twelve.ext.mediaMetadataFlow
import org.lineageos.twelve.ext.next
import org.lineageos.twelve.ext.playbackParametersFlow
import org.lineageos.twelve.ext.playbackProgressFlow
import org.lineageos.twelve.ext.playbackStateFlow
import org.lineageos.twelve.ext.repeatModeFlow
import org.lineageos.twelve.ext.shuffleModeEnabled
import org.lineageos.twelve.ext.shuffleModeFlow
import org.lineageos.twelve.ext.toThumbnail
import org.lineageos.twelve.ext.typedRepeatMode
import org.lineageos.twelve.models.PlaybackProgress
import org.lineageos.twelve.models.PlaybackState
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.RequestStatus

/**
 * A view model useful to playback stuff locally (not in the playback service).
 */
class LocalPlayerViewModel(application: Application) : AndroidViewModel(application) {
    enum class PlaybackSpeed(val value: Float) {
        ONE(1f),
        ONE_POINT_FIVE(1.5f),
        TWO(2f),
        ZERO_POINT_FIVE(0.5f);

        companion object {
            fun fromValue(value: Float) = entries.firstOrNull {
                it.value == value
            }
        }
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application)
    }

    // ExoPlayer
    private val exoPlayer = ExoPlayer.Builder(applicationContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val playbackState = exoPlayer.playbackStateFlow()
        .flowOn(Dispatchers.Main)

    val mediaMetadata = exoPlayer.mediaMetadataFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MediaMetadata.EMPTY
        )

    val isPlaying = exoPlayer.isPlayingFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val shuffleMode = exoPlayer.shuffleModeFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val repeatMode = exoPlayer.repeatModeFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RepeatMode.NONE
        )

    val mediaArtwork = combine(
        mediaMetadata,
        playbackState,
    ) { mediaMetadata, playbackState ->
        when (playbackState) {
            PlaybackState.BUFFERING -> RequestStatus.Loading()
            else -> RequestStatus.Success<_, Nothing>(mediaMetadata.toThumbnail(applicationContext))
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading()
        )

    val playbackProgress = exoPlayer.playbackProgressFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PlaybackProgress.EMPTY
        )

    val playbackParameters = exoPlayer.playbackParametersFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PlaybackParameters.DEFAULT
        )

    val availableCommands = exoPlayer.availableCommandsFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Player.Commands.EMPTY
        )

    override fun onCleared() {
        exoPlayer.release()

        super.onCleared()
    }

    fun setMediaUris(uris: Iterable<Uri>) {
        exoPlayer.apply {
            // Initialize shuffle and repeat modes
            typedRepeatMode = sharedPreferences.typedRepeatMode
            shuffleModeEnabled = sharedPreferences.shuffleModeEnabled

            setMediaItems(
                uris.map {
                    MediaItem.fromUri(it)
                }
            )
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        exoPlayer.apply {
            if (playWhenReady) {
                pause()
            } else {
                play()
            }
        }
    }

    fun shufflePlaybackSpeed() {
        val playbackSpeed = PlaybackSpeed.fromValue(
            exoPlayer.playbackParameters.speed
        ) ?: PlaybackSpeed.ONE

        exoPlayer.setPlaybackSpeed(playbackSpeed.next().value)
    }

    fun seekToPosition(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun toggleShuffleMode() {
        exoPlayer.shuffleModeEnabled = exoPlayer.shuffleModeEnabled.not()
        sharedPreferences.shuffleModeEnabled = exoPlayer.shuffleModeEnabled
    }

    fun toggleRepeatMode() {
        exoPlayer.typedRepeatMode = exoPlayer.typedRepeatMode.next()
        sharedPreferences.typedRepeatMode = exoPlayer.typedRepeatMode
    }

    fun seekToPrevious() {
        exoPlayer.apply {
            val currentMediaItemIndex = currentMediaItemIndex
            seekToPrevious()
            if (this.currentMediaItemIndex < currentMediaItemIndex) {
                play()
            }
        }
    }

    fun seekToNext() {
        exoPlayer.apply {
            seekToNext()
            play()
        }
    }
}
