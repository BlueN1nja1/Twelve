/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.FlowResult
import org.lineageos.twelve.models.FlowResult.Companion.asFlowResult
import org.lineageos.twelve.models.FlowResult.Companion.foldLatest
import org.lineageos.twelve.models.FlowResult.Companion.getOrNull
import org.lineageos.twelve.models.Playlist

class PlaylistViewModel(application: Application) : TwelveViewModel(application) {
    private val playlistUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist = playlistUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.playlist(it)
        }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading()
        )

    val playlistMetadataCanBeEdited = playlist
        .foldLatest(
            onSuccess = { it.first.type == Playlist.Type.PLAYLIST },
            onError = { _, _ -> false },
        )
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    fun loadPlaylist(playlistUri: Uri) {
        this.playlistUri.value = playlistUri
    }

    suspend fun renamePlaylist(name: String) {
        playlistUri.value?.let { playlistUri ->
            withContext(Dispatchers.IO) {
                mediaRepository.renamePlaylist(playlistUri, name)
            }
        }
    }

    suspend fun deletePlaylist() {
        playlistUri.value?.let { playlistUri ->
            withContext(Dispatchers.IO) {
                mediaRepository.deletePlaylist(playlistUri)
            }
        }
    }

    fun playPlaylist(position: Int = 0) {
        playlist.value.getOrNull()?.second?.takeUnless {
            it.isEmpty()
        }?.let {
            playAudio(it, position)
        }
    }

    fun shufflePlayPlaylist() {
        playlist.value.getOrNull()?.second?.takeUnless {
            it.isEmpty()
        }?.let {
            playAudio(it.shuffled(), 0)
        }
    }
}
