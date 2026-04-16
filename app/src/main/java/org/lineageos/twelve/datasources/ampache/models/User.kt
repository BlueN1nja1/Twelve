/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User.
 *
 * @param id The ID
 * @param username The username
 * @param auth The auth
 * @param email The email
 * @param access The access
 * @param streamtoken The streamtoken
 * @param fullnamePublic The fullname public
 * @param validation The validation
 * @param disabled The disabled
 * @param createDate The create date
 * @param lastSeen The last seen
 * @param website The website
 * @param state The state
 * @param city The city
 * @param art The artwork URL
 * @param hasArt Whether this user has an artwork available
 */
@Serializable
data class User(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("auth") val auth: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("access") val access: Int? = null,
    @SerialName("streamtoken") val streamtoken: String? = null,
    @SerialName("fullname_public") val fullnamePublic: Boolean? = null,
    @SerialName("validation") val validation: String? = null,
    @SerialName("disabled") val disabled: Boolean? = null,
    @SerialName("create_date") val createDate: InstantAsTimestampLong? = null,
    @SerialName("last_seen") val lastSeen: InstantAsTimestampLong? = null,
    @SerialName("website") val website: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("art") val art: String? = null,
    @SerialName("has_art") val hasArt: Boolean? = null,
)
