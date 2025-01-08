/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.visualizer

import android.media.audiofx.Visualizer
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.bogerchan.niervisualizer.NierVisualizerManager

/**
 * A somewhat coroutine-friendly implementation of [NierVisualizerManager.NVDataSource].
 * Set the audio session ID with [setAudioSessionId] and collect [workFlow] to let
 * the [Visualizer] do its thing.
 */
class VisualizerNVDataSource : NierVisualizerManager.NVDataSource, DefaultLifecycleObserver {
    private val audioSessionId = MutableStateFlow<Int?>(null)

    private var visualizer: Visualizer? = null

    private val fftBuffer = ByteArray(CAPTURE_SIZE)
    private val waveBuffer = ByteArray(CAPTURE_SIZE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val workFlow = audioSessionId
        .filterNotNull()
        .flatMapLatest { audioSessionId ->
            callbackFlow<Unit> {
                val visualizer = Visualizer(audioSessionId).apply {
                    enabled = false
                    captureSize = CAPTURE_SIZE
                    try {
                        scalingMode = Visualizer.SCALING_MODE_NORMALIZED
                    } catch (e: NoSuchMethodError) {
                        Log.e(LOG_TAG, "Can't set scaling mode", e)
                    }
                    measurementMode = Visualizer.MEASUREMENT_MODE_NONE
                }
                require(visualizer.captureSize == CAPTURE_SIZE) {
                    "Capture size mismatch: ${visualizer.captureSize} != $CAPTURE_SIZE"
                }
                visualizer.enabled = true
                this@VisualizerNVDataSource.visualizer = visualizer

                awaitClose {
                    this@VisualizerNVDataSource.visualizer = null
                    visualizer.enabled = false
                    visualizer.release()
                }
            }
        }

    override fun fetchFftData() = fftBuffer.takeIf {
        visualizer?.getFft(it) == Visualizer.SUCCESS
    }

    override fun fetchWaveData() = waveBuffer.takeIf {
        visualizer?.getWaveForm(it) == Visualizer.SUCCESS
    }

    override fun getDataLength() = CAPTURE_SIZE

    override fun getDataSamplingInterval() = (1000L / Visualizer.getMaxCaptureRate())

    fun setAudioSessionId(audioSessionId: Int?) {
        this.audioSessionId.value = audioSessionId
    }

    companion object {
        private val LOG_TAG = VisualizerNVDataSource::class.simpleName!!

        private const val CAPTURE_SIZE = 512
    }
}
