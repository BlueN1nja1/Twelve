/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * Starred songs, albums and artists.
 *
 * @param artist Starred artists
 * @param album Starred albums
 * @param song Starred songs
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Starred2(
    val artist: List<ArtistID3>? = null,
    val album: List<AlbumID3>? = null,
    val song: List<Child>? = null,
)
