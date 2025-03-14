/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.ext

import android.widget.AutoCompleteTextView

fun AutoCompleteTextView.selectItem(position: Int = 0) {
    setText(adapter.getItem(position).toString(), false)
    showDropDown()
    setSelection(position)
    listSelection = position
    performCompletion()
    dismissDropDown()
}
