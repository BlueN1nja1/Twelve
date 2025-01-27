/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import org.lineageos.twelve.services.PlaybackService
import org.lineageos.twelve.services.PlaybackService.CustomCommand.Companion.sendCustomCommand

class SettingsViewModel(application: Application) : TwelveViewModel(application) {
    @OptIn(UnstableApi::class)
    suspend fun toggleOffload(offload: Boolean) {
        withMediaController {
            sendCustomCommand(
                PlaybackService.CustomCommand.TOGGLE_OFFLOAD,
                bundleOf(
                    PlaybackService.CustomCommand.ARG_VALUE to offload
                )
            )
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleSkipSilence(skipSilence: Boolean) {
        withMediaController {
            sendCustomCommand(
                PlaybackService.CustomCommand.TOGGLE_SKIP_SILENCE,
                bundleOf(
                    PlaybackService.CustomCommand.ARG_VALUE to skipSilence
                )
            )
        }
    }

    suspend fun resetLocalStats() {
        mediaRepository.resetLocalStats()
    }

    private suspend fun withMediaController(block: suspend MediaController.() -> Unit) {
        mediaController.value?.let {
            block(it)
        }
    }
}
