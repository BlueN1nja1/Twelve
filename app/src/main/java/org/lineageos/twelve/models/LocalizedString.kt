/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.content.Context
import androidx.annotation.StringRes
import org.lineageos.twelve.R
import kotlin.reflect.safeCast

sealed interface LocalizedString {
    fun getString(context: Context): String
    fun areContentsTheSame(other: LocalizedString): Boolean

    class StringLocalizedString(
        private val value: String,
    ) : LocalizedString {
        override fun getString(context: Context) = value

        override fun areContentsTheSame(other: LocalizedString) = areContentsTheSame(other) {
            this.value == it.value
        }
    }

    class StringResIdLocalizedString(
        @StringRes private val stringResId: Int,
        private val stringResIdArgs: List<Any>? = null,
    ) : LocalizedString {
        override fun getString(context: Context) = stringResIdArgs?.let { stringResIdArgs ->
            context.getString(stringResId, *stringResIdArgs.toTypedArray())
        } ?: context.getString(stringResId)

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
