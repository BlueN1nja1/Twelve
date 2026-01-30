/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import coil3.Image
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.maxBitmapSize
import coil3.size.Size
import coil3.target.Target
import coil3.toBitmap
import kotlin.reflect.KClass

/**
 * A helper class used to update a widget.
 */
abstract class AppWidgetUpdater<T : AppWidgetProvider>(
    private val appWidgetProviderKClass: KClass<T>,
    @LayoutRes val layoutResId: Int,
) {
    abstract suspend fun RemoteViews.update(context: Context)

    suspend fun update(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            // Get the layout for the widget
            val views = RemoteViews(context.packageName, layoutResId).apply {
                update(context)
            }

            // Tell the AppWidgetManager to perform an update on the current widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    suspend fun update(context: Context) = AppWidgetManager.getInstance(context).let {
        update(
            context,
            it,
            it.getAppWidgetIds(
                ComponentName(context, appWidgetProviderKClass.java)
            ),
        )
    }

    suspend fun RemoteViews.fetchImage(
        context: Context,
        data: Any,
        @IdRes imageViewResId: Int
    ) {
        val imageLoader = context.imageLoader

        val imageRequest = ImageRequest.Builder(context)
            .target(RemoteViewsTarget(this, imageViewResId))
            .data(data)
            .maxBitmapSize(Size(512, 512))
            .allowHardware(false)
            .build()

        imageLoader.execute(imageRequest)
    }

    private class RemoteViewsTarget(
        private val remoteViews: RemoteViews,
        @IdRes private val imageViewResId: Int
    ) : Target {
        override fun onStart(placeholder: Image?) = setDrawable(placeholder)
        override fun onError(error: Image?) = setDrawable(error)
        override fun onSuccess(result: Image) = setDrawable(result)
        private fun setDrawable(image: Image?) {
            remoteViews.setImageViewBitmap(imageViewResId, image?.toBitmap())
        }
    }
}
