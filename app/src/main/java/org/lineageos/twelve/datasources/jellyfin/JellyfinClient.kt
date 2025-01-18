/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.jellyfin.models.CreatePlaylist
import org.lineageos.twelve.datasources.jellyfin.models.CreatePlaylistResult
import org.lineageos.twelve.datasources.jellyfin.models.Item
import org.lineageos.twelve.datasources.jellyfin.models.PlaylistItems
import org.lineageos.twelve.datasources.jellyfin.models.QueryResult
import org.lineageos.twelve.datasources.jellyfin.models.SystemInfo
import org.lineageos.twelve.datasources.jellyfin.models.UpdatePlaylist
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest
import java.util.UUID

/**
 * Jellyfin client.
 *
 * @param server The base URL of the server
 * @param username The login username of the server
 * @param password The corresponding password of the user
 * @param deviceIdentifier The device identifier
 * @param packageName The package name of the app
 * @param tokenGetter A function to get the token
 * @param tokenSetter A function to set the token
 * @param cache OkHttp's [Cache]
 */
class JellyfinClient(
    server: String,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String,
    tokenGetter: () -> String?,
    tokenSetter: (String) -> Unit,
    cache: Cache? = null,
) {
    private val serverUri = Uri.parse(server)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(JellyfinAuthInterceptor(tokenGetter))
        .authenticator(
            JellyfinAuthenticator(
                serverUri,
                username,
                password,
                deviceIdentifier,
                packageName,
                tokenGetter,
                tokenSetter,
            )
        )
        .cache(cache)
        .build()

    private val api = Api(okHttpClient, serverUri)

    suspend fun getAlbums(sortingRule: SortingRule) = ApiRequest.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    ).execute(api)

    suspend fun getArtists(sortingRule: SortingRule) = ApiRequest.get<QueryResult>(
        listOf("Artists"),
        queryParameters = listOf(
            "IncludeItemTypes" to "Audio",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    ).execute(api)

    suspend fun getGenres(sortingRule: SortingRule) = ApiRequest.get<QueryResult>(
        listOf("Genres"),
        queryParameters = listOf(
            "IncludeItemTypes" to "Audio",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    ).execute(api)

    suspend fun getPlaylists(sortingRule: SortingRule) = ApiRequest.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "IncludeItemTypes" to "Playlist",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    ).execute(api)

    suspend fun getItems(query: String) = ApiRequest.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "SearchTerm" to query,
            "IncludeItemTypes" to "Playlist,MusicAlbum,MusicArtist,MusicGenre,Audio",
            "Recursive" to true,
        ),
    ).execute(api)

    suspend fun getAlbum(id: UUID) = getItem(id)

    suspend fun getArtist(id: UUID) = getItem(id)

    suspend fun getPlaylist(id: UUID) = getItem(id)

    suspend fun getGenre(id: UUID) = getItem(id)

    fun getAlbumThumbnail(id: UUID) = getItemThumbnail(id)

    fun getArtistThumbnail(id: UUID) = getItemThumbnail(id)

    fun getPlaylistThumbnail(id: UUID) = getItemThumbnail(id)

    fun getGenreThumbnail(id: UUID) = getItemThumbnail(id)

    suspend fun getAlbumTracks(id: UUID) = ApiRequest.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ParentId" to id,
            "IncludeItemTypes" to "Audio",
            "Recursive" to true,
        ),
    ).execute(api)

    suspend fun getArtistWorks(id: UUID) = ApiRequest.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ArtistIds" to id,
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        ),
    ).execute(api)

    suspend fun getPlaylistItemIds(id: UUID) = ApiRequest.get<PlaylistItems>(
        listOf(
            "Playlists",
            id.toString(),
        ),
    ).execute(api)

    suspend fun getPlaylistTracks(id: UUID) = ApiRequest.get<QueryResult>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
    ).execute(api)

    suspend fun getGenreContent(id: UUID) = ApiRequest.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "GenreIds" to id,
            "IncludeItemTypes" to "MusicAlbum,Playlist,Audio",
            "Recursive" to true,
        ),
    ).execute(api)

    suspend fun getAudio(id: UUID) = getItem(id)

    fun getAudioPlaybackUrl(id: UUID) = api.buildUrl(
        listOf(
            "Audio",
            id.toString(),
            "stream",
        ),
        queryParameters = listOf(
            "static" to true,
        ),
    )

    suspend fun createPlaylist(name: String) =
        ApiRequest.post<CreatePlaylist, CreatePlaylistResult>(
            listOf("Playlists"),
            data = CreatePlaylist(
                name = name,
                ids = listOf(),
                users = listOf(),
                isPublic = true,
            ),
        ).execute(api)

    suspend fun renamePlaylist(id: UUID, name: String) = ApiRequest.post<UpdatePlaylist, Unit>(
        listOf(
            "Playlists",
            id.toString(),
        ),
        data = UpdatePlaylist(
            name = name,
        ),
    ).execute(api)

    suspend fun addItemToPlaylist(id: UUID, audioId: UUID) = ApiRequest.post<Any, Unit>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
        queryParameters = listOf(
            "Ids" to audioId,
        ),
    ).execute(api)

    suspend fun removeItemFromPlaylist(id: UUID, audioId: UUID) = ApiRequest.delete<Unit>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
        queryParameters = listOf(
            "EntryIds" to audioId,
        ),
    ).execute(api)

    suspend fun getSystemInfo() = ApiRequest.get<SystemInfo>(
        listOf(
            "System",
            "Info",
            "Public",
        ),
    ).execute(api)

    private suspend fun getItem(id: UUID) = ApiRequest.get<Item>(
        listOf(
            "Items",
            id.toString(),
        ),
    ).execute(api)

    private fun getItemThumbnail(id: UUID) = api.buildUrl(
        listOf(
            "Items",
            id.toString(),
            "Images",
            "Primary",
        ),
    )

    private fun getSortParameter(sortingRule: SortingRule) = buildList {
        add(
            "sortBy" to when (sortingRule.strategy) {
                SortingStrategy.ARTIST_NAME -> "AlbumArtist,Artist"
                SortingStrategy.CREATION_DATE -> "DateCreated"
                SortingStrategy.MODIFICATION_DATE -> "DateLastContentAdded"
                SortingStrategy.NAME -> "Name"
                SortingStrategy.PLAY_COUNT -> "PlayCount"
            }
        )

        add(
            "sortOrder" to when (sortingRule.reverse) {
                true -> "Descending"
                false -> "Ascending"
            }
        )
    }

    companion object {
        const val JELLYFIN_API_VERSION = "10.10.3"
    }
}
