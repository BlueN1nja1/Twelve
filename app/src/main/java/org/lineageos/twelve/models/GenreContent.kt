/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Content related to a certain genre.
 *
 * @param appearsInAlbums Albums with audios related to this genre
 * @param appearsInPlaylists Playlists with audios related to this genre
 * @param audios Audios related to this genre
 */
data class GenreContent(
    val appearsInAlbums: List<Album>,
    val appearsInPlaylists: List<Playlist>,
    val audios: List<Audio>,
)
