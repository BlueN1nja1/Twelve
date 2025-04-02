/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.JellyfinDataSource
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.datasources.SubsonicDataSource

/**
 * Data provider type. This regulates how data should be fetched, usually having a [MediaDataSource]
 * for each one.
 *
 * @param nameStringResId String resource ID of the display name of the provider
 * @param iconDrawableResId The drawable resource ID of the provider
 * @param arguments The arguments of the provider required to start a session. Those will be used
 *   by the providers manager to show the user a dialog to configure the provider
 * @param canBeManaged Whether providers of this type can be configured by the user
 */
enum class ProviderType(
    @StringRes val nameStringResId: Int,
    @DrawableRes val iconDrawableResId: Int,
    val arguments: List<ProviderArgument<*>>,
    val canBeManaged: Boolean,
) {
    /**
     * MediaStore provider.
     */
    MEDIASTORE(
        R.string.provider_type_local,
        R.drawable.ic_smartphone,
        listOf(),
        false,
    ),

    /**
     * Subsonic / OpenSubsonic / Navidrome provider.
     *
     * @see <a href="https://www.subsonic.org/pages/index.jsp">Subsonic home page</a>
     * @see <a href="https://opensubsonic.netlify.app">OpenSubsonic home page</a>
     * @see <a href="https://navidrome.org">Navidrome home page</a>
     */
    SUBSONIC(
        R.string.provider_type_subsonic,
        R.drawable.ic_sailing,
        listOf(
            SubsonicDataSource.ARG_SERVER,
            SubsonicDataSource.ARG_USERNAME,
            SubsonicDataSource.ARG_PASSWORD,
            SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION,
        ),
        true,
    ),

    /**
     * Jellyfin provider.
     *
     * [Home page](https://jellyfin.org)
     */
    JELLYFIN(
        R.string.provider_type_jellyfin,
        R.drawable.ic_jellyfin,
        listOf(
            JellyfinDataSource.ARG_SERVER,
            JellyfinDataSource.ARG_USERNAME,
            JellyfinDataSource.ARG_PASSWORD,
        ),
        true,
    )
}
