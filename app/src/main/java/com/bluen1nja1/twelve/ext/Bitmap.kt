/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.ext

import android.graphics.Bitmap
import java.nio.ByteBuffer

fun Bitmap.toByteArray(): ByteArray = ByteBuffer.allocate(rowBytes * height).apply {
    copyPixelsToBuffer(this)
}.array()
