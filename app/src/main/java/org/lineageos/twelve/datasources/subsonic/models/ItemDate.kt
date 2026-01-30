/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * A date for a media item that may be just a year, or year-month, or full date.
 *
 * Note: OpenSubsonic only.
 *
 * @param year The year
 * @param month The month (1-12)
 * @param day The day (1-31)
 */
@Serializable
data class ItemDate(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
)
