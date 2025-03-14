/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.models

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi

/**
 * Audio encoding formats.
 */
@OptIn(UnstableApi::class)
enum class Encoding(
    val displayName: String,
    private val media3Encoding: @C.Encoding Int,
) {
    PCM_8BIT("PCM 8-bit", C.ENCODING_PCM_8BIT),
    PCM_16BIT("PCM 16-bit", C.ENCODING_PCM_16BIT),
    PCM_16_BIT_BIG_ENDIAN("PCM 16-bit (big endian)", C.ENCODING_PCM_16BIT_BIG_ENDIAN),
    PCM_24BIT("PCM 24-bit", C.ENCODING_PCM_24BIT),
    PCM_24_BIT_BIG_ENDIAN("PCM 24-bit (big endian)", C.ENCODING_PCM_24BIT_BIG_ENDIAN),
    PCM_32BIT("PCM 32-bit", C.ENCODING_PCM_32BIT),
    PCM_32_BIT_BIG_ENDIAN("PCM 32-bit (big endian)", C.ENCODING_PCM_32BIT_BIG_ENDIAN),
    PCM_FLOAT("PCM 32-bit floating point", C.ENCODING_PCM_FLOAT),
    MP3("MP3", C.ENCODING_MP3),
    AAC_LC("Advanced Audio Coding Low Complexity (AAC-LC)", C.ENCODING_AAC_LC),
    AAC_HE_V1("Advanced Audio Coding High-Efficiency v1 (AAC HE v1)", C.ENCODING_AAC_HE_V1),
    AAC_HE_V2("Advanced Audio Coding High-Efficiency v2 (AAC HE v2)", C.ENCODING_AAC_HE_V2),
    AAC_XHE("Advance Audio Coding Extended High-Efficiency (AAC xHE)", C.ENCODING_AAC_XHE),
    AAC_ELD("Advance Audio Coding Enhanced Low Delay (AAC ELD)", C.ENCODING_AAC_ELD),
    AAC_ER_BSAC(
        "Advance Audio Coding Error Resilient Bit-Sliced Arithmetic Coding", C.ENCODING_AAC_ER_BSAC
    ),
    AC3("Dolby Digital (AC-3)", C.ENCODING_AC3),
    E_AC3("Dolby Digital Plus (E-AC-3)", C.ENCODING_E_AC3),
    E_AC3_JOC("Dolby Digital Plus with Dolby Atmos (E-AC-3-JOC)", C.ENCODING_E_AC3_JOC),
    AC4("Dolby Audio Codec 4 (AC-4)", C.ENCODING_AC4),
    DTS("DTS", C.ENCODING_DTS),
    DTS_HD("DTS HD", C.ENCODING_DTS_HD),
    DOLBY_TRUEHD("Dolby TrueHD", C.ENCODING_DOLBY_TRUEHD),
    OPUS("Opus", C.ENCODING_OPUS),
    DTS_UHD_P2("DTS UHD Profile-2", C.ENCODING_DTS_UHD_P2);

    companion object {
        fun fromMedia3Encoding(media3Encoding: @C.Encoding Int) = when (media3Encoding) {
            Format.NO_VALUE,
            C.ENCODING_INVALID -> null

            else -> entries.firstOrNull {
                media3Encoding == it.media3Encoding
            } ?: throw Exception("Unknown encoding: $media3Encoding")
        }
    }
}
