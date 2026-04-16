/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Preferences.
 *
 * @param preference The list of preferences
 */
@Serializable
data class Preferences(
    @SerialName("preference") val preference: List<Preference>,
)
