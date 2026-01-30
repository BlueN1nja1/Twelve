/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * The replay gain data of a song.
 *
 * Note: OpenSubsonic only.
 *
 * @param trackGain The track replay gain value (In Db)
 * @param albumGain The album replay gain value (In Db)
 * @param trackPeak The track peak value (Must be positive)
 * @param albumPeak The album peak value (Must be positive)
 * @param baseGain The base gain value (In Db) (Ogg Opus Output Gain for example)
 * @param fallbackGain An optional fallback gain that clients should apply when the corresponding
 *   gain value is missing (Can be computed from the tracks or exposed as an user setting)
 */
@Serializable
data class ReplayGain(
    val trackGain: Double? = null,
    val albumGain: Double? = null,
    val trackPeak: Double? = null,
    val albumPeak: Double? = null,
    val baseGain: Double? = null,
    val fallbackGain: Double? = null
)
