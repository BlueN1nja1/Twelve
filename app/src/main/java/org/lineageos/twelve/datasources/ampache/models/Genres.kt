/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Genres.
 *
 * @param totalCount The total count
 * @param md5 The md5
 * @param genre The genres
 */
@Serializable
data class Genres(
    @SerialName("total_count") val totalCount: Int? = null,
    @SerialName("md5") val md5: String? = null,
    @SerialName("genre") val genre: List<Genre>,
)
