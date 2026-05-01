/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Base class for all of the app's app widget providers.
 *
 * @param updater The widget updater
 */
abstract class BaseAppWidgetProvider<T : AppWidgetProvider>(
    private val updater: AppWidgetUpdater<T>,
) : AppWidgetProvider() {
    final override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        MainScope().launch {
            updater.update(
                context ?: return@launch,
                appWidgetManager ?: return@launch,
                appWidgetIds ?: return@launch,
            )
        }
    }
}
