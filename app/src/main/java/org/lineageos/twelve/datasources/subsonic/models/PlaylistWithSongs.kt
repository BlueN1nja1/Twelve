/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistWithSongs(
    // Playlist start
    val allowedUser: List<String>? = null,
    val id: String,
    val name: String,
    val comment: String? = null,
    val owner: String? = null,
    val public: Boolean? = null,
    val songCount: Int,
    val duration: Int? = null, // OpenSubsonic violates the API
    val created: InstantAsString,
    val changed: InstantAsString,
    val coverArt: String? = null,
    // Playlist end

    val entry: List<Child>? = null,
) {
    fun toPlaylist() = Playlist(
        allowedUser = allowedUser,
        id = id,
        name = name,
        comment = comment,
        owner = owner,
        public = public,
        songCount = songCount,
        duration = duration ?: 0,
        created = created,
        changed = changed,
        coverArt = coverArt,
    )
}
