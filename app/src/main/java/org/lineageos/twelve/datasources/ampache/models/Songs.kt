/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Songs.
 *
 * @param totalCount The total count
 * @param md5 The md5
 * @param song The songs
 */
@Serializable
data class Songs(
    @SerialName("total_count") val totalCount: Int? = null,
    @SerialName("md5") val md5: String? = null,
    @SerialName("song") val song: List<Song>,
)
