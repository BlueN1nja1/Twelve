/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class License(
    val valid: Boolean,
    val email: String? = null,
    val licenseExpires: InstantAsString? = null,
    val trialExpires: InstantAsString? = null,
)
