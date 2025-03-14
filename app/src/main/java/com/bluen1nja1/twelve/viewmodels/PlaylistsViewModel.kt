/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import com.bluen1nja1.twelve.ext.PLAYLISTS_SORTING_REVERSE_KEY
import com.bluen1nja1.twelve.ext.PLAYLISTS_SORTING_STRATEGY_KEY
import com.bluen1nja1.twelve.ext.playlistsSortingRule
import com.bluen1nja1.twelve.ext.preferenceFlow
import com.bluen1nja1.twelve.models.FlowResult
import com.bluen1nja1.twelve.models.FlowResult.Companion.asFlowResult
import com.bluen1nja1.twelve.models.SortingRule

class PlaylistsViewModel(application: Application) : TwelveViewModel(application) {
    val navigationProvider = mediaRepository.navigationProvider

    val sortingRule = sharedPreferences.preferenceFlow(
        PLAYLISTS_SORTING_STRATEGY_KEY,
        PLAYLISTS_SORTING_REVERSE_KEY,
        getter = SharedPreferences::playlistsSortingRule,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists = sortingRule
        .flatMapLatest { mediaRepository.playlists(it) }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading()
        )

    fun setSortingRule(sortingRule: SortingRule) {
        sharedPreferences.playlistsSortingRule = sortingRule
    }
}
