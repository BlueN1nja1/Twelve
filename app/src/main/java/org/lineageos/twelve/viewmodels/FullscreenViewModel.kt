/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.FULLSCREEN_MODE_KEY
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.fullscreenMode
import org.lineageos.twelve.ext.preferenceFlow

/**
 * View model for the main activity.
 */
class FullscreenViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)!!
    }

    val fullscreenMode = sharedPreferences.preferenceFlow(
        FULLSCREEN_MODE_KEY,
        getter = SharedPreferences::fullscreenMode,
    )
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )
}
