/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult3(
    val artist: List<ArtistID3>? = null,
    val album: List<AlbumID3>? = null,
    val song: List<Child>? = null,
)
