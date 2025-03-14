/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.datasources.jellyfin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
enum class ItemType(
    val type: String,
) {
    @SerialName("MusicAlbum")
    MUSIC_ALBUM("MusicAlbum"),

    @SerialName("MusicArtist")
    MUSIC_ARTIST("MusicArtist"),

    @SerialName("Person")
    PERSON("Person"),

    @SerialName("Audio")
    AUDIO("Audio"),

    @SerialName("Genre")
    GENRE("Genre"),

    @SerialName("MusicGenre")
    MUSIC_GENRE("MusicGenre"),

    @SerialName("Playlist")
    PLAYLIST("Playlist"),
}
