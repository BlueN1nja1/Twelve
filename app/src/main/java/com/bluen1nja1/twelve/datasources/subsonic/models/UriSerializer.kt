/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.datasources.subsonic.models

import android.net.Uri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class UriSerializer : KSerializer<Uri> {
    override val descriptor = PrimitiveSerialDescriptor(
        "Uri", PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): Uri = Uri.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}
