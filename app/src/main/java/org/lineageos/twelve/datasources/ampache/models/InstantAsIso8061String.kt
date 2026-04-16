/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.Serializable
import org.lineageos.twelve.datasources.ampache.serializers.Iso8601InstantSerializer
import java.time.Instant

typealias InstantAsIso8061String = @Serializable(with = Iso8601InstantSerializer::class) Instant
