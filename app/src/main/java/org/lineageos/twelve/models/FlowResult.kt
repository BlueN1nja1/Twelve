/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
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
sealed interface FlowResult<T, E> {
    class Loading<T, E> : FlowResult<T, E>
    class Success<T, E>(val data: T) : FlowResult<T, E>
    class Error<T, E>(val error: E, val throwable: Throwable? = null) : FlowResult<T, E>

    companion object {
        /**
         * Get the data if the result is [Success], null otherwise.
         */
        fun <T, E> FlowResult<T, E>.getOrNull() = when (this) {
            is Loading -> null
            is Success -> data
            is Error -> null
        }

        /**
         * Convert a flow of [Result] to a flow of [FlowResult].
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun <T, E> Flow<Result<out T, out E>>.asFlowResult() = mapLatest {
            when (it) {
                is Result.Success -> Success<T, E>(it.data)
                is Result.Error -> Error(it.error, it.throwable)
            }
        }

        /**
         * Transform the data of a flow of [FlowResult] to a new [FlowResult].
         * When the original flow emits a [Loading] or an [Error] state, the new flow will emit the
         * same result.
         *
         * @see Flow.mapLatest
         */
        @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.mapLatestFlowResult(
            @BuilderInference transform: suspend (value: T) -> FlowResult<R, E>
        ) = mapLatest {
            when (it) {
                is Loading -> Loading()
                is Success -> transform(it.data)
                is Error -> Error(it.error, it.throwable)
            }
        }

        /**
         * Map the data of a flow of [FlowResult].
         * When the original flow emits a [Loading] or an [Error] state, the new flow will emit the
         * same result.
         *
         * @see Flow.mapLatest
         */
        @OptIn(ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.mapLatestData(
            @BuilderInference transform: suspend (value: T) -> R
        ) = mapLatestFlowResult { Success(transform(it)) }

        /**
         * Fold the data of a flow of [FlowResult] to [R].
         * When the original flow emits a [Loading] value, the new flow will emit nothing.
         */
        @OptIn(ExperimentalTypeInference::class)
        fun <T, E, R> Flow<FlowResult<T, E>>.foldLatest(
            @BuilderInference onSuccess: suspend (value: T) -> R,
            @BuilderInference onError: suspend (error: E, throwable: Throwable?) -> R,
        ) = channelFlow {
            this@foldLatest.collectLatest {
                when (it) {
                    is Loading -> {
                        // Do nothing
                    }

                    is Success -> trySend(onSuccess(it.data))
                    is Error -> trySend(onError(it.error, it.throwable))
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
            onError = { error, throwable -> flowOf(Error(error, throwable)) },
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
