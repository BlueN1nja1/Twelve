/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.ClipData

fun ClipData.asArray() = buildList {
    for (i in 0 until itemCount) {
        add(getItemAt(i))
    }
}
