/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.RequestStatus.Companion.map

class MediaItemViewModel(application: Application) : TwelveViewModel(application) {
    private val uri = MutableStateFlow<Uri?>(null)
    private val mediaType = MutableStateFlow<MediaType?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data = combine(
        uri.filterNotNull(),
        mediaType.filterNotNull(),
    ) { uri, mediaType ->
        when (mediaType) {
            MediaType.ALBUM -> mediaRepository.album(uri)
            MediaType.ARTIST -> mediaRepository.artist(uri).mapLatest {
                it.map { album -> album.first to listOf() }
            }

            MediaType.AUDIO -> mediaRepository.audio(uri).mapLatest {
                it.map { audio -> audio to listOf(audio) }
            }

            MediaType.GENRE -> mediaRepository.genre(uri).mapLatest {
                it.map { genre -> genre.first to listOf() }
            }

            MediaType.PLAYLIST -> mediaRepository.playlist(uri)
        }
    }
        .flatMapLatest { it }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawMediaItem = data
        .mapLatest {
            when (it) {
                is RequestStatus.Success -> it.data.first
                else -> null
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaItem = data
        .mapLatest { data -> data.map { it.first } }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tracks = data
        .mapLatest {
            when (it) {
                is RequestStatus.Loading -> null
                is RequestStatus.Success -> it.data.second
                is RequestStatus.Error -> listOf()
            }
        }
        .filterNotNull()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = listOf(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val albumUri = rawMediaItem
        .mapLatest {
            when (it) {
                is Audio -> it.albumUri
                else -> null
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistUri = rawMediaItem
        .mapLatest {
            when (it) {
                is Audio -> it.artistUri
                is Album -> it.artistUri
                else -> null
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val genreUri = rawMediaItem
        .mapLatest {
            when (it) {
                is Audio -> it.genreUri
                else -> null
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    fun loadMediaItem(uri: Uri, mediaType: MediaType) {
        this.uri.value = uri
        this.mediaType.value = mediaType
    }

    fun addToQueue(vararg audios: Audio) {
        mediaController.value?.apply {
            addMediaItems(audios.map { it.toMedia3MediaItem(applicationContext) })

            // If the added items are the only one, play them
            if (mediaItemCount == audios.count()) {
                play()
            }
        }
    }

    fun playNext(vararg audios: Audio) {
        mediaController.value?.apply {
            addMediaItems(
                currentMediaItemIndex + 1,
                audios.map { it.toMedia3MediaItem(applicationContext) }
            )

            // If the added items are the only one, play them
            if (mediaItemCount == audios.count()) {
                play()
            }
        }
    }

    suspend fun removeAudioFromPlaylist(playlistUri: Uri) {
        uri.value?.takeIf { mediaType.value == MediaType.AUDIO }?.let {
            withContext(Dispatchers.IO) {
                mediaRepository.removeAudioFromPlaylist(playlistUri, it)
            }
        }
    }
}
