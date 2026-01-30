/*
 * SPDX-FileCopyrightText: 2022-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams

fun View.slide() {
    if (isVisible) {
        slideDown()
    } else {
        slideUp()
    }
}

fun View.slideUp() {
    if (isVisible) {
        return
    }

    isVisible = true

    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

    startAnimation(
        AnimationSet(true).apply {
            addAnimation(
                TranslateAnimation(
                    0f, 0f, measuredHeight.toFloat(), 0f
                ).apply {
                    duration = 250
                }
            )
            addAnimation(
                AlphaAnimation(0.0f, 1.0f).apply {
                    duration = 250
                }
            )
        }
    )
}

fun View.slideDown() {
    if (!isVisible) {
        return
    }

    isVisible = false

    startAnimation(
        AnimationSet(true).apply {
            addAnimation(
                TranslateAnimation(0f, 0f, 0f, height.toFloat()).apply {
                    duration = 200
                }
            )
            addAnimation(
                AlphaAnimation(1.0f, 0.0f).apply {
                    duration = 200
                }
            )
        }
    )
}

/**
 * Return whether [View.getLayoutDirection] is [View.LAYOUT_DIRECTION_RTL].
 */
val View.isRtl: Boolean
    get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

/**
 * Updates the padding of the view based on the insets.
 * Layout direction is taken into account.
 *
 * @param insets The insets to apply
 * @param start Whether the start padding should be applied
 * @param top Whether the top padding should be applied
 * @param end Whether the end padding should be applied
 * @param bottom Whether the bottom padding should be applied
 */
fun View.updatePadding(
    insets: Insets,
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) {
    val (left, right) = when (isRtl) {
        true -> end to start
        false -> start to end
    }

    setPadding(
        insets.left.takeIf { left } ?: paddingLeft,
        insets.top.takeIf { top } ?: paddingTop,
        insets.right.takeIf { right } ?: paddingRight,
        insets.bottom.takeIf { bottom } ?: paddingBottom
    )
}

/**
 * Updates the margin of the view based on the insets.
 * Layout direction is taken into account.
 *
 * @param insets The insets to apply
 * @param start Whether the start padding should be applied
 * @param top Whether the top padding should be applied
 * @param end Whether the end padding should be applied
 * @param bottom Whether the bottom padding should be applied
 */
fun View.updateMargin(
    insets: Insets,
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) {
    val (left, right) = when (isRtl) {
        true -> end to start
        false -> start to end
    }

    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        leftMargin = insets.left.takeIf { left } ?: leftMargin
        topMargin = insets.top.takeIf { top } ?: topMargin
        rightMargin = insets.right.takeIf { right } ?: rightMargin
        bottomMargin = insets.bottom.takeIf { bottom } ?: bottomMargin
    }
}

/**
 * @see toPx
 */
fun View.toPx(dp: Int) = resources.displayMetrics.toPx(dp)
