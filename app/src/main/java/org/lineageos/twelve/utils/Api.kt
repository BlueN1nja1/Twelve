/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.RequestStatus
import java.net.SocketTimeoutException

class Api(val okHttpClient: OkHttpClient, private val serverUri: Uri) {
    val json = Json {
        ignoreUnknownKeys = true
    }

    // GET method
    suspend inline fun <reified T> get(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
    ): MethodResult<T> {
        val url = buildUrl(path, queryParameters)
        val request = buildRequest(url, "GET")
        return executeRequest<T>(request)
    }

    // POST method
    suspend inline fun <reified T, reified E> post(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
        data: T? = null,
        emptyResponse: () -> E = { Unit as E }
    ): MethodResult<E> {
        val url = buildUrl(path, queryParameters)
        val body = encodeRequestBody(data)
        val request = buildRequest(url, "POST", body)
        return executeRequest<E>(request, emptyResponse)
    }

    // DELETE method
    suspend inline fun <reified T> delete(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
    ): MethodResult<T> {
        val url = buildUrl(path, queryParameters)
        val request = buildRequest(url, "DELETE")
        return executeRequest(request) { Unit as T }
    }

    fun buildUrl(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList()
    ) = serverUri.buildUpon().apply {
        path.forEach {
            appendPath(it)
        }
        queryParameters.forEach { (key, value) ->
            value?.let { appendQueryParameter(key, it.toString()) }
        }
    }.build().toString()

    inline fun <reified T> encodeRequestBody(data: T?) = data?.let {
        json.encodeToString(it)
    }?.toRequestBody("application/json".toMediaType()) ?: "".toRequestBody()

    fun buildRequest(url: String, method: String, body: RequestBody? = null) =
        Request.Builder().url(url).method(method, body).build()

    // Helper function to execute the request
    suspend inline fun <reified T> executeRequest(
        request: Request,
        onEmptyResponse: () -> T = {
            throw IllegalStateException("No onEmptyResponse() provided, but the response is empty")
        }
    ) = runCatching {
        okHttpClient.newCall(request).executeAsync().let { response ->
            if (response.isSuccessful) {
                response.body?.use { body ->
                    val string = body.string()
                    if (string.isEmpty()) {
                        MethodResult.Success(onEmptyResponse())
                    } else {
                        MethodResult.Success(json.decodeFromString<T>(string))
                    }
                } ?: MethodResult.Success(onEmptyResponse())
            } else {
                MethodResult.HttpError(response.code, Throwable(response.message))
            }
        }
    }.fold(
        onSuccess = { it },
        onFailure = { e ->
            when (e) {
                is SocketTimeoutException -> MethodResult.HttpError(408, e)
                is SerializationException -> MethodResult.DeserializationError(e)
                is CancellationException -> MethodResult.CancellationError(e)
                else -> MethodResult.GenericError(e)
            }
        },
    )
}

sealed interface MethodResult<T> {
    data class Success<T>(val result: T) : MethodResult<T>
    data class HttpError<T>(val code: Int, val error: Throwable? = null) : MethodResult<T>
    data class GenericError<T>(val error: Throwable? = null) : MethodResult<T>
    data class DeserializationError<T>(val error: Throwable? = null) : MethodResult<T>
    data class CancellationError<T>(val error: Throwable? = null) : MethodResult<T>
    data class InvalidResponse<T>(val error: Throwable? = null) : MethodResult<T>
}

suspend fun <T, O> MethodResult<T>.toRequestStatus(
    resultGetter: suspend T.() -> O
): RequestStatus<O, MediaError> = when (this) {
    is MethodResult.Success -> RequestStatus.Success(result.resultGetter())

    is MethodResult.HttpError -> RequestStatus.Error(
        when (code) {
            401 -> MediaError.AUTHENTICATION_REQUIRED
            403 -> MediaError.INVALID_CREDENTIALS
            404 -> MediaError.NOT_FOUND
            else -> MediaError.IO
        },
        error
    )

    is MethodResult.DeserializationError -> RequestStatus.Error(MediaError.DESERIALIZATION, error)
    is MethodResult.CancellationError -> RequestStatus.Error(MediaError.CANCELLED, error)
    is MethodResult.InvalidResponse -> RequestStatus.Error(MediaError.INVALID_RESPONSE, error)
    is MethodResult.GenericError -> RequestStatus.Error(MediaError.IO, error)
}

suspend fun <T, O> MethodResult<T>.toResult(
    resultGetter: suspend T.() -> O
) = when (this) {
    is MethodResult.Success -> result.resultGetter()
    else -> null
}
