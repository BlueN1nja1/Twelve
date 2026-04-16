/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import okhttp3.Interceptor
import okhttp3.Response

class JellyfinAuthInterceptor(
    private val tokenGetter: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // If no token is found, simply proceed with the JellyfinAuthenticator
        val token = tokenGetter() ?: return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .header("Authorization", "MediaBrowser Token=\"$token\"")
            .build()

        return chain.proceed(request)
    }
}
