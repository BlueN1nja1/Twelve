/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.view.View
import android.view.inputmethod.InputMethodManager

private const val SHOW_REQUEST_TIMEOUT = 1000

private fun InputMethodManager.scheduleShowSoftInput(
    view: View,
    flags: Int,
    runnable: Runnable,
    showRequestTime: Long,
) {
    if (!view.hasFocus()
        || (showRequestTime + SHOW_REQUEST_TIMEOUT) <= System.currentTimeMillis()
    ) {
        return
    }

    if (showSoftInput(view, flags)) {
        return
    } else {
        view.removeCallbacks(runnable)
        view.postDelayed(runnable, 50)
    }
}

private fun InputMethodManager.scheduleHideSoftInput(
    view: View,
    flags: Int,
    runnable: Runnable,
    showRequestTime: Long,
) {
    if ((showRequestTime + SHOW_REQUEST_TIMEOUT) <= System.currentTimeMillis()) {
        return
    }

    if (hideSoftInputFromWindow(view.windowToken, flags)) {
        return
    } else {
        view.removeCallbacks(runnable)
        view.postDelayed(runnable, 50)
    }
}

/**
 * @see InputMethodManager.showSoftInput
 */
fun InputMethodManager.scheduleShowSoftInput(view: View, flags: Int) {
    val currentTimeMillis = System.currentTimeMillis()

    val runnable = object : Runnable {
        override fun run() {
            scheduleShowSoftInput(view, flags, this, currentTimeMillis)
        }
    }

    runnable.run()
}

/**
 * @see InputMethodManager.hideSoftInputFromWindow
 */
fun InputMethodManager.scheduleHideSoftInput(view: View, flags: Int) {
    val currentTimeMillis = System.currentTimeMillis()

    val runnable = object : Runnable {
        override fun run() {
            scheduleHideSoftInput(view, flags, this, currentTimeMillis)
        }
    }

    runnable.run()
}
