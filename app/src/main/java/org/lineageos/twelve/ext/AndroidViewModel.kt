/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel

val AndroidViewModel.applicationContext: Context
    get() = getApplication<Application>().applicationContext

val AndroidViewModel.resources: Resources
    get() = getApplication<Application>().resources
