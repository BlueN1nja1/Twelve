/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Preference.
 *
 * @param id The ID
 * @param name The name
 * @param level The level
 * @param description The description
 * @param value The value
 * @param type The type
 * @param category The category
 * @param subcategory The subcategory
 * @param hasAccess Whether the user has access to this preference
 */
@Serializable
data class Preference(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("level") val level: Int,
    @SerialName("description") val description: String,
    @SerialName("value") val value: String,
    @SerialName("type") val type: String,
    @SerialName("category") val category: String,
    @SerialName("subcategory") val subcategory: String?,
    @SerialName("has_access") val hasAccess: Boolean,
)
