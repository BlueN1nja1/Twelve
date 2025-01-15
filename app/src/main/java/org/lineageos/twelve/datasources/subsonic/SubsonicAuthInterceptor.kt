/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic

import okhttp3.HttpUrl
import okhttp3.Interceptor
import org.lineageos.twelve.datasources.subsonic.models.Version
import java.security.MessageDigest

class SubsonicAuthInterceptor(
    private val version: Version,
    private val username: String,
    private val password: String,
    private val clientName: String,
    private val useLegacyAuthentication: Boolean = false,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request().newBuilder()
            .url(
                chain.request().url.addQueryParameters(getAuthParameters())
            )
            .build()
    )

    fun getAuthParameters() = buildList {
        add("u" to username)
        add("v" to version.value)
        add("c" to clientName)
        add("f" to PROTOCOL_JSON)
        if (!useLegacyAuthentication) {
            val salt = generateSalt()
            add("t" to getSaltedPassword(password, salt))
            add("s" to salt)
        } else {
            add("p" to password)
        }
    }

    private fun HttpUrl.addQueryParameters(queryParameters: List<Pair<String, Any>>) =
        newBuilder().apply {
            queryParameters.forEach { (key, value) ->
                addQueryParameter(key, value.toString())
            }
        }.build()

    companion object {
        private const val PROTOCOL_JSON = "json"

        private val md5MessageDigest = MessageDigest.getInstance("MD5")

        private val allowedSaltChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')

        private fun generateSalt() = (1..20)
            .map { allowedSaltChars.random() }
            .joinToString("")

        private fun getSaltedPassword(password: String, salt: String) = md5MessageDigest.digest(
            password.toByteArray() + salt.toByteArray()
        ).toString()
    }
}
