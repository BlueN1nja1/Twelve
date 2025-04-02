/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.content.Context
import android.content.SharedPreferences
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.datasources.JellyfinDataSource
import org.lineageos.twelve.datasources.MediaStoreDataSource
import org.lineageos.twelve.datasources.SubsonicDataSource
import org.lineageos.twelve.ext.SPLIT_LOCAL_DEVICES_KEY
import org.lineageos.twelve.ext.preferenceFlow
import org.lineageos.twelve.ext.splitLocalDevices
import org.lineageos.twelve.ext.storageVolumesFlow
import org.lineageos.twelve.models.Provider
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.ProviderType

@OptIn(ExperimentalCoroutinesApi::class)
class ProvidersRepository(
    context: Context,
    coroutineScope: CoroutineScope,
    private val database: TwelveDatabase,
) {
    /**
     * Shared preferences.
     */
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // MediaStore
    private val storageManager = context.getSystemService(StorageManager::class.java)

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
            when (splitLocalDevices) {
                true -> mediaStoreVolumes.forEach {
                    add(
                        Provider(
                            ProviderType.MEDIASTORE,
                            it.mediaStoreVolumeName.hashCode().toLong(),
                            it.getDescription(context),
                        ) to bundleOf(
                            MediaStoreDataSource.ARG_VOLUME_NAME.key to it.mediaStoreVolumeName,
                        )
                    )
                }

                false -> add(
                    Provider(
                        ProviderType.MEDIASTORE,
                        0L,
                        Build.MODEL,
                    ) to bundleOf(
                        MediaStoreDataSource.ARG_VOLUME_NAME.key to MediaStore.VOLUME_EXTERNAL,
                    )
                )
            }
        }
    }

    // Subsonic
    private val subsonicProviders = database.getSubsonicProviderDao().getAll()
        .mapLatest {
            it.map { provider ->
                Provider(
                    ProviderType.SUBSONIC,
                    provider.id,
                    provider.name,
                ) to bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to provider.url,
                    SubsonicDataSource.ARG_USERNAME.key to provider.username,
                    SubsonicDataSource.ARG_PASSWORD.key to provider.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            provider.useLegacyAuthentication,
                )
            }
        }

    // Jellyfin
    private val jellyfinProviders = database.getJellyfinProviderDao().getAll()
        .mapLatest {
            it.map { provider ->
                Provider(
                    ProviderType.JELLYFIN,
                    provider.id,
                    provider.name,
                ) to bundleOf(
                    JellyfinDataSource.ARG_SERVER.key to provider.url,
                    JellyfinDataSource.ARG_USERNAME.key to provider.username,
                    JellyfinDataSource.ARG_PASSWORD.key to provider.password,
                )
            }
        }

    // All providers
    val allProvidersToArguments = combine(
        mediaStoreProviders,
        subsonicProviders,
        jellyfinProviders,
    ) { it ->
        buildList {
            it.forEach {
                addAll(it)
            }
        }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(
            coroutineScope,
            started = SharingStarted.Eagerly,
            replay = 1,
        )

    val allProviders = allProvidersToArguments
        .mapLatest { allProvidersToArguments ->
            allProvidersToArguments.map { it.first }
        }
        .flowOn(Dispatchers.IO)
        .shareIn(
            coroutineScope,
            started = SharingStarted.Eagerly,
            replay = 1,
        )

    /**
     * Get a flow of the [Provider].
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @return A flow of the corresponding [Provider].
     */
    fun provider(
        providerIdentifier: ProviderIdentifier
    ) = providerToArguments(providerIdentifier).mapLatest { it?.first }

    /**
     * Get a flow of the [Bundle] containing the arguments. This method should only be used by the
     * provider manager fragment.
     *
     * @param providerIdentifier The [ProviderIdentifier]
     * @return A flow of [Bundle] containing the arguments.
     */
    fun providerArguments(
        providerIdentifier: ProviderIdentifier
    ) = providerToArguments(providerIdentifier).mapLatest { it?.second }

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
        ProviderType.MEDIASTORE -> throw Exception("Cannot create MediaStore providers")

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
            ProviderType.MEDIASTORE -> throw Exception("Cannot update MediaStore providers")

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
            ProviderType.MEDIASTORE -> throw Exception("Cannot delete MediaStore providers")

            ProviderType.SUBSONIC -> database.getSubsonicProviderDao().delete(
                providerIdentifier.typeId
            )

            ProviderType.JELLYFIN -> database.getJellyfinProviderDao().delete(
                providerIdentifier.typeId
            )
        }
    }

    private fun providerToArguments(
        providerIdentifier: ProviderIdentifier
    ) = allProvidersToArguments.mapLatest {
        it.firstOrNull { (provider, _) ->
            provider.type == providerIdentifier.type && provider.typeId == providerIdentifier.typeId
        }
    }

    companion object {
        /**
         * @see MediaStore.getExternalVolumeNames
         */
        private val storageVolumeMountedStates = arrayOf(
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY,
        )
    }
}
