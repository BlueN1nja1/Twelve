/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.models.AudioOutputMode
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.viewmodels.NowPlayingStatsViewModel
import java.util.Locale

/**
 * A fragment showing playback statistics for nerds and audiophiles thinking that audio files
 * with a sample rate higher than 48 kHz is better.
 */
class NowPlayingStatsDialogFragment : DialogFragment(R.layout.fragment_now_playing_stats_dialog) {
    // View models
    private val viewModel by viewModels<NowPlayingStatsViewModel>()

    // Views
    private val outputChannelCountListItem by getViewProperty<ListItem>(R.id.outputChannelCountListItem)
    private val outputEncodingListItem by getViewProperty<ListItem>(R.id.outputEncodingListItem)
    private val outputHeaderListItem by getViewProperty<ListItem>(R.id.outputHeaderListItem)
    private val outputItemsLinearLayout by getViewProperty<LinearLayout>(R.id.outputItemsLinearLayout)
    private val outputSampleRateListItem by getViewProperty<ListItem>(R.id.outputSampleRateListItem)
    private val sourceChannelCountListItem by getViewProperty<ListItem>(R.id.sourceChannelCountListItem)
    private val sourceEncodingListItem by getViewProperty<ListItem>(R.id.sourceEncodingListItem)
    private val sourceFileTypeListItem by getViewProperty<ListItem>(R.id.sourceFileTypeListItem)
    private val sourceSampleRateListItem by getViewProperty<ListItem>(R.id.sourceSampleRateListItem)
    private val transcodingBitrateListItem by getViewProperty<ListItem>(R.id.transcodingBitrateListItem)
    private val transcodingEncodingListItem by getViewProperty<ListItem>(R.id.transcodingEncodingListItem)
    private val transcodingFloatModeEnabledListItem by getViewProperty<ListItem>(R.id.transcodingFloatModeEnabledListItem)
    private val transcodingOutputModeListItem by getViewProperty<ListItem>(R.id.transcodingOutputModeListItem)

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(
        requireContext()
    ).show()!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mimeType.collectLatest {
                        it?.let {
                            sourceFileTypeListItem.supportingText = it
                        } ?: sourceFileTypeListItem.setSupportingText(
                            R.string.audio_file_type_unknown
                        )
                    }
                }

                launch {
                    viewModel.sourceAudioStreamInformation.collectLatest {
                        it?.sampleRate?.also { sampleRate ->
                            sourceSampleRateListItem.setSupportingText(
                                R.string.audio_sample_rate_format,
                                decimalFormatter.format(sampleRate.toFloat() / 1000)
                            )
                        } ?: sourceSampleRateListItem.setSupportingText(
                            R.string.audio_sample_rate_unknown
                        )

                        it?.channelCount?.let { channelCount ->
                            sourceChannelCountListItem.supportingText = channelCount.toString()
                        } ?: sourceChannelCountListItem.setSupportingText(
                            R.string.audio_channel_count_unknown
                        )

                        it?.encoding?.also { encoding ->
                            sourceEncodingListItem.supportingText = encoding.displayName
                        } ?: sourceEncodingListItem.setSupportingText(
                            R.string.audio_encoding_unknown
                        )
                    }
                }

                launch {
                    viewModel.transcodingFloatModeEnabled.collectLatest {
                        transcodingFloatModeEnabledListItem.setSupportingText(
                            when (it) {
                                true -> R.string.audio_float_mode_enabled
                                false -> R.string.audio_float_mode_disabled
                                null -> R.string.audio_float_mode_unknown
                            }
                        )
                    }
                }

                launch {
                    viewModel.transcodingEncoding.collectLatest {
                        it?.let {
                            transcodingEncodingListItem.supportingText = it.displayName
                        } ?: transcodingEncodingListItem.setSupportingText(
                            R.string.audio_encoding_unknown
                        )
                    }
                }

                launch {
                    viewModel.transcodingOutputMode.collectLatest {
                        transcodingOutputModeListItem.setSupportingText(
                            when (it) {
                                AudioOutputMode.PCM -> R.string.audio_output_mode_pcm
                                AudioOutputMode.OFFLOAD -> R.string.audio_output_mode_offload
                                AudioOutputMode.PASSTHROUGH -> R.string.audio_output_mode_passthrough
                                null -> R.string.audio_output_mode_unknown
                            }
                        )
                    }
                }

                launch {
                    viewModel.transcodingBitrate.collectLatest {
                        it?.let {
                            transcodingBitrateListItem.setSupportingText(
                                R.string.audio_bitrate_format,
                                decimalFormatter.format(it.toFloat() / 1000)
                            )
                        } ?: transcodingBitrateListItem.setSupportingText(
                            R.string.audio_bitrate_unknown
                        )
                    }
                }

                launch {
                    viewModel.hasOutputInformation.collectLatest {
                        when (it) {
                            false -> outputHeaderListItem.setSupportingText(
                                R.string.audio_output_not_available_in_current_configuration
                            )

                            else -> outputHeaderListItem.supportingText = null
                        }

                        outputItemsLinearLayout.isVisible = it != false
                    }
                }

                launch {
                    viewModel.outputAudioStreamInformation.collectLatest {
                        it?.sampleRate?.also { sampleRate ->
                            outputSampleRateListItem.setSupportingText(
                                R.string.audio_sample_rate_format,
                                decimalFormatter.format(sampleRate.toFloat() / 1000)
                            )
                        } ?: outputSampleRateListItem.setSupportingText(
                            R.string.audio_sample_rate_unknown
                        )

                        it?.channelCount?.let { channelCount ->
                            outputChannelCountListItem.supportingText = channelCount.toString()
                        } ?: outputChannelCountListItem.setSupportingText(
                            R.string.audio_channel_count_unknown
                        )

                        it?.encoding?.let { encoding ->
                            outputEncodingListItem.supportingText = encoding.displayName
                        } ?: outputEncodingListItem.setSupportingText(
                            R.string.audio_encoding_unknown
                        )
                    }
                }
            }
        }
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)

        private val decimalFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
