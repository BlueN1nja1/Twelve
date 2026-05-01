/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.os.Bundle
import androidx.annotation.StringRes
import org.lineageos.twelve.R
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Argument of a provider.
 *
 * @param T The type of the argument
 * @param key The key of the argument
 * @param type The [KClass] of the argument
 * @param nameStringResId The string resource ID of the argument name
 * @param required Whether this argument is required
 * @param hidden Whether the value of this argument should be hidden
 * @param defaultValue The default value of the argument
 * @param validate A lambda to validate the value of the argument, returning a
 *   [ProviderArgument.ValidationError] if the value is invalid, null otherwise
 */
data class ProviderArgument<T : Any>(
    val key: String,
    val type: KClass<T>,
    @StringRes val nameStringResId: Int,
    val required: Boolean,
    val hidden: Boolean,
    val defaultValue: T? = null,
    val validate: ((T) -> ValidationError?) = { null },
) {
    /**
     * Validation error of a [ProviderArgument].
     *
     * @param message The error message
     * @param messageStringResId The localized error message string resource ID
     */
    data class ValidationError(
        val message: String,
        @StringRes val messageStringResId: Int,
    )

    companion object {
        private val requiredValidationError = ValidationError(
            "A value is required",
            R.string.provider_argument_validation_error_required,
        )

        /**
         * Get the argument value from a [Bundle] or the default value if it is not present.
         */
        private fun <T : Any> Bundle.getArgumentValue(
            providerArguments: ProviderArgument<T>
        ) = when (containsKey(providerArguments.key)) {
            true -> when (providerArguments.type) {
                String::class -> getString(providerArguments.key)
                Boolean::class -> getBoolean(providerArguments.key)
                else -> throw Exception("Unsupported type")
            }?.let {
                providerArguments.type.cast(it)
            }

            false -> providerArguments.defaultValue
        }

        /**
         * Get the optional argument from a [Bundle]. This will also validate the value and throw
         * an exception if the value is invalid.
         */
        fun <T : Any> Bundle.getArgument(
            providerArguments: ProviderArgument<T>
        ) = getArgumentValue(providerArguments)?.also { argumentValue ->
            providerArguments.validate(argumentValue)?.let {
                throw Exception(
                    "Validation error for argument ${providerArguments.key}: ${it.message}"
                )
            }
        }

        /**
         * Get the required argument from a [Bundle]. This will also validate the value and throw
         * an exception if the value is invalid.
         */
        fun <T : Any> Bundle.requireArgument(
            providerArguments: ProviderArgument<T>
        ) = getArgument(providerArguments) ?: throw Exception("Argument not found")

        /**
         * Validate the argument in this [Bundle] and return a [ValidationError] if it is invalid.
         * Will also check if the argument is required.
         */
        fun <T : Any> Bundle.validateArgument(
            providerArguments: ProviderArgument<T>
        ) = getArgumentValue(providerArguments).let { argumentValue ->
            argumentValue?.let {
                providerArguments.validate(it)
            } ?: requiredValidationError.takeIf {
                providerArguments.required && argumentValue == null
            }
        }
    }
}
