/*
 * SPDX-FileCopyrightText: 2025-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable
import org.lineageos.twelve.ext.readSerializable

/**
 * [Provider] identifier. Two instances are the same if they have the same [typeId] and [type].
 *
 * @param type The provider type
 * @param typeId The ID of the provider relative to the [ProviderType]
 */
@Serializable
data class ProviderIdentifier(
    val type: ProviderType,
    val typeId: Long,
) : Comparable<ProviderIdentifier>, Parcelable {
    override fun compareTo(other: ProviderIdentifier) = compareValuesBy(
        this, other,
        ProviderIdentifier::type,
        ProviderIdentifier::typeId,
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(type)
        dest.writeLong(typeId)
    }

    companion object CREATOR : Parcelable.Creator<ProviderIdentifier> {
        override fun createFromParcel(source: Parcel?) = source?.let {
            ProviderIdentifier(
                type = it.readSerializable(ProviderType::class)!!,
                typeId = it.readLong(),
            )
        }

        override fun newArray(size: Int) = arrayOfNulls<ProviderIdentifier>(size)
    }
}
