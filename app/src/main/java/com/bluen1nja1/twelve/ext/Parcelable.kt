/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.ext

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import kotlin.reflect.KClass

fun <T : Parcelable> Parcel.readParcelable(clazz: KClass<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readParcelable(clazz.java.classLoader, clazz.java)
    } else {
        @Suppress("DEPRECATION")
        readParcelable(clazz.java.classLoader)
    }
