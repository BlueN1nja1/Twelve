/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Maps a list of elements asynchronously.
 */
suspend fun <T, R> Iterable<T>.mapAsync(
    transform: suspend (T) -> R,
): List<R> = coroutineScope {
    map {
        async {
            transform(it)
        }
    }.awaitAll()
}
