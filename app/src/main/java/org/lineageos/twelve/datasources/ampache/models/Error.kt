/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Error.
 *
 * @param error The [Error.Information]
 */
@Serializable
data class Error(
    @SerialName("error") val error: Information,
) {
    /**
     * Error information.
     *
     * @param errorCode The error code
     * @param errorAction The action that caused this error
     * @param errorType The type of the error
     * @param errorMessage The error message
     */
    @Serializable
    data class Information(
        @SerialName("errorCode") val errorCode: Code,
        @SerialName("errorAction") val errorAction: String?,
        @SerialName("errorType") val errorType: Type,
        @SerialName("errorMessage") val errorMessage: String,
    ) {
        @Serializable(with = Code.Serializer::class)
        sealed class Code(open val value: Int) {
            /**
             * Access Control not Enabled.
             *
             * The API is disabled. Enable 'access_control' in your config.
             */
            data object AccessControlNotEnabled : Code(4700)

            /**
             * Received Invalid Handshake.
             *
             * This is a temporary error, this means no valid session was passed or the handshake
             * failed.
             */
            data object InvalidHandshake : Code(4701)

            /**
             * Access Denied.
             *
             * The requested method is not available.
             * You can check the error message for details about which feature is disabled.
             */
            data object AccessDenied : Code(4703)

            /**
             * Not Found.
             *
             * The API could not find the requested object.
             */
            data object NotFound : Code(4704)

            /**
             * Missing.
             *
             * This is a fatal error, the service requested a method that the API does not
             * implement.
             */
            data object Missing : Code(4705)

            /**
             * Depreciated.
             *
             * This is a fatal error, the method requested is no longer available.
             */
            data object Deprecated : Code(4706)

            /**
             * Bad Request.
             *
             * Used when you have specified a valid method but something about the input is
             * incorrect, invalid or missing.
             * You can check the error message for details, but do not re-attempt the exact same
             * request.
             */
            data object BadRequest : Code(4710)

            /**
             * Failed Access Check.
             *
             * Access denied to the requested object or function for this user.
             */
            data object FailedAccessCheck : Code(4742)

            /**
             * Unknown error.
             */
            data class Other(override val value: Int) : Code(value)

            class Serializer : KSerializer<Code> {
                override val descriptor = PrimitiveSerialDescriptor(
                    Serializer::class.qualifiedName!!, PrimitiveKind.STRING
                )

                override fun serialize(
                    encoder: Encoder,
                    value: Code,
                ) {
                    encoder.encodeString(value.value.toString())
                }

                override fun deserialize(
                    decoder: Decoder,
                ) = decoder.decodeString().toInt().let { value ->
                    when (value) {
                        AccessControlNotEnabled.value -> AccessControlNotEnabled
                        InvalidHandshake.value -> InvalidHandshake
                        AccessDenied.value -> AccessDenied
                        NotFound.value -> NotFound
                        Missing.value -> Missing
                        Deprecated.value -> Deprecated
                        BadRequest.value -> BadRequest
                        FailedAccessCheck.value -> FailedAccessCheck
                        else -> Other(value)
                    }
                }
            }
        }

        @Serializable(with = Type.Serializer::class)
        sealed class Type(open val value: String) {
            /**
             * Account errors are things that your user can't do. Either the permission level or
             * something with the session is incorrect.
             */
            data object Account : Type("account")

            /**
             * System errors tell you whether a system feature is disabled or something else has
             * failed on the server. Check the debug logs for further information.
             */
            data object System : Type("system")

            /**
             * Everything else will be a parameter from the call that caused your error. Maybe the
             * email you used was malformed or the song you looked for doesn't exist?
             */
            data class Other(override val value: String) : Type(value)

            class Serializer : KSerializer<Type> {
                override val descriptor = PrimitiveSerialDescriptor(
                    Serializer::class.qualifiedName!!, PrimitiveKind.STRING
                )

                override fun serialize(
                    encoder: Encoder,
                    value: Type,
                ) {
                    encoder.encodeString(value.value)
                }

                override fun deserialize(
                    decoder: Decoder,
                ) = when (val value = decoder.decodeString()) {
                    Account.value -> Account
                    System.value -> System
                    else -> Other(value)
                }
            }
        }
    }
}
