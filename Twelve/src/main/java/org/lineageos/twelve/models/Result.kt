/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Result status. This is very similar to Arrow's `Either<A, B>`
 */
sealed interface Result<out T, out E> {
    /**
     * The result is ready.
     *
     * @param data The obtained data
     */
    data class Success<T>(val data: T) : Result<T, Nothing>

    /**
     * The request failed.
     *
     * @param error The error
     * @param throwable An optional [Throwable] object
     */
    data class Failure<E>(val error: E, val throwable: Throwable? = null) : Result<Nothing, E>

    companion object {
        /**
         * Get the data if the result is [Success], null otherwise.
         */
        fun <T, E> Result<T, E>.getOrNull() = when (this) {
            is Success -> data
            is Failure -> null
        }

        /**
         * Map the successful result to another [Result] object.
         * On [Failure], the original [Result] is returned.
         */
        inline fun <T, E, R> Result<T, E>.flatMap(
            mapping: (T) -> Result<R, E>
        ): Result<R, E> = when (this) {
            is Success -> mapping(data)
            is Failure -> this
        }

        /**
         * Map the successful result to another type.
         * On [Failure], the original [Result] is returned.
         */
        inline fun <T, E, R> Result<T, E>.map(
            mapping: (T) -> R
        ): Result<R, E> = flatMap { Success(mapping(it)) }

        /**
         * Execute a block if the result is [Success].
         *
         * @param block The block to execute
         */
        inline fun <R : Result<T, E>, reified T, E> R.onSuccess(
            block: (T) -> Unit,
        ): R = this.also {
            when (this) {
                is Success<*> -> block(data as T)
                is Failure<*> -> Unit
            }
        }

        /**
         * Execute a block if the result is [Failure].
         *
         * @param block The block to execute
         */
        inline fun <R : Result<T, E>, T, reified E> R.onError(
            block: (E) -> Unit,
        ): R = this.also {
            when (this) {
                is Success<*> -> Unit
                is Failure<*> -> block(error as E)
            }
        }
    }
}
