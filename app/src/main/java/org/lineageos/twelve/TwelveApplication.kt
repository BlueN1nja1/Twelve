/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.app.Application
import androidx.media3.common.util.UnstableApi
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.MainScope
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.repositories.MediaRepository
import org.lineageos.twelve.repositories.ResumptionPlaylistRepository
import org.lineageos.twelve.ui.coil.ThumbnailMapper

@androidx.annotation.OptIn(UnstableApi::class)
class TwelveApplication : Application(), SingletonImageLoader.Factory {
    private val database by lazy { TwelveDatabase.getInstance(applicationContext) }
    val mediaRepository by lazy { MediaRepository(applicationContext, MainScope(), database) }
    val resumptionPlaylistRepository by lazy { ResumptionPlaylistRepository(database) }

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(this)
        .components {
            add(ThumbnailMapper)
        }
        .build()
}
