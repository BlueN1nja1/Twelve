/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import org.lineageos.twelve.datasources.MediaDataSource

/**
 * [MediaDataSource] information.
 *
 * @param key Unique key describing this information
 * @param keyLocalizedString The key's [LocalizedString] that will be shown to the user
 * @param value The value's [LocalizedString] that will be shown to the user
 */
data class DataSourceInformation(
    val key: String,
    val keyLocalizedString: LocalizedString,
    val value: LocalizedString,
) : UniqueItem<DataSourceInformation> {
    override fun areItemsTheSame(other: DataSourceInformation) = this.key == other.key

    override fun areContentsTheSame(other: DataSourceInformation) =
        this.keyLocalizedString.areContentsTheSame(
            other.keyLocalizedString
        ) && this.value.areContentsTheSame(
            other.value
        )
}
