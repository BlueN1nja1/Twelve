/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.buildMediaItem
import org.lineageos.twelve.ext.permissionsGranted
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.Provider
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.repositories.MediaRepository
import org.lineageos.twelve.utils.PermissionsUtils

class MediaRepositoryTree(
    private val context: Context,
    private val repository: MediaRepository,
) {
    /**
     * No permissions media item.
     */
    private val noPermissionsMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_no_permissions),
        mediaId = NO_PERMISSIONS_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
    )

    /**
     * No permissions description media item.
     */
    private val noPermissionsDescriptionMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_no_permissions_description),
        mediaId = NO_PERMISSIONS_DESCRIPTION_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = false,
        mediaType = MediaMetadata.MEDIA_TYPE_MIXED,
    )

    /**
     * Albums media item.
     */
    private val albumsMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_albums),
        mediaId = ALBUMS_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS,
    )

    /**
     * Artists media item.
     */
    private val artistsMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_artists),
        mediaId = ARTISTS_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS,
    )

    /**
     * Genres media item.
     */
    private val genresMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_genres),
        mediaId = GENRES_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_GENRES,
    )

    /**
     * Playlists media item.
     */
    private val playlistsMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_playlists),
        mediaId = PLAYLISTS_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS,
    )

    /**
     * Change provider media item.
     */
    private val changeProviderMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_change_provider),
        mediaId = CHANGE_PROVIDER_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
    )

    /**
     * Provider changed media item.
     */
    private val providerChangedMediaItem = buildMediaItem(
        title = context.getString(R.string.library_item_provider_changed),
        mediaId = PROVIDER_CHANGED_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = false,
        mediaType = MediaMetadata.MEDIA_TYPE_MIXED,
    )

    /**
     * Get the root media item of the tree.
     */
    val rootMediaItem = buildMediaItem(
        title = context.getString(R.string.app_name),
        mediaId = ROOT_MEDIA_ITEM_ID,
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED,
    )

    /**
     * Given a media ID, gets it's corresponding media item.
     */
    suspend fun getItem(mediaId: String) = when (mediaId) {
        ROOT_MEDIA_ITEM_ID -> rootMediaItem

        NO_PERMISSIONS_MEDIA_ITEM_ID -> noPermissionsMediaItem

        NO_PERMISSIONS_DESCRIPTION_MEDIA_ITEM_ID -> noPermissionsDescriptionMediaItem

        ALBUMS_MEDIA_ITEM_ID -> albumsMediaItem

        ARTISTS_MEDIA_ITEM_ID -> artistsMediaItem

        GENRES_MEDIA_ITEM_ID -> genresMediaItem

        PLAYLISTS_MEDIA_ITEM_ID -> playlistsMediaItem

        CHANGE_PROVIDER_MEDIA_ITEM_ID -> changeProviderMediaItem

        else -> when {
            mediaId.startsWith(PROVIDER_MEDIA_ITEM_ID_PREFIX) ->
                mediaIdToProvider(mediaId)?.toMedia3MediaItem()

            else -> mediaIdToUniqueItem(mediaId)?.toMedia3MediaItem()
        }
    }

    /**
     * Given an item's media ID, gets its children.
     */
    suspend fun getChildren(mediaId: String) = when (mediaId) {
        ROOT_MEDIA_ITEM_ID -> when (context.permissionsGranted(PermissionsUtils.mainPermissions)) {
            true -> listOf(
                albumsMediaItem,
                artistsMediaItem,
                genresMediaItem,
                playlistsMediaItem,
                changeProviderMediaItem,
            )

            false -> listOf(noPermissionsMediaItem)
        }

        NO_PERMISSIONS_MEDIA_ITEM_ID -> listOf(noPermissionsDescriptionMediaItem)

        NO_PERMISSIONS_DESCRIPTION_MEDIA_ITEM_ID -> listOf()

        ALBUMS_MEDIA_ITEM_ID -> repository.albums().toOneShotResult().map {
            it.toMedia3MediaItem()
        }

        ARTISTS_MEDIA_ITEM_ID -> repository.artists().toOneShotResult().map {
            it.toMedia3MediaItem()
        }

        GENRES_MEDIA_ITEM_ID -> repository.genres().toOneShotResult().map {
            it.toMedia3MediaItem()
        }

        PLAYLISTS_MEDIA_ITEM_ID -> repository.playlists().toOneShotResult().map {
            it.toMedia3MediaItem()
        }

        CHANGE_PROVIDER_MEDIA_ITEM_ID -> repository.allVisibleProviders.value.map {
            it.toMedia3MediaItem()
        }

        else -> when (val it = mediaIdToUniqueItem(mediaId)) {
            is Album -> repository.album(it.uri).toOneShotResult().second.map { albumAudios ->
                albumAudios.toMedia3MediaItem()
            }

            is Artist -> repository.artist(it.uri).toOneShotResult().second.let { artistWorks ->
                listOf(
                    artistWorks.albums,
                    artistWorks.appearsInAlbum,
                ).flatten().map { allRelatedAlbums ->
                    allRelatedAlbums.toMedia3MediaItem()
                }
            }

            is Audio -> listOf()

            is Genre -> repository.genre(it.uri).toOneShotResult().second.let { genreContent ->
                listOf(
                    genreContent.appearsInAlbums,
                    genreContent.appearsInPlaylists,
                    genreContent.audios,
                ).flatten().map { allRelatedMediaItems ->
                    allRelatedMediaItems.toMedia3MediaItem()
                }
            }

            is Playlist -> repository.playlist(
                it.uri
            ).toOneShotResult().second.map { playlistAudio ->
                playlistAudio.toMedia3MediaItem()
            }

            null -> when {
                mediaId.startsWith(PROVIDER_MEDIA_ITEM_ID_PREFIX) -> {
                    mediaIdToProvider(mediaId)?.let { provider ->
                        repository.setNavigationProvider(provider)
                    }

                    listOf(providerChangedMediaItem)
                }

                else -> listOf()
            }
        }
    }

    /**
     * Given a list of media items, gets an equivalent list of items that can be passed to the
     * player. This should be used with onAddMediaItems and onSetMediaItems.
     * TODO: [MediaItem.requestMetadata] support.
     */
    suspend fun resolveMediaItems(mediaItems: List<MediaItem>) = mediaItems.mapNotNull {
        it.takeIf { it.localConfiguration?.uri != null } ?: getItem(it.mediaId)
    }

    /**
     * Given a query, search for media items.
     */
    suspend fun search(query: String) = repository.search("%${query}%").toOneShotResult().map {
        it.toMedia3MediaItem()
    }

    /**
     * Given a media ID, get the item from the repository.
     */
    private suspend fun mediaIdToUniqueItem(mediaId: String) = when {
        mediaId.startsWith(Album.ALBUM_MEDIA_ITEM_ID_PREFIX) -> {
            repository.album(Uri.parse(mediaId.removePrefix(Album.ALBUM_MEDIA_ITEM_ID_PREFIX)))
                .toOneShotResult().first
        }

        mediaId.startsWith(Artist.ARTIST_MEDIA_ITEM_ID_PREFIX) -> {
            repository.artist(Uri.parse(mediaId.removePrefix(Artist.ARTIST_MEDIA_ITEM_ID_PREFIX)))
                .toOneShotResult().first
        }

        mediaId.startsWith(Audio.AUDIO_MEDIA_ITEM_ID_PREFIX) -> {
            repository.audio(Uri.parse(mediaId.removePrefix(Audio.AUDIO_MEDIA_ITEM_ID_PREFIX)))
                .toOneShotResult()
        }

        mediaId.startsWith(Genre.GENRE_MEDIA_ITEM_ID_PREFIX) -> {
            repository.genre(Uri.parse(mediaId.removePrefix(Genre.GENRE_MEDIA_ITEM_ID_PREFIX)))
                .toOneShotResult().first
        }

        mediaId.startsWith(Playlist.PLAYLIST_MEDIA_ITEM_ID_PREFIX) -> {
            repository.playlist(Uri.parse(mediaId.removePrefix(Playlist.PLAYLIST_MEDIA_ITEM_ID_PREFIX)))
                .toOneShotResult().first
        }

        else -> null
    }

    private fun Provider.toMedia3MediaItem() = buildMediaItem(
        title = name,
        mediaId = "$PROVIDER_MEDIA_ITEM_ID_PREFIX${type.name};$typeId",
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_MIXED,
        subtitle = context.getString(type.nameStringResId),
    )

    private suspend fun mediaIdToProvider(mediaId: String): Provider? {
        if (!mediaId.startsWith(PROVIDER_MEDIA_ITEM_ID_PREFIX)) {
            return null
        }

        val (type, typeId) = mediaId.removePrefix(PROVIDER_MEDIA_ITEM_ID_PREFIX).split(";")
        val providerType = ProviderType.valueOf(type)
        val providerTypeId = typeId.toLong()

        return repository.provider(ProviderIdentifier(providerType, providerTypeId)).first()
    }

    companion object {
        // Root ID
        private const val ROOT_MEDIA_ITEM_ID = "[root]"

        // No permissions ID
        private const val NO_PERMISSIONS_MEDIA_ITEM_ID = "[no_permissions]"
        private const val NO_PERMISSIONS_DESCRIPTION_MEDIA_ITEM_ID = "[no_permissions_description]"

        // Root elements IDs
        private const val ALBUMS_MEDIA_ITEM_ID = "[albums]"
        private const val ARTISTS_MEDIA_ITEM_ID = "[artists]"
        private const val GENRES_MEDIA_ITEM_ID = "[genres]"
        private const val PLAYLISTS_MEDIA_ITEM_ID = "[playlists]"
        private const val CHANGE_PROVIDER_MEDIA_ITEM_ID = "[change_provider]"

        // Provider ID prefix
        private const val PROVIDER_MEDIA_ITEM_ID_PREFIX = "[provider]"

        // Provider changed ID
        private const val PROVIDER_CHANGED_MEDIA_ITEM_ID = "[provider_changed]"

        /**
         * Converts a flow of [RequestStatus] to a one-shot result of [T].
         * Raises an exception on error.
         */
        private suspend fun <T, E> Flow<RequestStatus<T, E>>.toOneShotResult() = mapNotNull {
            when (it) {
                is RequestStatus.Loading -> {
                    null
                }

                is RequestStatus.Success -> {
                    it.data
                }

                is RequestStatus.Error -> throw Exception(
                    "Error while loading data, ${it.error}"
                )
            }
        }.first()
    }
}
