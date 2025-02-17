/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.content.res.Resources
import androidx.annotation.StringRes
import org.lineageos.twelve.R
import kotlin.reflect.safeCast

sealed interface LocalizedString {
    fun getString(resources: Resources): String
    fun areContentsTheSame(other: LocalizedString): Boolean

    class StringLocalizedString(
        private val value: String,
    ) : LocalizedString {
        override fun getString(resources: Resources) = value

        override fun areContentsTheSame(other: LocalizedString) = areContentsTheSame(other) {
            this.value == it.value
        }
    }

    class StringResIdLocalizedString(
        @StringRes private val stringResId: Int,
        private val stringResIdArgs: List<Any>? = null,
    ) : LocalizedString {
        override fun getString(resources: Resources) = stringResIdArgs?.let { stringResIdArgs ->
            resources.getString(stringResId, *stringResIdArgs.toTypedArray())
        } ?: resources.getString(stringResId)

        override fun areContentsTheSame(
            other: LocalizedString
        ) = areContentsTheSame(other) {
            this.stringResId == it.stringResId
                    && this.stringResIdArgs?.toTypedArray().contentEquals(
                it.stringResIdArgs?.toTypedArray()
            )
        }
    }

    companion object {
        private inline fun <reified T : LocalizedString> T.areContentsTheSame(
            other: LocalizedString, comparator: T.(other: T) -> Boolean,
        ) = T::class.safeCast(other)?.let { this.comparator(it) } ?: false

        fun of(value: Boolean?) = StringResIdLocalizedString(
            when (value) {
                true -> R.string.yes
                false -> R.string.no
                null -> R.string.unknown
            }
        )
    }
}
