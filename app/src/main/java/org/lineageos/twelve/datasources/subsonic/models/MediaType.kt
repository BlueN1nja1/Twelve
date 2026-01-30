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

@Serializable(with = MediaType.Serializer::class)
enum class MediaType(val value: String) {
    MUSIC("music"),
    PODCAST("podcast"),
    AUDIOBOOK("audiobook"),
    VIDEO("video");

    class Serializer : KSerializer<MediaType> {
        override val descriptor = PrimitiveSerialDescriptor(
            "ResponseStatus", PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder) = decoder.decodeString().let {
            fromValue(it) ?: throw SerializationException("Unknown MediaType value $it")
        }

        override fun serialize(encoder: Encoder, value: MediaType) {
            encoder.encodeString(value.value)
        }
    }

    companion object {
        fun fromValue(value: String) = entries.firstOrNull { it.value == value }
    }
}
