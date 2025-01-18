/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.RequestStatus.Companion.fold

class ProviderViewModel(application: Application) : TwelveViewModel(application) {
    /**
     * The provider identifiers to manage.
     */
    private val providerIds = MutableStateFlow<Pair<ProviderType, Long>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val provider = providerIds
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.provider(it.first, it.second)
        }
        .mapLatest { provider ->
            provider?.let {
                RequestStatus.Success<_, MediaError>(it)
            } ?: RequestStatus.Error(MediaError.NOT_FOUND)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val canBeManaged = provider
        .mapLatest {
            when (it) {
                is RequestStatus.Loading -> null
                is RequestStatus.Success -> it.data.type != ProviderType.LOCAL
                is RequestStatus.Error -> false
            }
        }
        .filterNotNull()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val status = provider
        .flatMapLatest { provider ->
            provider.fold(
                onLoading = {
                    flowOf(RequestStatus.Loading(it))
                },
                onSuccess = {
                    mediaRepository.status(it)
                },
                onError = {
                    flowOf(RequestStatus.Error(it))
                }
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    fun setProviderIds(providerIds: Pair<ProviderType, Long>?) {
        this.providerIds.value = providerIds
    }

    /**
     * Delete the provider.
     */
    suspend fun deleteProvider() {
        val (providerType, providerTypeId) = providerIds.value ?: return

        withContext(Dispatchers.IO) {
            mediaRepository.deleteProvider(providerType, providerTypeId)
        }
    }
}
