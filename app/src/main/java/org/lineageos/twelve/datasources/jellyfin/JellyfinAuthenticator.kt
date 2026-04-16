/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import android.os.Build
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.lineageos.twelve.datasources.jellyfin.models.AuthenticateUser
import org.lineageos.twelve.datasources.jellyfin.models.AuthenticateUserResult
import org.lineageos.twelve.ext.executeAsync

class JellyfinAuthenticator(
    serverUri: Uri,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String,
    private val tokenGetter: () -> String?,
    private val tokenSetter: (String) -> Unit,
) : Authenticator {
    private val mutex = Mutex()
    private val okHttpClient = OkHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val authenticationUrl = serverUri.buildUpon()
        .appendPath("Users")
        .appendPath("AuthenticateByName")
        .build()
        .toString()

    // This block is only run in case the request got a 401 error code
    override fun authenticate(route: Route?, response: Response) = runBlocking {
        val token = tokenGetter()

        mutex.withLock {
            val newToken = tokenGetter()

            // Ensure no other request has updated the token
            // If it did we assume the new token is valid
            if (newToken != null && newToken != token) {
                return@runBlocking response.request.newBuilder()
                    .header("Authorization", "MediaBrowser Token=\"$newToken\"")
                    .build()
            }

            // Either the previous token expired or we didn't have a token
            getNewAccessToken()?.let {
                tokenSetter(it)

                return@runBlocking response.request.newBuilder()
                    .header("Authorization", "MediaBrowser Token=\"$newToken\"")
                    .build()
            }
        }

        // If we reach this point, we couldn't get a new token
        null
    }

    private fun getNewAccessToken() = runBlocking {
        val response = runCatching {
            okHttpClient.newCall(
                Request.Builder()
                    .url(authenticationUrl)
                    .headers(getAuthenticationRequestHeaders())
                    .post(
                        json.encodeToString(
                            AuthenticateUser(username, password)
                        ).toRequestBody("application/json".toMediaType())
                    )
                    .build()
            ).executeAsync()
        }.fold(
            onSuccess = { it },
            onFailure = { return@runBlocking null }
        )

        if (!response.isSuccessful) {
            return@runBlocking null
        }

        val authResponse = response.body?.use { body ->
            json.decodeFromString<AuthenticateUserResult>(body.string())
        } ?: return@runBlocking null

        authResponse.accessToken
    }

    private fun getAuthenticationRequestHeaders() = Headers.Builder().apply {
        add(
            "Authorization",
            "MediaBrowser Client=\"${packageName}\", " +
                    "Device=\"${Build.MODEL}\", " +
                    "DeviceId=\"${deviceIdentifier}\", " +
                    "Version=\"${JellyfinClient.JELLYFIN_API_VERSION}\""
        )
    }.build()
}
