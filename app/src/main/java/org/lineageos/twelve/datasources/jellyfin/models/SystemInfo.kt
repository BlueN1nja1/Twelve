/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class SystemInfo(
    @SerialName("LocalAddress") val localAddress: String?,
    @SerialName("ServerName") val serverName: String?,
    @SerialName("Version") val version: String?,
    @SerialName("ProductName") val productName: String?,
    @SerialName("OperatingSystem") val operatingSystem: String?,
    @SerialName("Id") val id: String?,
    @SerialName("StartupWizardCompleted") val startupWizardCompleted: Boolean?
)
