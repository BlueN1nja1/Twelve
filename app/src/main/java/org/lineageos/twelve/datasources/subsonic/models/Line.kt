/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * One line of a song lyric.
 *
 * Note: OpenSubsonic only.
 *
 * @param value The actual text of this line
 * @param start The start time of the lyrics, relative to the start time of the track, in
 *   milliseconds. If this is not part of synced lyrics, start must be omitted
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Line(
    val value: String,
    val start: Long? = null,
)
