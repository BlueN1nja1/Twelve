/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * A genre returned in list of genres for an item.
 *
 * Note: OpenSubsonic only.
 *
 * @param name Genre name
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class ItemGenre(
    val name: String,
)
