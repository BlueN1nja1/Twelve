/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class Iso8601InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor(
        Iso8601InstantSerializer::class.qualifiedName!!, PrimitiveKind.STRING
    )

    override fun deserialize(
        decoder: Decoder
    ): Instant = OffsetDateTime.parse(decoder.decodeString()).toInstant()

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(
            OffsetDateTime.ofInstant(value, ZoneId.of("Z")).toString()
        )
    }
}
