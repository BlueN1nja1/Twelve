/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import me.bogerchan.niervisualizer.renderer.IRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleRenderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType2Renderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType3Renderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType4Renderer
import me.bogerchan.niervisualizer.renderer.line.LineRenderer
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.availableCommandsFlow
import org.lineageos.twelve.ext.isPlayingFlow
import org.lineageos.twelve.ext.mediaItemFlow
import org.lineageos.twelve.ext.mediaMetadataFlow
import org.lineageos.twelve.ext.next
import org.lineageos.twelve.ext.playbackParametersFlow
import org.lineageos.twelve.ext.playbackProgressFlow
import org.lineageos.twelve.ext.playbackStateFlow
import org.lineageos.twelve.ext.repeatModeFlow
import org.lineageos.twelve.ext.shuffleModeFlow
import org.lineageos.twelve.ext.toThumbnail
import org.lineageos.twelve.ext.tracksFlow
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.PlaybackProgress
import org.lineageos.twelve.models.PlaybackState
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.services.PlaybackService
import org.lineageos.twelve.services.PlaybackService.CustomCommand.Companion.sendCustomCommand
import org.lineageos.twelve.utils.MimeUtils

open class NowPlayingViewModel(application: Application) : TwelveViewModel(application) {
    enum class VisualizerType(val factory: () -> Array<IRenderer>?) {
        NONE({ null }),
        TYPE_1({ arrayOf(ColumnarType1Renderer()) }),
        TYPE_2({ arrayOf(ColumnarType2Renderer()) }),
        TYPE_3({ arrayOf(ColumnarType3Renderer()) }),
        TYPE_4({ arrayOf(ColumnarType4Renderer()) }),
        LINE({ arrayOf(LineRenderer(true)) }),
        CIRCLE_BAR({ arrayOf(CircleBarRenderer()) }),
        CIRCLE({ arrayOf(CircleRenderer(true)) }),
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaMetadata = mediaController
        .filterNotNull()
        .flatMapLatest { it.mediaMetadataFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MediaMetadata.EMPTY
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaItem = mediaController
        .filterNotNull()
        .flatMapLatest { it.mediaItemFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val audio = mediaItem
        .filterNotNull()
        .flatMapLatest {
            val audioUri = Uri.parse(it.mediaId.removePrefix(Audio.AUDIO_MEDIA_ITEM_ID_PREFIX))
            mediaRepository.audio(audioUri)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackState = mediaController
        .filterNotNull()
        .flatMapLatest { it.playbackStateFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val isPlaying = mediaController
        .filterNotNull()
        .flatMapLatest { it.isPlayingFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val shuffleMode = mediaController
        .filterNotNull()
        .flatMapLatest { it.shuffleModeFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val repeatMode = mediaController
        .filterNotNull()
        .flatMapLatest { it.repeatModeFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RepeatMode.NONE
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackParameters = mediaController
        .filterNotNull()
        .flatMapLatest { it.playbackParametersFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
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

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTrackFormat = mediaController
        .filterNotNull()
        .flatMapLatest { it.tracksFlow() }
        .flowOn(Dispatchers.Main)
        .mapLatest { tracks ->
            val groups = tracks.groups.filter { group ->
                group.type == C.TRACK_TYPE_AUDIO && group.isSelected
            }

            require(groups.size <= 1) { "More than one audio track selected" }

            groups.firstOrNull()?.let { group ->
                (0..group.length).firstNotNullOfOrNull { i ->
                    when (group.isTrackSelected(i)) {
                        true -> group.getTrackFormat(i)
                        false -> null
                    }
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val mimeType = combine(currentTrackFormat, mediaItem) { format, mediaItem ->
        format?.sampleMimeType
            ?: format?.containerMimeType
            ?: mediaItem?.localConfiguration?.mimeType
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    val displayFileType = mimeType
        .mapLatest { mimeType ->
            mimeType?.let {
                MimeUtils.mimeTypeToDisplayName(it)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableCommands = mediaController
        .filterNotNull()
        .flatMapLatest { it.availableCommandsFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val durationCurrentPositionMs = mediaController
        .filterNotNull()
        .flatMapLatest { mediaController ->
            flow {
                while (true) {
                    val duration = mediaController.duration.takeIf { it != C.TIME_UNSET }
                    emit(duration to duration?.let { mediaController.currentPosition })
                    delay(200)
                }
            }
        }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null to null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackProgress = mediaController
        .filterNotNull()
        .flatMapLatest { it.playbackProgressFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PlaybackProgress.EMPTY
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val audioSessionId = mediaController
        .filterNotNull()
        .mapLatest { mediaController ->
            mediaController.sendCustomCommand(
                PlaybackService.CustomCommand.GET_AUDIO_SESSION_ID,
                Bundle.EMPTY
            ).await().extras.getInt(PlaybackService.CustomCommand.RSP_VALUE)
        }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    private val _currentVisualizerType = MutableStateFlow(VisualizerType.entries.first())
    val currentVisualizerType = combine(
        _currentVisualizerType,
        isPlaying,
    ) { currentVisualizerType, isPlaying ->
        currentVisualizerType.takeIf { isPlaying } ?: VisualizerType.NONE
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = VisualizerType.NONE
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val isVisualizerEnabled = currentVisualizerType
        .mapLatest { it != VisualizerType.NONE }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    fun togglePlayPause() {
        mediaController.value?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        mediaController.value?.seekTo(positionMs)
    }

    fun seekToPrevious() {
        mediaController.value?.let {
            val currentMediaItemIndex = it.currentMediaItemIndex
            it.seekToPrevious()
            if (it.currentMediaItemIndex < currentMediaItemIndex) {
                it.play()
            }
        }
    }

    fun seekToNext() {
        mediaController.value?.let {
            it.seekToNext()
            it.play()
        }
    }

    fun toggleShuffleMode() {
        shuffleModeEnabled = shuffleModeEnabled.not()
    }

    fun toggleRepeatMode() {
        typedRepeatMode = typedRepeatMode.next()
    }

    fun nextVisualizerType() {
        _currentVisualizerType.value = _currentVisualizerType.value.next()
    }
}
