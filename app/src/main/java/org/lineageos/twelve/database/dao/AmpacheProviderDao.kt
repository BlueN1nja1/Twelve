/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.AmpacheProvider
import java.time.Instant

@Dao
interface AmpacheProviderDao {
    /**
     * Add a new Ampache provider to the database.
     */
    @Query(
        """
            INSERT INTO AmpacheProvider (name, url, username, password)
            VALUES (:name, :url, :username, :password)
        """
    )
    suspend fun create(
        name: String,
        url: String,
        username: String,
        password: String,
    ): Long

    /**
     * Update an Ampache provider.
     */
    @Query(
        """
            UPDATE AmpacheProvider
            SET name = :name,
                url = :url,
                username = :username,
                password = :password
            WHERE ampache_provider_id = :ampacheProviderId
        """
    )
    suspend fun update(
        ampacheProviderId: Long,
        name: String,
        url: String,
        username: String,
        password: String,
    )

    /**
     * Delete an Ampache provider from the database.
     */
    @Query("DELETE FROM AmpacheProvider WHERE ampache_provider_id = :ampacheProviderId")
    suspend fun delete(ampacheProviderId: Long)

    /**
     * Fetch all Ampache providers from the database.
     */
    @Query("SELECT * FROM AmpacheProvider")
    fun getAll(): Flow<List<AmpacheProvider>>

    /**
     * Fetch an Ampache provider by its ID from the database.
     */
    @Query("SELECT * FROM AmpacheProvider WHERE ampache_provider_id = :ampacheProviderId")
    fun getById(ampacheProviderId: Long): Flow<AmpacheProvider?>

    /**
     * Fetch the token of an Ampache provider by its ID from the database.
     */
    @Query("SELECT token FROM AmpacheProvider WHERE ampache_provider_id = :ampacheProviderId")
    fun getToken(ampacheProviderId: Long): String?

    /**
     * Fetch the token expiration of an Ampache provider by its ID from the database.
     */
    @Query("SELECT token_expiration FROM AmpacheProvider WHERE ampache_provider_id = :ampacheProviderId")
    fun getTokenExpiration(ampacheProviderId: Long): Instant?

    /**
     * Update the token of an Ampache provider by its ID in the database.
     */
    @Query(
        """
            UPDATE AmpacheProvider
            SET token = :token, token_expiration = :tokenExpiration
            WHERE ampache_provider_id = :ampacheProviderId
        """
    )
    fun updateToken(ampacheProviderId: Long, token: String?, tokenExpiration: Instant?)
}
