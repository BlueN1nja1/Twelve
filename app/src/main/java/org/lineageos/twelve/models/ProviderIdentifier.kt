/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import kotlinx.serialization.Serializable

/**
 * [Provider] identifier. Two instances are the same if they have the same [typeId] and [type].
 *
 * @param type The provider type
 * @param typeId The ID of the provider relative to the [ProviderType]
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
open class ProviderIdentifier(
    val type: ProviderType,
    val typeId: Long,
) : Comparable<ProviderIdentifier> {
    override fun compareTo(other: ProviderIdentifier) = compareValuesBy(
        this, other,
        ProviderIdentifier::type,
        ProviderIdentifier::typeId,
    )
}
