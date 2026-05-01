/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Rules for sorting data.
 * Data providers must do a best effort attempt to respect the rules.
 *
 * @param strategy The strategy to use for sorting
 * @param reverse If the data should be sorted in descending order
 */
data class SortingRule(
    val strategy: SortingStrategy,
    val reverse: Boolean = false,
)
