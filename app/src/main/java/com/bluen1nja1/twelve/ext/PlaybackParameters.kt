/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.ext

import androidx.annotation.FloatRange
import androidx.media3.common.PlaybackParameters

fun PlaybackParameters.withPitch(
    @FloatRange(
        from = 0.0,
        fromInclusive = false
    ) pitch: Float
) = PlaybackParameters(speed, pitch)
