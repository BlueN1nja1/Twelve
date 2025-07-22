/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UUIDSerializer::class)

package org.lineageos.twelve.datasources.jellyfin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.jellyfin.serializers.UUIDSerializer
import java.util.UUID

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class ArtistItem(
    @SerialName("Name") val name: String,
    @SerialName("Id") val id: UUID,
)
