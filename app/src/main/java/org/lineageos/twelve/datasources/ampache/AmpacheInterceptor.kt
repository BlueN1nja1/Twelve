/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache

import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.readBomAsCharset
import org.lineageos.twelve.datasources.ampache.models.Error
import org.lineageos.twelve.datasources.ampache.models.Handshake
import org.lineageos.twelve.models.Result
import java.time.Instant
import kotlin.reflect.KMutableProperty0

/**
 * Interceptor for Ampache that handles authentication and convert error responses to HTTP errors.
 */
class AmpacheInterceptor(
    private val username: String,
    private val password: String,
    applicationName: String,
    tokenProperty: KMutableProperty0<Pair<String, Instant>?>,
) : Interceptor {
    private val baseParameters = listOf(
        "version" to AmpacheClient.API_VERSION,
        "client" to applicationName,
    )

    private var token by tokenProperty

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val okHttpClient = OkHttpClient()
    private val handshakeMutex: Mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        // Inject the basic auth parameters
        val request = chain.request().newBuilder()
            .url(
                chain.request().url.newBuilder()
                    .addQueryParameters(baseParameters)
                    .build()
            )
            .build()

        // Get the token, or make a new one if missing or expired
        val (token, isNewToken) = getToken(request) ?: return makeTokenFailureResponse(request)

        // Make the request
        val requestWithToken = request.injectToken(token)
        val response = okHttpClient.newCall(requestWithToken).execute()

        // Return the response if not an error
        val error = parseError(response) as? Result.Error ?: return response

        return when (error.error.error.errorCode) {
            is Error.Information.Code.InvalidHandshake -> {
                // Do not retry the request with a recent invalid token
                if (isNewToken) {
                    Log.i(
                        LOG_TAG,
                        "Despite getting a new token, the server still rejected it"
                    )
                    return response
                }

                // We have to retry the request with a new token
                val (token, _) = getNewToken(request) ?: return makeTokenFailureResponse(request)

                // Make the request with the new token
                val requestWithToken = request.injectToken(token)
                val retryResponse = okHttpClient.newCall(requestWithToken).execute()

                // Return the response if not an error
                val retryError = parseError(retryResponse) as? Result.Error ?: return retryResponse

                // Return the error response
                makeErrorResponse(request, retryError.error)
            }

            else -> {
                Log.e(LOG_TAG, "Got error response from server: ${error.error}")
                makeErrorResponse(request, error.error)
            }
        }
    }

    private fun makeTokenFailureResponse(request: Request) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(401)
        .message("Authentication required")
        .body("Failed getting a token".toResponseBody(null))
        .build()

    private fun makeErrorResponse(request: Request, error: Error) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(
            when (error.error.errorCode) {
                is Error.Information.Code.AccessControlNotEnabled -> 405
                is Error.Information.Code.InvalidHandshake -> 401
                is Error.Information.Code.AccessDenied -> 403
                is Error.Information.Code.NotFound -> 404
                is Error.Information.Code.Missing -> 405
                is Error.Information.Code.Deprecated -> 410
                is Error.Information.Code.BadRequest -> 400
                is Error.Information.Code.FailedAccessCheck -> 403
                is Error.Information.Code.Other -> 500
            }
        )
        .message("API error")
        .body(error.error.errorMessage.toResponseBody(null))
        .build()

    private fun Request.injectToken(token: String) = newBuilder()
        .url(url.newBuilder().setQueryParameter("auth", token).build())
        .build()

    /**
     * Get the token, or make a new one if missing or expired.
     *
     * @param request The request to get the token for
     * @return A pair of the token and whether it was retrieved from the server
     */
    private fun getToken(request: Request): Pair<String, Boolean>? {
        val token = this.token ?: return getNewToken(request)

        // Check for expired token
        val now = Instant.now()
        if (now > token.second) {
            return getNewToken(request)
        }

        return token.first to false
    }

    /**
     * Get a new token from the server. This also handles concurrency, so that the first call that
     * wins the mutex race gets to make the new token, while the others wait for it and retrieves it
     * and go on with their request.
     *
     * @return A pair of the token and whether it was retrieved from the server
     */
    private fun getNewToken(request: Request) = runBlocking {
        return@runBlocking if (handshakeMutex.tryLock()) {
            // We're now responsible for creating a new token
            try {
                token = null

                val url = request.url.newBuilder()
                    .query(null)
                    .addQueryParameter("action", "handshake")
                    .addQueryParameters(baseParameters)
                    .addQueryParameters(getHandshakeParameters())
                    .build()

                val response = okHttpClient.newCall(
                    request.newBuilder()
                        .url(url)
                        .build()
                ).execute()

                // Handle "{"error":""}" responses
                when (val result = parseError(response)) {
                    is Result.Success -> {
                        // Try to parse the response
                        val jsonObject = json.decodeFromString<Handshake>(result.data)

                        val token = jsonObject.auth
                        val expires = jsonObject.sessionExpire

                        // Save the token info
                        this@AmpacheInterceptor.token = token to expires

                        token to true
                    }

                    is Result.Error -> {
                        Log.i(LOG_TAG, "Cannot get new token: ${result.error}")
                        null
                    }
                }
            } finally {
                handshakeMutex.unlock()
            }
        } else {
            // Wait until the lock is released, then use the new token
            handshakeMutex.withLock {
                val token = this@AmpacheInterceptor.token ?: return@runBlocking null

                // Check for expired token
                val now = Instant.now()
                if (now > token.second) {
                    return@runBlocking null
                }

                token.first to true
            }
        }
    }

    /**
     * Get the base parameters.
     */
    private fun getHandshakeParameters() = buildList {
        val instant = Instant.now()
        val passphrase = AmpacheClient.calculatePassphrase(password, instant)

        add("auth" to passphrase)
        add("timestamp" to instant.epochSecond)
        add("user" to username)
    }

    /**
     * Parse the response and return [Result.Error] if the response contains an [Error] object.
     */
    private fun parseError(response: Response): Result<String, Error> {
        if (!response.isSuccessful) {
            return Result.Error(
                Error(
                    Error.Information(
                        Error.Information.Code.Other(response.code),
                        null,
                        Error.Information.Type.Other("${response.code}"),
                        response.message
                    )
                )
            )
        }

        val body = response.body ?: return Result.Error(
            Error(
                Error.Information(
                    Error.Information.Code.Other(response.code),
                    null,
                    Error.Information.Type.Other("${response.code}"),
                    "Empty body"
                )
            )
        )

        // Do not consume the response
        val string = body.source().let { source ->
            source.peek().readString(charset = source.readBomAsCharset(Charsets.UTF_8))
        }

        runCatching {
            val error = json.decodeFromString<Error>(string)

            return Result.Error(error)
        }

        return Result.Success(string)
    }

    private fun HttpUrl.Builder.addQueryParameters(
        queryParameters: List<Pair<String, Any>>
    ) = apply {
        queryParameters.forEach { (key, value) ->
            addQueryParameter(key, value.toString())
        }
    }

    companion object {
        private val LOG_TAG = AmpacheInterceptor::class.simpleName!!
    }
}
