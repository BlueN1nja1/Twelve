/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistsID3(
    val index: List<IndexID3>,
    val ignoredArticles: String,

    // Navidrome
    val lastModified: Long? = null, // TODO
)
