/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * A disc title for an album.
 *
 * Note: OpenSubsonic only.
 *
 * @param disc The disc number
 * @param title The name of the disc
 */
@Serializable
data class DiscTitle(
    val disc: Int,
    val title: String,
)
