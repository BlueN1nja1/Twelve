/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Request status for flows.
 */
sealed class RequestStatus<T, E> {
    /**
     * Result is not ready yet.
     *
     * @param progress An optional percentage of the request progress
     */
    class Loading<T, E>(
        @androidx.annotation.IntRange(from = 0, to = 100) val progress: Int? = null
    ) : RequestStatus<T, E>()

    /**
     * The result is ready.
     *
     * @param data The obtained data
     */
    class Success<T, E>(val data: T) : RequestStatus<T, E>()

    /**
     * The request failed.
     *
     * @param error The error
     */
    class Error<T, E>(val error: E, val throwable: Throwable? = null) : RequestStatus<T, E>()

    companion object {
        /**
         * Map the result to another type.
         */
        fun <T, E, R> RequestStatus<T, E>.map(
            mapping: (T) -> R
        ): RequestStatus<R, E> = when (this) {
            is Loading -> Loading(progress)
            is Success -> Success(mapping(data))
            is Error -> Error(error, throwable)
        }

        /**
         * Fold the request status.
         */
        fun <T, E, R> RequestStatus<T, E>.fold(
            onLoading: (Int?) -> R,
            onSuccess: (T) -> R,
            onError: (E) -> R,
        ): R = when (this) {
            is Loading -> onLoading(progress)
            is Success -> onSuccess(data)
            is Error -> onError(error)
        }
    }
}
