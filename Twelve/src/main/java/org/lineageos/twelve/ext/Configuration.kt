/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.res.Configuration

/**
 * Return whether the orientation is [Configuration.ORIENTATION_LANDSCAPE].
 */
val Configuration.isLandscape
    get() = orientation == Configuration.ORIENTATION_LANDSCAPE
