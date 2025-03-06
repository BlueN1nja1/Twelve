/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class InstantConverter {
    @TypeConverter
    fun fromString(value: String?) = value?.let { OffsetDateTime.parse(it).toInstant() }

    @TypeConverter
    fun toString(value: Instant?) = value?.let {
        OffsetDateTime.ofInstant(value, ZoneId.of("Z")).toString()
    }
}
