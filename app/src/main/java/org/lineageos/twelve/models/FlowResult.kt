/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlin.experimental.ExperimentalTypeInference

/**
 * A data holder used for flows.
 */
sealed interface FlowResult<out T, out E> {
    /**
     * The result is loading.
     */
    data object Loading : FlowResult<Nothing, Nothing>

    /**
     * The result is ready.
     *
     * @param data The obtained data
     */
    data class Success<T>(val data: T) : FlowResult<T, Nothing>

    /**
     * The request failed.
     *
     * @param error The error
     * @param throwable An optional [Throwable] object
     */
    data class Failure<E>(val error: E, val throwable: Throwable? = null) : FlowResult<Nothing, E>

    companion object {
        /**
         * Get the data if the result is [Success], null otherwise.
         */
        fun <T, E> FlowResult<T, E>.getOrNull() = when (this) {
            is Loading -> null
            is Success -> data
            is Failure -> null
        }

        /**
         * Convert a flow of [Result] to a flow of [FlowResult].
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun <T, E> Flow<Result<T, E>>.asFlowResult() = mapLatest {
            when (it) {
                is Result.Success -> Success(it.data)
                is Result.Failure -> Failure(it.error, it.throwable)
            }
        }

        /**
         * Transform the data of a flow of [FlowResult] to a new [FlowResult].
         * When the original flow emits a [Loading] or an [Failure] state, the new flow will emit
         * the same result.
         *
         * @see Flow.mapLatest
         */
        @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.mapLatestFlowResult(
            transform: suspend (value: T) -> FlowResult<R, E>
        ) = mapLatest {
            when (it) {
                is Loading -> it
                is Success -> transform(it.data)
                is Failure -> it
            }
        }

        /**
         * Map the data of a flow of [FlowResult].
         * When the original flow emits a [Loading] or an [Failure] state, the new flow will emit
         * the same result.
         *
         * @see Flow.mapLatest
         */
        @OptIn(ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.mapLatestData(
            transform: suspend (value: T) -> R
        ) = mapLatestFlowResult { Success(transform(it)) }

        /**
         * Fold the data of a flow of [FlowResult] to [R].
         * When the original flow emits a [Loading] value, the new flow will emit nothing.
         */
        @OptIn(ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.foldLatest(
            onSuccess: suspend (value: T) -> R,
            onError: suspend (error: E, throwable: Throwable?) -> R,
        ) = channelFlow {
            this@foldLatest.collectLatest {
                when (it) {
                    is Loading -> {
                        // Do nothing
                    }

                    is Success -> send(onSuccess(it.data))
                    is Failure -> send(onError(it.error, it.throwable))
                }
            }
        }

        /**
         * Fold the data of a flow of [FlowResult] to a flow of [R].
         * When the original flow emits a [Loading] value, the new flow will emit nothing.
         */
        @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.flatMapLatestData(
            @BuilderInference onSuccess: suspend (value: T) -> Flow<FlowResult<R, E>>,
        ) = foldLatest(
            onSuccess = { onSuccess(it) },
            onError = { error, throwable -> flowOf(Failure(error, throwable)) },
        ).flatMapLatest { it }

        /**
         * Map the [FlowResult] to the data or null.
         */
        fun <T, E> Flow<FlowResult<T, E>>.mapLatestDataOrNull() = foldLatest(
            onSuccess = { it },
            onError = { _, _ -> null },
        )
    }
}
