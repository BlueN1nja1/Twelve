/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Version.Serializer::class)
data class Version(
    val major: Int,
    val minor: Int,
    val revision: Int,
) {
    val value = "$major.$minor.$revision"

    override fun toString() = value

    class Serializer : KSerializer<Version> {
        override val descriptor = PrimitiveSerialDescriptor(
            "Version", PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder) = fromValue(decoder.decodeString())

        override fun serialize(encoder: Encoder, value: Version) {
            encoder.encodeString(value.value)
        }
    }

    companion object {
        fun fromValue(value: String) = value.split('.')
            .map { it.toInt() }
            .let { (major, minor, revision) -> Version(major, minor, revision) }
    }
}
