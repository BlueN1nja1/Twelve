/*
 * SPDX-FileCopyrightText: 2023-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import kotlin.reflect.KClass

fun <T : Parcelable> Parcel.readParcelable(clazz: KClass<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readParcelable(clazz.java.classLoader, clazz.java)
    } else {
        @Suppress("DEPRECATION")
        readParcelable(clazz.java.classLoader)
    }

fun <T : Serializable> Parcel.readSerializable(clazz: KClass<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readSerializable(clazz.java.classLoader, clazz.java)
    } else {
        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        readSerializable() as T?
    }
