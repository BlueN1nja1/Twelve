/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.dialogs

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.scheduleShowSoftInput

/**
 * Basic edit text dialog builder. Add a positive button with a listener that receives the text
 * with [setPositiveButton]. Call [show] instead of [create]
 */
class EditTextMaterialAlertDialogBuilder(
    context: Context, overrideThemeResId: Int = 0
) : MaterialAlertDialogBuilder(context, overrideThemeResId) {
    // Views
    private lateinit var editText: EditText

    // System services
    private val inputMethodManager = context.getSystemService(InputMethodManager::class.java)

    // Values
    private var text: String? = null
    private var hint: String? = null
    private var positiveListener: ((text: String) -> Unit)? = null

    init {
        setView(R.layout.alert_dialog_edit_text)
    }

    fun setText(text: String?) = apply {
        this.text = text
    }

    fun setHint(hint: String?) = apply {
        this.hint = hint
    }

    fun setPositiveButton(
        textId: Int,
        listener: ((text: String) -> Unit)?
    ) = super.setPositiveButton(textId) { _, _ ->
        positiveListener?.invoke(editText.text.toString())
    }.also {
        positiveListener = listener
    }

    fun setPositiveButton(
        text: CharSequence?,
        listener: ((text: String) -> Unit)?
    ) = super.setPositiveButton(text) { _, _ ->
        positiveListener?.invoke(editText.text.toString())
    }.also {
        positiveListener = listener
    }

    override fun show(): AlertDialog = super.show().also {
        editText = it.findViewById(R.id.editText)!!
        text?.let { text ->
            editText.setText(text)
        }
        hint?.let { hint ->
            editText.hint = hint
        }

        editText.setOnEditorActionListener { _, _, _ ->
            it.dismiss()
            positiveListener?.invoke(editText.text.toString())

            true
        }

        editText.requestFocus()
        inputMethodManager.scheduleShowSoftInput(editText, 0)
    }
}
