/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.models

/**
 * Information regarding an audio stream.
 *
 * @param sampleRate The sample rate of the stream.
 * @param channelCount The channel count of the stream.
 * @param encoding The [Encoding] of the stream.
 */
data class AudioStreamInformation(
    val sampleRate: Int?,
    val channelCount: Int?,
    val encoding: Encoding?,
)
