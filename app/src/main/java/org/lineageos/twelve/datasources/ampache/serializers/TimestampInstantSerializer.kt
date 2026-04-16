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

class TimestampInstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor(
        TimestampInstantSerializer::class.qualifiedName!!, PrimitiveKind.LONG
    )

    override fun deserialize(
        decoder: Decoder
    ): Instant = Instant.ofEpochSecond(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(
            OffsetDateTime.ofInstant(value, ZoneId.of("Z")).toEpochSecond()
        )
    }
}
