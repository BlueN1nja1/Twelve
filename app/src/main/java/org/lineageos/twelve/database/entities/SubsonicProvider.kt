/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Subsonic provider entity.
 *
 * @param id The unique ID of this instance
 * @param name The name of this provider
 * @param url The URL of this provider
 * @param username The username of this provider
 * @param password The password of this provider
 * @param useLegacyAuthentication Whether to use legacy authentication
 */
@Entity
data class SubsonicProvider(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "subsonic_provider_id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "use_legacy_authentication") val useLegacyAuthentication: Boolean,
)
