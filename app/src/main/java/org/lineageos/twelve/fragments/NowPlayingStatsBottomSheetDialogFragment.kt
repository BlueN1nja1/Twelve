/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.models.OutputConfiguration
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.viewmodels.NowPlayingViewModel
import java.util.Locale

/**
 * A fragment showing playback statistics for nerds and audiophiles thinking that audio files
 * with a sample rate higher than 48 kHz is better.
 */
class NowPlayingStatsBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_now_playing_stats_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<NowPlayingViewModel>()

    // Views
    private val deviceMaxChannelCountListItem by getViewProperty<ListItem>(R.id.deviceMaxChannelCountListItem)
    private val deviceNameListItem by getViewProperty<ListItem>(R.id.deviceNameListItem)
    private val deviceTypeListItem by getViewProperty<ListItem>(R.id.deviceTypeListItem)
    private val deviceHeaderListItem by getViewProperty<ListItem>(R.id.deviceHeaderListItem)
    private val deviceMaxSampleRateListItem by getViewProperty<ListItem>(R.id.deviceMaxSampleRateListItem)
    private val potentialIssueDownmixingListItem by getViewProperty<ListItem>(R.id.potentialIssueDownmixingListItem)
    private val potentialIssueDownsamplingListItem by getViewProperty<ListItem>(R.id.potentialIssueDownsamplingListItem)
    private val potentialIssuePcmFloatModeDisabledListItem by getViewProperty<ListItem>(R.id.potentialIssuePcmFloatModeDisabledListItem)
    private val potentialIssuePostProcessingLossyCompressionListItem by getViewProperty<ListItem>(R.id.potentialIssuePostProcessingLossyCompressionListItem)
    private val potentialIssuesHeaderListItem by getViewProperty<ListItem>(R.id.potentialIssuesHeaderListItem)
    private val sourceAverageBitrateListItem by getViewProperty<ListItem>(R.id.sourceAverageBitrateListItem)
    private val sourceChannelCountListItem by getViewProperty<ListItem>(R.id.sourceChannelCountListItem)
    private val sourceCompressionListItem by getViewProperty<ListItem>(R.id.sourceCompressionListItem)
    private val sourceEncodingListItem by getViewProperty<ListItem>(R.id.sourceEncodingListItem)
    private val sourceFileTypeListItem by getViewProperty<ListItem>(R.id.sourceFileTypeListItem)
    private val sourcePeakBitrateListItem by getViewProperty<ListItem>(R.id.sourcePeakBitrateListItem)
    private val sourceSampleRateListItem by getViewProperty<ListItem>(R.id.sourceSampleRateListItem)
    private val transcodingBitrateListItem by getViewProperty<ListItem>(R.id.transcodingBitrateListItem)
    private val transcodingChannelCountListItem by getViewProperty<ListItem>(R.id.transcodingChannelCountListItem)
    private val transcodingCompressionListItem by getViewProperty<ListItem>(R.id.transcodingCompressionListItem)
    private val transcodingEncodingListItem by getViewProperty<ListItem>(R.id.transcodingEncodingListItem)
    private val transcodingHeaderListItem by getViewProperty<ListItem>(R.id.transcodingHeaderListItem)
    private val transcodingOutputModeListItem by getViewProperty<ListItem>(R.id.transcodingOutputModeListItem)
    private val transcodingSampleRateListItem by getViewProperty<ListItem>(R.id.transcodingSampleRateListItem)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.outputConfiguration.collectLatest { outputConfiguration ->
                        // Verdict
                        outputConfiguration?.verdict?.potentialIssues.orEmpty().also {
                            potentialIssuesHeaderListItem.isVisible = it.isNotEmpty()

                            potentialIssueDownsamplingListItem.isVisible = it.contains(
                                OutputConfiguration.Verdict.Issue.DOWNSAMPLING
                            )
                            potentialIssuePcmFloatModeDisabledListItem.isVisible = it.contains(
                                OutputConfiguration.Verdict.Issue.PCM_FLOAT_MODE_DISABLED
                            )
                            potentialIssueDownmixingListItem.isVisible = it.contains(
                                OutputConfiguration.Verdict.Issue.DOWNMIXING
                            )
                            potentialIssuePostProcessingLossyCompressionListItem.isVisible =
                                it.contains(
                                    OutputConfiguration.Verdict.Issue.POST_PROCESSING_LOSSY_COMPRESSION
                                )
                        }

                        // Source
                        sourceFileTypeListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.source?.mimeType,
                            R.string.audio_file_type_unknown,
                        )

                        sourceEncodingListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.source?.encoding?.displayName,
                            R.string.audio_encoding_unknown,
                        )

                        sourceCompressionListItem.setSupportingText(
                            when (outputConfiguration?.source?.compression) {
                                OutputConfiguration.Compression.LOSSY ->
                                    R.string.audio_format_compression_lossy

                                OutputConfiguration.Compression.LOSSLESS ->
                                    R.string.audio_format_compression_lossless

                                OutputConfiguration.Compression.UNCOMPRESSED ->
                                    R.string.audio_format_compression_uncompressed

                                null -> R.string.audio_format_compression_unknown
                            }
                        )

                        sourceCompressionListItem.setSupportingText(
                            when (outputConfiguration?.source?.compression) {
                                OutputConfiguration.Compression.LOSSY ->
                                    R.string.audio_format_compression_lossy

                                OutputConfiguration.Compression.LOSSLESS ->
                                    R.string.audio_format_compression_lossless

                                OutputConfiguration.Compression.UNCOMPRESSED ->
                                    R.string.audio_format_compression_uncompressed

                                null -> R.string.audio_format_compression_unknown
                            }
                        )

                        sourceSampleRateListItem.setSupportingTextOrUnknown(
                            R.string.audio_sample_rate_format,
                            outputConfiguration?.source?.sampleRateHz?.let {
                                decimalFormatter.format(it / 1000f)
                            },
                            R.string.audio_sample_rate_unknown,
                        )

                        sourceChannelCountListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.source?.channelCount?.toString(),
                            R.string.audio_channel_count_unknown,
                        )

                        sourcePeakBitrateListItem.setSupportingTextOrUnknown(
                            R.string.audio_bitrate_format,
                            outputConfiguration?.source?.peakBitrateBps?.let {
                                decimalFormatter.format(it / 1000f)
                            },
                        )

                        sourceAverageBitrateListItem.setSupportingTextOrUnknown(
                            R.string.audio_bitrate_format,
                            outputConfiguration?.source?.averageBitrateBps?.let {
                                decimalFormatter.format(it / 1000f)
                            },
                        )

                        // Transcoding
                        transcodingHeaderListItem.setLeadingIconImage(
                            when (outputConfiguration?.transcoding?.outputMode) {
                                OutputConfiguration.Transcoding.OutputMode.PCM ->
                                    R.drawable.ic_conversion_path

                                OutputConfiguration.Transcoding.OutputMode.OFFLOAD ->
                                    R.drawable.ic_conversion_path_off

                                OutputConfiguration.Transcoding.OutputMode.PASSTHROUGH ->
                                    R.drawable.ic_double_arrow

                                null -> R.drawable.ic_conversion_path
                            }
                        )

                        transcodingEncodingListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.transcoding?.encoding?.displayName,
                            R.string.audio_encoding_unknown,
                        )

                        transcodingCompressionListItem.setSupportingText(
                            when (outputConfiguration?.transcoding?.encoding?.compression) {
                                OutputConfiguration.Compression.LOSSY ->
                                    R.string.audio_format_compression_lossy

                                OutputConfiguration.Compression.LOSSLESS ->
                                    R.string.audio_format_compression_lossless

                                OutputConfiguration.Compression.UNCOMPRESSED ->
                                    R.string.audio_format_compression_uncompressed

                                null -> R.string.audio_format_compression_unknown
                            }
                        )

                        transcodingOutputModeListItem.setSupportingText(
                            when (outputConfiguration?.transcoding?.outputMode) {
                                OutputConfiguration.Transcoding.OutputMode.PCM ->
                                    R.string.audio_output_mode_pcm

                                OutputConfiguration.Transcoding.OutputMode.OFFLOAD ->
                                    R.string.audio_output_mode_offload

                                OutputConfiguration.Transcoding.OutputMode.PASSTHROUGH ->
                                    R.string.audio_output_mode_passthrough

                                null -> R.string.audio_output_mode_unknown
                            }
                        )

                        transcodingSampleRateListItem.setSupportingTextOrUnknown(
                            R.string.audio_sample_rate_format,
                            outputConfiguration?.transcoding?.sampleRateHz?.let {
                                decimalFormatter.format(it / 1000f)
                            },
                            R.string.audio_sample_rate_unknown,
                        )

                        transcodingChannelCountListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.transcoding?.channelCount?.toString(),
                            R.string.audio_channel_count_unknown,
                        )

                        transcodingBitrateListItem.setSupportingTextOrUnknown(
                            R.string.audio_bitrate_format,
                            outputConfiguration?.transcoding?.bitrateBps?.let {
                                decimalFormatter.format(it / 1000f)
                            },
                        )

                        // Device
                        deviceHeaderListItem.setLeadingIconImage(
                            when (outputConfiguration?.device?.type) {
                                OutputConfiguration.Device.Type.BUILTIN ->
                                    R.drawable.ic_mobile_speaker

                                OutputConfiguration.Device.Type.HEADPHONES ->
                                    R.drawable.ic_headphones

                                OutputConfiguration.Device.Type.EXTERNAL_SPEAKERS ->
                                    R.drawable.ic_speaker_group

                                OutputConfiguration.Device.Type.BLUETOOTH ->
                                    R.drawable.ic_bluetooth

                                OutputConfiguration.Device.Type.HDMI ->
                                    R.drawable.ic_settings_input_hdmi

                                OutputConfiguration.Device.Type.USB ->
                                    R.drawable.ic_usb

                                OutputConfiguration.Device.Type.REMOTE ->
                                    R.drawable.ic_cast

                                OutputConfiguration.Device.Type.HEARING_AID ->
                                    R.drawable.ic_hearing_aid

                                null -> R.drawable.ic_media_output
                            }
                        )

                        deviceNameListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.device?.name,
                            R.string.audio_output_device_name_unknown
                        )

                        deviceTypeListItem.setSupportingText(
                            when (outputConfiguration?.device?.type) {
                                OutputConfiguration.Device.Type.BUILTIN ->
                                    R.string.audio_output_device_type_builtin

                                OutputConfiguration.Device.Type.HEADPHONES ->
                                    R.string.audio_output_device_type_headphones

                                OutputConfiguration.Device.Type.EXTERNAL_SPEAKERS ->
                                    R.string.audio_output_device_type_external_speakers

                                OutputConfiguration.Device.Type.BLUETOOTH ->
                                    R.string.audio_output_device_type_bluetooth

                                OutputConfiguration.Device.Type.HDMI ->
                                    R.string.audio_output_device_type_hdmi

                                OutputConfiguration.Device.Type.USB ->
                                    R.string.audio_output_device_type_usb

                                OutputConfiguration.Device.Type.REMOTE ->
                                    R.string.audio_output_device_type_remote

                                OutputConfiguration.Device.Type.HEARING_AID ->
                                    R.string.audio_output_device_type_hearing_aid

                                null -> R.string.audio_output_device_type_unknown
                            }
                        )

                        deviceMaxSampleRateListItem.setSupportingTextOrUnknown(
                            R.string.audio_sample_rate_format,
                            outputConfiguration?.device?.sampleRatesHz?.maxOrNull()?.let {
                                decimalFormatter.format(it / 1000f)
                            },
                        )

                        deviceMaxChannelCountListItem.setSupportingTextOrUnknown(
                            outputConfiguration?.device?.channelCounts?.maxOrNull()?.toString(),
                        )
                    }
                }
            }
        }
    }

    private fun ListItem.setSupportingTextOrUnknown(
        value: String?,
        @StringRes unknownStringResId: Int? = null,
    ) {
        value?.also {
            supportingText = it
            isVisible = true
        } ?: run {
            unknownStringResId?.also {
                setSupportingText(it)
                isVisible = true
            } ?: run {
                isVisible = false
            }
        }
    }

    private fun ListItem.setSupportingTextOrUnknown(
        @StringRes stringResId: Int,
        value: Any?,
        @StringRes unknownStringResId: Int? = null,
    ) {
        value?.also {
            setSupportingText(stringResId, it)
            isVisible = true
        } ?: run {
            unknownStringResId?.also {
                setSupportingText(it)
                isVisible = true
            } ?: run {
                isVisible = false
            }
        }
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)

        private val decimalFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
