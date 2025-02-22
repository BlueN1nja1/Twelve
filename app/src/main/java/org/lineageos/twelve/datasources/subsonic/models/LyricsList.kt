/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * List of structured lyrics.
 *
 * Note: OpenSubsonic only.
 *
 * @param structuredLyrics Structured lyrics. There can be multiple lyrics of the same type with the
 *   same language
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class LyricsList(
    val structuredLyrics: List<StructuredLyrics>,
)
