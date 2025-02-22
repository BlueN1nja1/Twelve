/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller

/**
 * [Source](https://stackoverflow.com/a/53756296)
 */
open class CenterSmoothScroller(context: Context?) : LinearSmoothScroller(context) {
    final override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ) = (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
}
