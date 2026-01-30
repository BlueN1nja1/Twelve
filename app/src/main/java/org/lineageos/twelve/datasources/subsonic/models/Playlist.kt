/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val allowedUser: List<String>? = null,
    val id: String,
    val name: String,
    val comment: String? = null,
    val owner: String? = null,
    val public: Boolean? = null,
    val songCount: Int,
    val duration: Int,
    val created: InstantAsString,
    val changed: InstantAsString,
    val coverArt: String? = null,
)
