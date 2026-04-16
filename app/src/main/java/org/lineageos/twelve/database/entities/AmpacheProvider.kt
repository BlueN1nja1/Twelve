/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Ampache provider entity.
 *
 * @param id The unique ID of this instance
 * @param name The name of the provider
 * @param url The URL of the provider
 * @param username The username to use for authentication
 * @param password The password to use for authentication
 * @param token The token to use for authentication
 * @param tokenExpiration The token expiration
 */
@Entity
data class AmpacheProvider(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ampache_provider_id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "token") val token: String? = null,
    @ColumnInfo(name = "token_expiration") val tokenExpiration: Instant? = null,
)
