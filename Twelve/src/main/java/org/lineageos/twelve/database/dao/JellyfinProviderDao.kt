/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.JellyfinProvider

@Dao
interface JellyfinProviderDao {
    /**
     * Add a new jellyfin provider to the database.
     */
    @Query(
        """
            INSERT INTO JellyfinProvider (name, url, username, password, device_identifier)
            VALUES (:name, :url, :username, :password, :deviceIdentifier)
        """
    )
    suspend fun create(
        name: String,
        url: String,
        username: String,
        password: String,
        deviceIdentifier: String = generateDeviceIdentifier(),
    ): Long

    /**
     * Update a jellyfin provider.
     */
    @Query(
        """
            UPDATE JellyfinProvider
            SET name = :name,
                url = :url,
                username = :username,
                password = :password,
                token = NULL
            WHERE jellyfin_provider_id = :jellyfinProviderId
        """
    )
    suspend fun update(
        jellyfinProviderId: Long,
        name: String,
        url: String,
        username: String,
        password: String,
    )

    /**
     * Delete a jellyfin provider from the database.
     */
    @Query("DELETE FROM JellyfinProvider WHERE jellyfin_provider_id = :jellyfinProviderId")
    suspend fun delete(jellyfinProviderId: Long)

    /**
     * Fetch all jellyfin providers from the database.
     */
    @Query("SELECT * FROM JellyfinProvider")
    fun getAll(): Flow<List<JellyfinProvider>>

    /**
     * Fetch a jellyfin provider by its ID from the database.
     */
    @Query("SELECT * FROM JellyfinProvider WHERE jellyfin_provider_id = :jellyfinProviderId")
    fun getById(jellyfinProviderId: Long): Flow<JellyfinProvider?>

    /**
     * Fetch the token of a jellyfin provider by its ID from the database.
     */
    @Query("SELECT token FROM JellyfinProvider WHERE jellyfin_provider_id = :jellyfinProviderId")
    fun getToken(jellyfinProviderId: Long): String?

    /**
     * Update the token of a jellyfin provider by its ID in the database.
     */
    @Query(
        """
            UPDATE JellyfinProvider
            SET token = :token
            WHERE jellyfin_provider_id = :jellyfinProviderId
        """
    )
    fun updateToken(jellyfinProviderId: Long, token: String)

    companion object {
        private val allowedSaltChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')

        private fun generateDeviceIdentifier() = (1..20)
            .map { allowedSaltChars.random() }
            .joinToString("")
    }
}
