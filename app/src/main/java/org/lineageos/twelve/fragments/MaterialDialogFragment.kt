/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * A [DialogFragment] that uses [MaterialAlertDialogBuilder] to build the base dialog.
 */
abstract class MaterialDialogFragment(
    @LayoutRes contentLayoutId: Int,
) : DialogFragment(contentLayoutId) {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setView(onCreateView(layoutInflater, null, savedInstanceState))
            .show()
}
