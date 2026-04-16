/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.Serializable
import org.lineageos.twelve.datasources.ampache.serializers.TimestampInstantSerializer
import java.time.Instant

typealias InstantAsTimestampLong = @Serializable(with = TimestampInstantSerializer::class) Instant
