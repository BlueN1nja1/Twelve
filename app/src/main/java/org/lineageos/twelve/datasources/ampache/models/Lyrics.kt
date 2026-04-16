/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Lyrics.
 *
 * @param text The text
 * @param url The URL
 */
@Serializable
data class Lyrics(
    @SerialName("text") val text: String,
    @SerialName("url") val url: String,
)
