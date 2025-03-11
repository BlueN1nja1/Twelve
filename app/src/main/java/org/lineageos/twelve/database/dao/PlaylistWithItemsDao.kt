/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import org.lineageos.twelve.database.TwelveDatabase

@Dao
abstract class PlaylistWithItemsDao(database: TwelveDatabase) {
    private val playlistDao = database.getPlaylistDao()
    private val playlistItemCrossRefDao = database.getPlaylistItemCrossRefDao()

    /**
     * Add an item to a playlist (creates a cross-reference).
     */
    open suspend fun addItemToPlaylist(playlistId: Long, audioUri: Uri) =
        playlistItemCrossRefDao._addItemToPlaylist(playlistId, audioUri)

    /**
     * Remove an item from a playlist (deletes the cross-reference) and delete the item if it's the
     * last association or if the user never listened to it.
     */
    open suspend fun removeItemFromPlaylist(playlistId: Long, audioUri: Uri) =
        playlistItemCrossRefDao._removeItemFromPlaylist(playlistId, audioUri)

    /**
     * Get a flow of the playlists that includes (or not) the given item.
     */
    fun getPlaylistsWithItemStatus(audioUri: Uri) =
        playlistDao._getPlaylistsWithItemStatus(audioUri)
}
