/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.net.Uri

/**
 * Check if this URI is relative to the given URI.
 *
 * @param other The URI to check against
 * @return True if this URI is relative to the given URI
 */
fun Uri.isRelativeTo(other: Uri): Boolean {
    if (scheme != other.scheme) {
        return false
    }

    if (authority != other.authority) {
        return false
    }

    val pathSegments = pathSegments
    val otherPathSegments = other.pathSegments

    if (pathSegments.size < otherPathSegments.size) {
        return false
    }

    for (i in otherPathSegments.indices) {
        if (pathSegments[i] != otherPathSegments[i]) {
            return false
        }
    }

    return true
}
