/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Error.
 *
 * @param code The error code
 * @param message The optional error message
 * @param helpUrl A URL (documentation, configuration, etc) which may provide additional context for
 *   the error)
 */
@Serializable
data class Error(
    val code: Code,
    val message: String? = null,

    // OpenSubsonic
    val helpUrl: String? = null,
) {
    /**
     * Subsonic error code.
     */
    @Serializable(with = Code.Serializer::class)
    enum class Code(val code: Int) {
        /**
         * A generic error.
         */
        GENERIC_ERROR(0),

        /**
         * Required parameter is missing.
         */
        REQUIRED_PARAMETER_MISSING(10),

        /**
         * Incompatible Subsonic REST protocol version. Client must upgrade.
         */
        OUTDATED_CLIENT(20),

        /**
         * Incompatible Subsonic REST protocol version. Server must upgrade.
         */
        OUTDATED_SERVER(30),

        /**
         * Wrong username or password.
         */
        WRONG_CREDENTIALS(40),

        /**
         * Token authentication not supported for LDAP users.
         *
         * Note: Some third party server implementations uses this to just signal that they need
         * legacy authentication.
         */
        TOKEN_AUTHENTICATION_NOT_SUPPORTED(41),

        /**
         * Provided authentication mechanism not supported.
         *
         * Note: OpenSubsonic only.
         */
        AUTHENTICATION_MECHANISM_NOT_SUPPORTED(42),

        /**
         * Multiple conflicting authentication mechanisms provided.
         *
         * Note: OpenSubsonic only.
         */
        MULTIPLE_CONFLICTING_AUTHENTICATION_MECHANISMS(43),

        /**
         * Invalid API key.
         *
         * Note: OpenSubsonic only.
         */
        INVALID_API_KEY(44),

        /**
         * User is not authorized for the given operation.
         */
        USER_NOT_AUTHORIZED(50),

        /**
         * The trial period for the Subsonic server is over. Please upgrade to Subsonic Premium.
         * Visit subsonic.org for details.
         */
        SUBSONIC_PREMIUM_TRIAL_ENDED(60),

        /**
         * The requested data was not found.
         */
        NOT_FOUND(70);

        class Serializer : KSerializer<Code> {
            override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.INT)

            override fun deserialize(decoder: Decoder) = decoder.decodeInt().let {
                Code.fromCode(it) ?: throw SerializationException("Unknown code $it")
            }

            override fun serialize(encoder: Encoder, value: Code) {
                encoder.encodeInt(value.code)
            }
        }

        companion object {
            fun fromCode(code: Int) = entries.firstOrNull { it.code == code }
        }
    }
}
