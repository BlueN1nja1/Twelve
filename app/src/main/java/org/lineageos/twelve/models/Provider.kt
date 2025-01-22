/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import org.lineageos.twelve.datasources.MediaDataSource

/**
 * A provider instance. The [type] determines how data should be retrieved from the provider.
 * Each provider has an associated [MediaDataSource] and related arguments, but those are not
 * exposed outside of the media repository.
 *
 * @param type The provider type
 * @param typeId The ID of the provider relative to the [ProviderType]
 * @param name The name of the provider given by the user
 * @param visible Whether the user should be aware of it
 */
class Provider(
    type: ProviderType,
    typeId: Long,
    val name: String,
    val visible: Boolean,
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
        Provider::visible,
    ) == 0
}
