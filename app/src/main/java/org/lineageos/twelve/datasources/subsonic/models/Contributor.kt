/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * A contributor artist for a song or an album.
 *
 * Note: OpenSubsonic only.
 *
 * @param role The contributor role
 * @param subRole The subRole for roles that may require it. Ex: The instrument for the performer
 *   role (TMCL/performer tags). Note: For consistency between different tag formats, the TIPL sub
 *   roles should be directly exposed in the role field
 * @param artist The artist taking on the role (Note: Only the required ArtistID3 fields should be
 *   returned by default)
 */
@Serializable
data class Contributor(
    val role: String,
    val subRole: String? = null,
    val artist: ArtistID3,
)
