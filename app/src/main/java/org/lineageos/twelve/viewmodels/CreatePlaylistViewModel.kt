/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.Provider
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.Result

class CreatePlaylistViewModel(application: Application) : TwelveViewModel(application) {
    private val providerIdentifier = MutableStateFlow<ProviderIdentifier?>(null)

    private val playlistName = MutableStateFlow("")

    val providersWithSelection = combine(
        providersRepository.allProviders,
        providerIdentifier
    ) { allProviders, providerIdentifier ->
        allProviders to providerIdentifier?.let {
            allProviders.indexOfFirst { provider ->
                provider.type == it.type && provider.typeId == it.typeId
            }.takeIf { it != -1 }
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            listOf<Provider>() to null
        )

    fun setProviderIdentifier(providerIdentifier: ProviderIdentifier?) {
        this.providerIdentifier.value = providerIdentifier
    }

    fun setProviderPosition(position: Int) = setProviderIdentifier(
        providersWithSelection.value.first[position]
    )

    fun getPlaylistName() = playlistName.value

    fun setPlaylistName(playlistName: String) {
        this.playlistName.value = playlistName
    }

    fun isPlaylistNameEmpty() = playlistName.value.isEmpty()

    suspend fun createPlaylist() = providerIdentifier.value?.let {
        withContext(Dispatchers.IO) {
            mediaRepository.createPlaylist(it, playlistName.value)
        }
    } ?: Result.Error(Error.IO)
}
