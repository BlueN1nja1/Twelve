/*
 * SPDX-FileCopyrightText: 2024-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import org.lineageos.twelve.datasources.MediaDataSource

/**
 * A provider instance. The [type] determines how data should be retrieved from the provider.
 * Each provider has an associated [MediaDataSource] and related arguments, but those are not
 * exposed outside of the media repository.
 *
 * @param identifier The provider identifier
 * @param name The name of the provider given by the user
 */
data class Provider(
    val identifier: ProviderIdentifier,
    val name: String,
) : UniqueItem<Provider> {
    constructor(
        type: ProviderType,
        typeId: Long,
        name: String,
    ) : this(
        identifier = ProviderIdentifier(type, typeId),
        name = name,
    )

    /**
     * @see ProviderIdentifier.type
     */
    val type by identifier::type

    /**
     * @see ProviderIdentifier.typeId
     */
    val typeId by identifier::typeId

    override fun areItemsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::identifier,
    ) == 0

    override fun areContentsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::name,
    ) == 0
}
