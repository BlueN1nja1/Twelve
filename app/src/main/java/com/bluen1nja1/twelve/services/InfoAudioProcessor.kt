/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.services

import androidx.annotation.OptIn
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer

/**
 * [AudioProcessor] that does nothing other than exposing the [AudioProcessor.AudioFormat].
 * Here the input is the output.
 */
@OptIn(UnstableApi::class)
class InfoAudioProcessor : BaseAudioProcessor() {
    private var audioFormat = AudioProcessor.AudioFormat.NOT_SET
        set(value) {
            field = value
            _audioFormatFlow.value = value.takeIf { it != AudioProcessor.AudioFormat.NOT_SET }
        }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat) =
        super.onConfigure(inputAudioFormat).also {
            audioFormat = inputAudioFormat
        }

    override fun queueInput(inputBuffer: ByteBuffer) {
        error("Should not be called")
    }

    override fun onReset() {
        audioFormat = AudioProcessor.AudioFormat.NOT_SET
    }

    companion object {
        private val _audioFormatFlow = MutableStateFlow<AudioProcessor.AudioFormat?>(null)
        val audioFormatFlow = _audioFormatFlow.asStateFlow()
    }
}
