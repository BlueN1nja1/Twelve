/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import com.bluen1nja1.twelve.models.FlowResult
import com.bluen1nja1.twelve.models.FlowResult.Companion.asFlowResult

class ActivityViewModel(application: Application) : TwelveViewModel(application) {
    val activity = mediaRepository.activity()
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading(),
        )
}
