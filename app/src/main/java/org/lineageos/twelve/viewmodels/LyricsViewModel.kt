/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lineageos.twelve.models.Lyrics

class LyricsViewModel(application: Application) : NowPlayingViewModel(application) {
    private val _positionSynced = MutableStateFlow(true)
    val positionSynced = _positionSynced.asStateFlow()

    fun setPositionSynced(outOfSync: Boolean) {
        _positionSynced.value = outOfSync
    }

    fun seekToLine(line: Lyrics.Line) {
        mediaController.value?.apply {
            line.durationMs?.let { durationMs ->
                seekTo(durationMs.first)

                // Since the user selected a line, sync to it
                _positionSynced.value = true
            }
        }
    }
}
