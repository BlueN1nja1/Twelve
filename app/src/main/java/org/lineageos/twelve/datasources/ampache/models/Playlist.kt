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
import java.util.Locale

/**
 * Playlist.
 *
 * @param id The ID
 * @param name The name
 * @param owner The owner
 * @param user The user
 * @param items The items
 * @param type The type
 * @param art The artwork URL
 * @param hasAccess Whether the user has access
 * @param hasCollaborate Whether the user has collaborate
 * @param hasArt Whether this playlist has an artwork available
 * @param flag The flag
 * @param rating The rating
 * @param averageRating The average rating
 * @param md5 The md5
 * @param lastUpdate The last update
 */
@Serializable
data class Playlist(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("owner") val owner: String,
    @SerialName("user") val user: User? = null,
    @SerialName("items") val items: Int,
    @SerialName("type") val type: Type,
    @SerialName("art") val art: String,
    @SerialName("has_access") val hasAccess: Boolean,
    @SerialName("has_collaborate") val hasCollaborate: Boolean,
    @SerialName("has_art") val hasArt: Boolean,
    @SerialName("flag") val flag: Boolean,
    @SerialName("rating") val rating: Int?,
    @SerialName("averagerating") val averageRating: Double? = null,
    @SerialName("md5") val md5: String?,
    @SerialName("last_update") val lastUpdate: InstantAsTimestampLong,
) {
    /**
     * Playlist type.
     */
    @Serializable(with = Type.Serializer::class)
    enum class Type(val value: String) {
        PUBLIC("public"),
        PRIVATE("private");

        class Serializer : KSerializer<Type> {
            override val descriptor = PrimitiveSerialDescriptor(
                Serializer::class.qualifiedName!!, PrimitiveKind.STRING
            )

            override fun serialize(
                encoder: Encoder,
                value: Type
            ) {
                encoder.encodeString(value.value)
            }

            override fun deserialize(
                decoder: Decoder
            ) = decoder.decodeString().lowercase(Locale.US).let { value ->
                entries.first { it.value == value }
            }
        }
    }
}
