/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.models

/**
 * Whatever an artist has worked on.
 *
 * @param albums Albums released by the artist
 * @param appearsInAlbum Albums on which the artist appears
 * @param appearsInPlaylist Playlists on which the artist appears
 */
data class ArtistWorks(
    val albums: List<Album>,
    val appearsInAlbum: List<Album>,
    val appearsInPlaylist: List<Playlist>,
)
