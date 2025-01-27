/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.ALBUMS_SORTING_REVERSE_KEY
import org.lineageos.twelve.ext.ALBUMS_SORTING_STRATEGY_KEY
import org.lineageos.twelve.ext.albumsSortingRule
import org.lineageos.twelve.ext.preferenceFlow
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule

class AlbumsViewModel(application: Application) : TwelveViewModel(application) {
    val sortingRule = sharedPreferences.preferenceFlow(
        ALBUMS_SORTING_STRATEGY_KEY,
        ALBUMS_SORTING_REVERSE_KEY,
        getter = SharedPreferences::albumsSortingRule,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val albums = sortingRule
        .flatMapLatest { mediaRepository.albums(it) }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    fun setSortingRule(sortingRule: SortingRule) {
        sharedPreferences.albumsSortingRule = sortingRule
    }
}
