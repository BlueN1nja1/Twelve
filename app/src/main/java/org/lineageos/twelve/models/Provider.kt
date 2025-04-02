/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import kotlinx.parcelize.Parcelize
import org.lineageos.twelve.datasources.MediaDataSource

/**
 * A provider instance. The [type] determines how data should be retrieved from the provider.
 * Each provider has an associated [MediaDataSource] and related arguments, but those are not
 * exposed outside of the media repository.
 *
 * @param type The provider type
 * @param typeId The ID of the provider relative to the [ProviderType]
 * @param name The name of the provider given by the user
 */
@Parcelize
class Provider(
    override val type: ProviderType,
    override val typeId: Long,
    val name: String,
) : ProviderIdentifier(type, typeId), UniqueItem<Provider> {
    override fun areItemsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::type,
        Provider::typeId,
    ) == 0

    override fun areContentsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::name,
    ) == 0
}
