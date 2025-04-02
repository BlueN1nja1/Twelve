/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.media.MediaScannerConnection
import android.os.storage.StorageManager
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.services.PlaybackService
import org.lineageos.twelve.services.PlaybackService.CustomCommand.Companion.sendCustomCommand

class SettingsViewModel(application: Application) : TwelveViewModel(application) {
    // System services
    private val storageManager by lazy {
        applicationContext.getSystemService(StorageManager::class.java)
    }

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

    fun rescanMediaStore() {
        MediaScannerConnection.scanFile(
            applicationContext,
            storageManager.storageVolumes.mapNotNull { it.directory?.absolutePath }.toTypedArray(),
            null,
            null,
        )
    }

    private suspend fun withMediaController(block: suspend MediaController.() -> Unit) {
        mediaController.value?.let {
            block(it)
        }
    }
}
