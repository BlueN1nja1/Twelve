/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable
import java.time.Instant

typealias InstantAsString = @Serializable(with = InstantSerializer::class) Instant
