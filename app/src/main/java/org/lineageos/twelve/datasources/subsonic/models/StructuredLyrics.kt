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
 * @param lang The lyrics language (ideally ISO 639). If the language is unknown (e.g. lrc file),
 *   the server must return `und` (ISO standard) or `xxx` (common value for taggers)
 * @param synced True if the lyrics are synced, false otherwise
 * @param line The actual lyrics. Ordered by start time (synced) or appearance order (unsynced)
 * @param displayArtist The artist name to display. This could be the localized name, or any other
 *   value
 * @param displayTitle The title to display. This could be the song title (localized), or any other
 *   value
 * @param offset The offset to apply to all lyrics, in milliseconds. Positive means lyrics appear
 *   sooner, negative means later. If not included, the offset must be assumed to be 0
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class StructuredLyrics(
    val lang: String,
    val synced: Boolean,
    val line: List<Line>,
    val displayArtist: String? = null,
    val displayTitle: String? = null,
    val offset: Long? = null,
)
