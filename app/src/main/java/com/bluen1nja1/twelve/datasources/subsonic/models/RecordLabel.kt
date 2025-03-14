/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * A record label for an album.
 *
 * Note: OpenSubsonic only.
 *
 * @param name The record label name
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class RecordLabel(
    val name: String,
)
