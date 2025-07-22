/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Data class describing the whole output setup, from the source stream up to the speakers.
 *
 * @param source The audio stream format information
 * @param transcoding Information related to how the speaker will receive the audio
 * @param device Output speaker configuration
 */
data class OutputConfiguration(
    /**
     * The audio stream format information.
     */
    val source: Source,

    /**
     * Information related to how the speaker will receive the audio.
     */
    val transcoding: Transcoding,

    /**
     * Output speaker configuration.
     */
    val device: Device,

    /**
     * Verdict of the output configuration.
     */
    val verdict: Verdict,
) {
    /**
     * Audio file/stream format information.
     *
     * @param mimeType The MIME type of the stream
     * @param encoding The [Encoding] of the stream
     * @param compression The [Compression] of the stream
     * @param sampleRateHz The sample rate of the stream in Hz
     * @param channelCount The number of channels of the stream
     * @param peakBitrateBps The peak bitrate of the stream in bit/s, may be null in certain
     *   configurations
     * @param averageBitrateBps The average bitrate of the stream in bit/s, may be null in certain
     *   configurations
     */
    data class Source(
        val mimeType: String?,
        val encoding: Encoding?,
        val compression: Compression?,
        val sampleRateHz: Int?,
        val channelCount: Int?,
        val peakBitrateBps: Int?,
        val averageBitrateBps: Int?,
    )

    /**
     * Information related to how the [Device] will receive the [Source].
     *
     * @param outputMode The [Transcoding.OutputMode] of the transcoding
     * @param encoding The [Encoding] of the resulting stream
     * @param sampleRateHz The sample rate of the resulting stream in Hz, may be null in certain
     *   configurations
     * @param channelCount The number of channels of the resulting stream, may be null in certain
     *   configurations
     * @param bitrateBps The bitrate of the resulting stream in bit/s, may be null in certain
     *   configurations
     */
    data class Transcoding(
        val outputMode: OutputMode,
        val encoding: Encoding?,
        val sampleRateHz: Int?,
        val channelCount: Int?,
        val bitrateBps: Int?,
    ) {
        /**
         * Audio output mode.
         */
        enum class OutputMode {
            /**
             * The audio sink plays PCM audio.
             */
            PCM,

            /**
             * The audio sink plays encoded audio in offload.
             */
            OFFLOAD,

            /**
             * The audio sink plays encoded audio in passthrough.
             */
            PASSTHROUGH,
        }
    }

    /**
     * Information related to an output device.
     *
     * @param name User-friendly name of the device
     * @param type The [Device.Type] of the device
     * @param sampleRatesHz The sample rates supported by the device in Hz
     * @param channelCounts The supported number of channels configurations of the device
     * @param encodings A set of supported [Encoding]s
     */
    data class Device(
        val name: String?,
        val type: Type?,
        val sampleRatesHz: Set<Int>,
        val channelCounts: Set<Int>,
        val encodings: Set<Encoding>,
    ) {
        enum class Type {
            /**
             * Internal speakers.
             */
            BUILTIN,

            /**
             * Headphones connected to the device.
             */
            HEADPHONES,

            /**
             * External speakers.
             */
            EXTERNAL_SPEAKERS,

            /**
             * Bluetooth audio device.
             */
            BLUETOOTH,

            /**
             * HDMI audio device.
             */
            HDMI,

            /**
             * USB device output.
             */
            USB,

            /**
             * Remote audio device (cast).
             */
            REMOTE,

            /**
             * Hearing aid device.
             */
            HEARING_AID,
        }
    }

    /**
     * Verdict of the output configuration.
     *
     * @param hiRes Whether the current playback deserves a Hi-Res stamp
     * @param potentialIssues A set of [Verdict.Issue] that the user can be warned about
     */
    data class Verdict(
        val hiRes: Boolean,
        val potentialIssues: Set<Issue>,
    ) {
        enum class Issue {
            /**
             * The device is outputting the stream at a sample rate lower than the source one.
             */
            DOWNSAMPLING,

            /**
             * The app is not using PCM float mode to output a stream with a bit depth higher than
             * 16-bit.
             */
            PCM_FLOAT_MODE_DISABLED,

            /**
             * The user is listening to a track with more channels than the output device.
             */
            DOWNMIXING,

            /**
             * The source stream is getting compressed to a lossy encoding
             */
            POST_PROCESSING_LOSSY_COMPRESSION,
        }
    }

    /**
     * Audio stream encoding formats.
     *
     * @param displayName User-friendly name of the encoding
     * @param compression The [Compression] of the stream
     */
    enum class Encoding(
        val displayName: String,
        val compression: Compression,
    ) {
        AAC_ELD(
            displayName = "Advanced Audio Coding Enhanced Low Delay (AAC ELD)",
            compression = Compression.LOSSY,
        ),
        AAC_ER_BSAC(
            displayName = "Advanced Audio Coding Error Resilient Bit-Sliced Arithmetic Coding",
            compression = Compression.LOSSY,
        ),
        AAC_LC(
            displayName = "Advanced Audio Coding Low Complexity (AAC-LC)",
            compression = Compression.LOSSY,
        ),
        AAC_HE_V1(
            displayName = "Advanced Audio Coding High-Efficiency v1 (AAC HE v1)",
            compression = Compression.LOSSY,
        ),
        AAC_HE_V2(
            displayName = "Advanced Audio Coding High-Efficiency v2 (AAC HE v2)",
            compression = Compression.LOSSY,
        ),
        AAC_XHE(
            displayName = "Advanced Audio Coding Extended High-Efficiency (AAC xHE)",
            compression = Compression.LOSSY,
        ),
        AC3(
            displayName = "Dolby Digital (AC-3)",
            compression = Compression.LOSSY,
        ),
        AC4(
            displayName = "Dolby Audio Codec 4 (AC-4)",
            compression = Compression.LOSSY,
        ),
        DOLBY_MAT(
            displayName = "Dolby Metadata-enhanced Audio Transmission",
            compression = Compression.LOSSY,
        ),
        DOLBY_TRUEHD(
            displayName = "Dolby TrueHD",
            compression = Compression.LOSSLESS,
        ),
        DRA(
            displayName = "Dynamic Resolution Adaptation",
            compression = Compression.LOSSY,
        ),
        DSD(
            displayName = "Direct Stream Digital",
            compression = Compression.UNCOMPRESSED,
        ),
        DTS(
            displayName = "DTS",
            compression = Compression.LOSSY,
        ),
        DTS_HD(
            displayName = "DTS HD",
            compression = Compression.LOSSY,
        ),
        DTS_HD_MA(
            displayName = "DTS HD Master Audio",
            compression = Compression.LOSSLESS,
        ),
        DTS_UHD_P1(
            displayName = "DTS:X Profile-1",
            compression = Compression.LOSSY,
        ),
        DTS_UHD_P2(
            displayName = "DTS:X Profile-2",
            compression = Compression.LOSSY,
        ),
        E_AC3(
            displayName = "Dolby Digital Plus (E-AC-3)",
            compression = Compression.LOSSY,
        ),
        E_AC3_JOC(
            displayName = "Dolby Digital Plus with Dolby Atmos (E-AC-3-JOC)",
            compression = Compression.LOSSY,
        ),
        IEC61937(
            displayName = "S/PDIF (IEC 61937)",
            compression = Compression.LOSSLESS,
        ),
        MP3(
            displayName = "MP3",
            compression = Compression.LOSSY,
        ),
        MPEGH_BL_L3(
            displayName = "MPEG-H 3D Audio Baseline Profile (level 3)",
            compression = Compression.LOSSY,
        ),
        MPEGH_BL_L4(
            displayName = "MPEG-H 3D Audio Baseline Profile (level 4)",
            compression = Compression.LOSSY,
        ),
        MPEGH_LC_L3(
            displayName = "MPEG-H 3D Audio Low Complexity Profile (level 3)",
            compression = Compression.LOSSY,
        ),
        MPEGH_LC_L4(
            displayName = "MPEG-H 3D Audio Low Complexity Profile (level 4)",
            compression = Compression.LOSSY,
        ),
        OPUS(
            displayName = "Opus",
            compression = Compression.LOSSY,
        ),
        PCM_8_BIT(
            displayName = "PCM 8-bit",
            compression = Compression.UNCOMPRESSED,
        ),
        PCM_16_BIT(
            displayName = "PCM 16-bit",
            compression = Compression.UNCOMPRESSED,
        ),
        PCM_24_BIT(
            displayName = "PCM 24-bit",
            compression = Compression.UNCOMPRESSED,
        ),
        PCM_24_BIT_PACKED(
            displayName = "PCM 24-bit packed",
            compression = Compression.UNCOMPRESSED,
        ),
        PCM_32_BIT(
            displayName = "PCM 32-bit",
            compression = Compression.UNCOMPRESSED,
        ),
        PCM_FLOAT(
            displayName = "PCM single-precision floating-point",
            compression = Compression.UNCOMPRESSED,
        );
    }

    /**
     * Audio format compression type.
     */
    enum class Compression {
        /**
         * Lossy audio format.
         */
        LOSSY,

        /**
         * Compressed lossless audio format.
         */
        LOSSLESS,

        /**
         * Uncompressed lossless audio format.
         */
        UNCOMPRESSED,
    }
}
