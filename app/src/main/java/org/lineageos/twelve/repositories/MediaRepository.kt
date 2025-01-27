/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.Cache
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.datasources.DummyDataSource
import org.lineageos.twelve.datasources.JellyfinDataSource
import org.lineageos.twelve.datasources.LocalDataSource
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.datasources.SubsonicDataSource
import org.lineageos.twelve.ext.DEFAULT_PROVIDER_KEY
import org.lineageos.twelve.ext.SPLIT_LOCAL_DEVICES_KEY
import org.lineageos.twelve.ext.defaultProvider
import org.lineageos.twelve.ext.preferenceFlow
import org.lineageos.twelve.ext.splitLocalDevices
import org.lineageos.twelve.ext.storageVolumesFlow
import org.lineageos.twelve.models.Provider
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy

/**
 * Media repository. This class coordinates all the providers and their data source.
 * All methods that involves a URI as a parameter will be redirected to the
 * proper data source that can handle the media item. Methods that just returns a list of things
 * will be redirected to the provider selected by the user (see [navigationProvider]).
 * If the navigation provider disappears, the local provider will be used as a fallback.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepository(
    private val context: Context,
    scope: CoroutineScope,
    private val database: TwelveDatabase,
) {
    // System services
    private val storageManager = context.getSystemService(StorageManager::class.java)

    /**
     * Content resolver.
     */
    private val contentResolver = context.contentResolver

    /**
     * Shared preferences.
     */
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Local data source singleton.
     */
    private val localDataSource = LocalDataSource(
        contentResolver,
        MediaStore.VOLUME_EXTERNAL,
        database
    )

    /**
     * All the available real storage volumes.
     */
    private val mediaStoreVolumes = storageManager.storageVolumesFlow()
        .mapLatest { storageVolumes ->
            storageVolumes
                .filter { it.state in storageVolumeMountedStates }
                .filter { it.mediaStoreVolumeName != null }
                .sortedBy { it.isPrimary.not() }
        }
        .distinctUntilChanged()

    private val mediaStoreProviders = combine(
        sharedPreferences.preferenceFlow(
            SPLIT_LOCAL_DEVICES_KEY,
            getter = SharedPreferences::splitLocalDevices,
        ),
        mediaStoreVolumes,
    ) { splitLocalDevices, mediaStoreVolumes ->
        buildList {
            add(
                Provider(
                    ProviderType.LOCAL,
                    LOCAL_PROVIDER_ID,
                    Build.MODEL,
                    !splitLocalDevices,
                ) to localDataSource
            )

            mediaStoreVolumes.forEach {
                val mediaStoreVolumeName = it.mediaStoreVolumeName ?: throw Exception(
                    "MediaStore volume name cannot be null"
                )

                add(
                    Provider(
                        ProviderType.LOCAL,
                        mediaStoreVolumeName.hashCode().toLong(),
                        it.getDescription(context),
                        splitLocalDevices,
                    ) to LocalDataSource(
                        contentResolver,
                        mediaStoreVolumeName,
                        database,
                    )
                )
            }
        }
    }

    /**
     * HTTP cache
     * 50 MB should be enough for most cases.
     */
    private val cache = Cache(context.cacheDir, 50 * 1024 * 1024)

    /**
     * All the providers. This is our single point of truth for the providers.
     */
    private val allProvidersToDataSource = combine(
        mediaStoreProviders,
        database.getSubsonicProviderDao().getAll().mapLatest { subsonicProviders ->
            subsonicProviders.map {
                val arguments = bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to it.url,
                    SubsonicDataSource.ARG_USERNAME.key to it.username,
                    SubsonicDataSource.ARG_PASSWORD.key to it.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            it.useLegacyAuthentication,
                )

                Provider(
                    ProviderType.SUBSONIC,
                    it.id,
                    it.name,
                    true,
                ) to SubsonicDataSource(
                    arguments,
                    { datasource ->
                        database.getLastPlayedDao().get(datasource)
                    }, { datasource, uri ->
                        database.getLastPlayedDao().set(datasource, uri)
                    },
                    cache
                )
            }
        },
        database.getJellyfinProviderDao().getAll().mapLatest { jellyfinProviders ->
            jellyfinProviders.map {
                val arguments = bundleOf(
                    JellyfinDataSource.ARG_SERVER.key to it.url,
                    JellyfinDataSource.ARG_USERNAME.key to it.username,
                    JellyfinDataSource.ARG_PASSWORD.key to it.password,
                )

                Provider(
                    ProviderType.JELLYFIN,
                    it.id,
                    it.name,
                    true,
                ) to JellyfinDataSource(
                    context,
                    arguments,
                    it.deviceIdentifier, {
                        database.getJellyfinProviderDao().getToken(it.id)
                    }, { token ->
                        database.getJellyfinProviderDao().updateToken(it.id, token)
                    }, { datasource ->
                        database.getLastPlayedDao().get(datasource)
                    }, { datasource, uri ->
                        database.getLastPlayedDao().set(datasource, uri)
                    },
                    cache
                )
            }
        }
    ) { providers -> providers.toList().flatten() }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(),
        )

    /**
     * All providers available to the app.
     */
    private val allProviders = allProvidersToDataSource.mapLatest {
        it.map { (provider, _) -> provider }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(),
        )

    /**
     * All providers that the user can be aware of.
     */
    val allVisibleProviders = allProviders
        .mapLatest { it.filter { provider -> provider.visible } }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(),
        )

    /**
     * The current navigation provider's identifiers.
     */
    private val navigationProviderIdentifier = sharedPreferences.preferenceFlow(
        DEFAULT_PROVIDER_KEY, getter = SharedPreferences::defaultProvider
    )
        .flowOn(Dispatchers.IO)
        .shareIn(
            scope,
            SharingStarted.WhileSubscribed(),
        )

    /**
     * The current navigation provider and its data source.
     */
    private val navigationProviderToDataSource = combine(
        navigationProviderIdentifier,
        allProvidersToDataSource,
    ) { navigationProviderIdentifier, allProvidersToDataSource ->
        navigationProviderIdentifier?.let {
            allProvidersToDataSource.firstOrNull { (provider, _) ->
                provider.type == it.type && provider.typeId == it.typeId && provider.visible
            }
        } ?: allProvidersToDataSource.firstOrNull { it.first.visible }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            null,
        )

    /**
     * The current navigation provider. This is used when the user looks for all media types,
     * like the home page, or with the search feature. In case the selected one disappears, the
     * repository will automatically fallback to the first provider available (this usually being
     * the local provider). If no provider is available, this will return null.
     */
    val navigationProvider = navigationProviderToDataSource
        .mapLatest { it?.first }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            null,
        )

    /**
     * The current navigation provider's data source. Even when no provider is available, a dummy
     * data source will be used.
     */
    private val navigationDataSource = navigationProviderToDataSource
        .mapLatest { it?.second ?: DummyDataSource }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            DummyDataSource,
        )

    init {
        scope.launch { gcLocalMediaStats() }
    }

    /**
     * Given a media item, get a flow of the provider that handles these media items' URIs.
     * All URIs must be supported by the same provider to get a valid result.
     *
     * @param uris The media items' URIs
     * @return A flow of the provider that handles these media items' URIs.
     */
    fun providerOfMediaItems(vararg uris: Uri) = allProvidersToDataSource.mapLatest {
        it.firstOrNull { (_, dataSource) ->
            uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
        }?.first
    }

    /**
     * Given a media item, get the provider that handles these media items' URIs.
     * All URIs must be supported by the same provider to get a valid result.
     *
     * @param uris The media items' URIs
     * @return The provider that handles these media items' URIs.
     */
    fun getProviderOfMediaItems(
        vararg uris: Uri
    ) = allProvidersToDataSource.value.firstOrNull { (_, dataSource) ->
        uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
    }?.first

    /**
     * Get a flow of the [Provider].
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @return A flow of the corresponding [Provider].
     */
    fun provider(providerIdentifier: ProviderIdentifier) = allProviders.mapLatest {
        it.firstOrNull { provider ->
            providerIdentifier.type == provider.type && providerIdentifier.typeId == provider.typeId
        }
    }

    /**
     * Get a flow of the [Bundle] containing the arguments. This method should only be used by the
     * provider manager fragment.
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @return A flow of [Bundle] containing the arguments.
     */
    fun providerArguments(providerIdentifier: ProviderIdentifier) = when (providerIdentifier.type) {
        ProviderType.LOCAL -> flowOf(Bundle.EMPTY)

        ProviderType.SUBSONIC -> database.getSubsonicProviderDao().getById(
            providerIdentifier.typeId
        ).mapLatest { subsonicProvider ->
            subsonicProvider?.let {
                bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to it.url,
                    SubsonicDataSource.ARG_USERNAME.key to it.username,
                    SubsonicDataSource.ARG_PASSWORD.key to it.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            it.useLegacyAuthentication,
                )
            }
        }

        ProviderType.JELLYFIN -> database.getJellyfinProviderDao().getById(
            providerIdentifier.typeId
        ).mapLatest { jellyfinProvider ->
            jellyfinProvider?.let {
                bundleOf(
                    JellyfinDataSource.ARG_SERVER.key to it.url,
                    JellyfinDataSource.ARG_USERNAME.key to it.username,
                    JellyfinDataSource.ARG_PASSWORD.key to it.password,
                )
            }
        }
    }

    /**
     * Add a new provider to the database.
     *
     * @param providerType The [ProviderType]
     * @param name The name of the new provider
     * @param arguments The arguments of the new provider. They must have been validated beforehand
     * @return A [Pair] containing the [ProviderType] and the ID of the new provider. You can then
     *   use those values to retrieve the new [Provider]
     */
    suspend fun addProvider(
        providerType: ProviderType, name: String, arguments: Bundle
    ) = when (providerType) {
        ProviderType.LOCAL -> throw Exception("Cannot create local providers")

        ProviderType.SUBSONIC -> {
            val server = arguments.requireArgument(SubsonicDataSource.ARG_SERVER)
            val username = arguments.requireArgument(SubsonicDataSource.ARG_USERNAME)
            val password = arguments.requireArgument(SubsonicDataSource.ARG_PASSWORD)
            val useLegacyAuthentication = arguments.requireArgument(
                SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION
            )

            val typeId = database.getSubsonicProviderDao().create(
                name, server, username, password, useLegacyAuthentication
            )

            providerType to typeId
        }

        ProviderType.JELLYFIN -> {
            val server = arguments.requireArgument(JellyfinDataSource.ARG_SERVER)
            val username = arguments.requireArgument(JellyfinDataSource.ARG_USERNAME)
            val password = arguments.requireArgument(JellyfinDataSource.ARG_PASSWORD)

            val typeId = database.getJellyfinProviderDao().create(
                name, server, username, password
            )

            providerType to typeId
        }
    }

    /**
     * Update an already existing provider.
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @param name The updated name
     * @param arguments The updated arguments
     */
    suspend fun updateProvider(
        providerIdentifier: ProviderIdentifier,
        name: String,
        arguments: Bundle
    ) {
        when (providerIdentifier.type) {
            ProviderType.LOCAL -> throw Exception("Cannot update local providers")

            ProviderType.SUBSONIC -> {
                val server = arguments.requireArgument(SubsonicDataSource.ARG_SERVER)
                val username = arguments.requireArgument(SubsonicDataSource.ARG_USERNAME)
                val password = arguments.requireArgument(SubsonicDataSource.ARG_PASSWORD)
                val useLegacyAuthentication = arguments.requireArgument(
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION
                )

                database.getSubsonicProviderDao().update(
                    providerIdentifier.typeId,
                    name,
                    server,
                    username,
                    password,
                    useLegacyAuthentication,
                )
            }

            ProviderType.JELLYFIN -> {
                val server = arguments.requireArgument(JellyfinDataSource.ARG_SERVER)
                val username = arguments.requireArgument(JellyfinDataSource.ARG_USERNAME)
                val password = arguments.requireArgument(JellyfinDataSource.ARG_PASSWORD)

                database.getJellyfinProviderDao().update(
                    providerIdentifier.typeId,
                    name,
                    server,
                    username,
                    password
                )
            }
        }
    }

    /**
     * Delete a provider.
     *
     * @param providerIdentifier The [ProviderIdentifier]
     */
    suspend fun deleteProvider(providerIdentifier: ProviderIdentifier) {
        when (providerIdentifier.type) {
            ProviderType.LOCAL -> throw Exception("Cannot delete local providers")

            ProviderType.SUBSONIC -> database.getSubsonicProviderDao().delete(
                providerIdentifier.typeId
            )

            ProviderType.JELLYFIN -> database.getJellyfinProviderDao().delete(
                providerIdentifier.typeId
            )
        }
    }

    /**
     * Change the default navigation provider. In case this provider disappears the repository will
     * automatically fallback to the local provider.
     *
     * @param providerIdentifier The new navigation provider identifier
     */
    fun setNavigationProvider(providerIdentifier: ProviderIdentifier) {
        sharedPreferences.defaultProvider = providerIdentifier
    }

    /**
     * Delete all local stats entries.
     */
    suspend fun resetLocalStats() {
        database.getLocalMediaStatsProviderDao().deleteAll()
    }

    /**
     * @see MediaDataSource.status
     */
    fun status(provider: Provider) = withProviderDataSource(provider) {
        status()
    }

    /**
     * @see MediaDataSource.mediaTypeOf
     */
    suspend fun mediaTypeOf(mediaItemUri: Uri) = withMediaItemsDataSource(mediaItemUri) {
        mediaTypeOf(mediaItemUri)
    }

    /**
     * @see MediaDataSource.activity
     */
    fun activity() = navigationDataSource.flatMapLatest { it.activity() }

    /**
     * @see MediaDataSource.albums
     */
    fun albums(
        sortingRule: SortingRule = defaultAlbumsSortingRule,
    ) = navigationDataSource.flatMapLatest {
        it.albums(sortingRule)
    }

    /**
     * @see MediaDataSource.artists
     */
    fun artists(
        sortingRule: SortingRule = defaultArtistsSortingRule,
    ) = navigationDataSource.flatMapLatest { it.artists(sortingRule) }

    /**
     * @see MediaDataSource.genres
     */
    fun genres(
        sortingRule: SortingRule = defaultGenresSortingRule,
    ) = navigationDataSource.flatMapLatest { it.genres(sortingRule) }

    /**
     * @see MediaDataSource.playlists
     */
    fun playlists(
        sortingRule: SortingRule = defaultPlaylistsSortingRule,
    ) = navigationDataSource.flatMapLatest { it.playlists(sortingRule) }

    /**
     * @see MediaDataSource.search
     */
    fun search(query: String) = navigationDataSource.flatMapLatest { it.search(query) }

    /**
     * @see MediaDataSource.audio
     */
    fun audio(audioUri: Uri) = withMediaItemsDataSourceFlow(audioUri) {
        audio(audioUri)
    }

    /**
     * @see MediaDataSource.album
     */
    fun album(albumUri: Uri) = withMediaItemsDataSourceFlow(albumUri) {
        album(albumUri)
    }

    /**
     * @see MediaDataSource.artist
     */
    fun artist(artistUri: Uri) = withMediaItemsDataSourceFlow(artistUri) {
        artist(artistUri)
    }

    /**
     * @see MediaDataSource.genre
     */
    fun genre(genreUri: Uri) = withMediaItemsDataSourceFlow(genreUri) {
        genre(genreUri)
    }

    /**
     * @see MediaDataSource.playlist
     */
    fun playlist(playlistUri: Uri) = withMediaItemsDataSourceFlow(playlistUri) {
        playlist(playlistUri)
    }

    /**
     * @see MediaDataSource.audioPlaylistsStatus
     */
    fun audioPlaylistsStatus(audioUri: Uri) = withMediaItemsDataSourceFlow(audioUri) {
        audioPlaylistsStatus(audioUri)
    }

    /**
     * @see MediaDataSource.createPlaylist
     */
    suspend fun createPlaylist(
        providerIdentifier: ProviderIdentifier, name: String
    ) = getDataSource(providerIdentifier)?.createPlaylist(
        name
    ) ?: RequestStatus.Error(
        MediaError.NOT_FOUND
    )

    /**
     * @see MediaDataSource.renamePlaylist
     */
    suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        withMediaItemsDataSource(playlistUri) {
            renamePlaylist(playlistUri, name)
        }

    /**
     * @see MediaDataSource.deletePlaylist
     */
    suspend fun deletePlaylist(playlistUri: Uri) = withMediaItemsDataSource(playlistUri) {
        deletePlaylist(playlistUri)
    }

    /**
     * @see MediaDataSource.addAudioToPlaylist
     */
    suspend fun addAudioToPlaylist(playlistUri: Uri, audioUri: Uri) =
        withMediaItemsDataSource(playlistUri, audioUri) {
            addAudioToPlaylist(playlistUri, audioUri)
        }

    /**
     * @see MediaDataSource.removeAudioFromPlaylist
     */
    suspend fun removeAudioFromPlaylist(playlistUri: Uri, audioUri: Uri) =
        withMediaItemsDataSource(playlistUri, audioUri) {
            removeAudioFromPlaylist(playlistUri, audioUri)
        }

    /**
     * @see MediaDataSource.onAudioPlayed
     */
    suspend fun onAudioPlayed(audioUri: Uri) =
        withMediaItemsDataSource(audioUri) {
            onAudioPlayed(audioUri)
        }

    /**
     * Get the [MediaDataSource] associated with the given [Provider].
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @return The corresponding [MediaDataSource]
     */
    private fun getDataSource(
        providerIdentifier: ProviderIdentifier,
    ) = allProvidersToDataSource.value.firstOrNull { (provider, _) ->
        providerIdentifier.type == provider.type && providerIdentifier.typeId == provider.typeId
    }?.second

    /**
     * Find the [MediaDataSource] that matches the given [Provider] and call the given predicate on
     * it.
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @return A flow containing the result of the predicate. It will emit a not found error if
     *   no [MediaDataSource] matches the given provider
     */
    private fun <T> withProviderDataSource(
        providerIdentifier: ProviderIdentifier,
        predicate: MediaDataSource.() -> Flow<RequestStatus<T, MediaError>>
    ) = allProvidersToDataSource.flatMapLatest {
        it.firstOrNull { (provider, _) ->
            providerIdentifier.type == provider.type && providerIdentifier.typeId == provider.typeId
        }?.second?.predicate() ?: flowOf(RequestStatus.Error(MediaError.NOT_FOUND))
    }

    /**
     * Find the [MediaDataSource] that handles the given URIs and call the given predicate on it.
     *
     * @param uris The URIs to check
     * @param predicate The predicate to call on the [MediaDataSource]
     * @return A flow containing the result of the predicate. It will emit a not found error if
     *   no [MediaDataSource] can handle the given URIs
     */
    private fun <T> withMediaItemsDataSourceFlow(
        vararg uris: Uri, predicate: MediaDataSource.() -> Flow<RequestStatus<T, MediaError>>
    ) = allProvidersToDataSource.flatMapLatest {
        it.firstOrNull { (_, dataSource) ->
            uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
        }?.second?.predicate() ?: flowOf(RequestStatus.Error(MediaError.NOT_FOUND))
    }

    /**
     * Find the [MediaDataSource] that handles the given URIs and call the given predicate on it.
     *
     * @param uris The URIs to check
     * @param predicate The predicate to call on the [MediaDataSource]
     * @return A [RequestStatus] containing the result of the predicate. It will return a not found
     *   error if no [MediaDataSource] can handle the given URIs
     */
    private suspend fun <T> withMediaItemsDataSource(
        vararg uris: Uri, predicate: suspend MediaDataSource.() -> RequestStatus<T, MediaError>
    ) = allProvidersToDataSource.value.firstOrNull { (_, dataSource) ->
        uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
    }?.second?.predicate() ?: RequestStatus.Error(MediaError.NOT_FOUND)

    companion object {
        private const val LOCAL_PROVIDER_ID = 0L

        /**
         * @see MediaStore.getExternalVolumeNames
         */
        private val storageVolumeMountedStates = arrayOf(
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY,
        )

        val defaultAlbumsSortingRule = SortingRule(
            SortingStrategy.CREATION_DATE, true
        )

        val defaultArtistsSortingRule = SortingRule(
            SortingStrategy.MODIFICATION_DATE, true
        )

        val defaultGenresSortingRule = SortingRule(
            SortingStrategy.NAME
        )

        val defaultPlaylistsSortingRule = SortingRule(
            SortingStrategy.MODIFICATION_DATE, true
        )
    }

    /**
     * Remove items that are no longer in the local data source from the local media stats table.
     */
    private suspend fun gcLocalMediaStats() {
        val statsDao = database.getLocalMediaStatsProviderDao()
        val allStats = statsDao.getAll()
        val inSource = localDataSource.audios().mapLatest { it }.first()

        val removedMedia = buildList {
            allStats.forEach {
                if (inSource.none { audio -> audio.playbackUri == it.mediaUri }) {
                    add(it.mediaUri)
                }
            }
        }

        statsDao.delete(removedMedia)
    }
}
