/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.SubsonicProvider

@Dao
interface SubsonicProviderDao {
    /**
     * Add a new subsonic provider to the database.
     */
    @Query(
        """
            INSERT INTO SubsonicProvider (name, url, username, password, use_legacy_authentication)
            VALUES (:name, :url, :username, :password, :useLegacyAuthentication)
        """
    )
    suspend fun create(
        name: String,
        url: String,
        username: String,
        password: String,
        useLegacyAuthentication: Boolean,
    ): Long

    /**
     * Update a subsonic provider.
     */
    @Query(
        """
            UPDATE SubsonicProvider
            SET name = :name,
                url = :url,
                username = :username,
                password = :password,
                use_legacy_authentication = :useLegacyAuthentication
            WHERE subsonic_provider_id = :subsonicProviderId
        """
    )
    suspend fun update(
        subsonicProviderId: Long,
        name: String,
        url: String,
        username: String,
        password: String,
        useLegacyAuthentication: Boolean,
    )

    /**
     * Delete a subsonic provider from the database.
     */
    @Query("DELETE FROM SubsonicProvider WHERE subsonic_provider_id = :subsonicProviderId")
    suspend fun delete(subsonicProviderId: Long)

    /**
     * Fetch all subsonic providers from the database.
     */
    @Query("SELECT * FROM SubsonicProvider")
    fun getAll(): Flow<List<SubsonicProvider>>

    /**
     * Fetch a subsonic provider by its ID from the database.
     */
    @Query("SELECT * FROM SubsonicProvider WHERE subsonic_provider_id = :subsonicProviderId")
    fun getById(subsonicProviderId: Long): Flow<SubsonicProvider?>
}
