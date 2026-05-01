/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.UiThread
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * Inspired by [ContentLoadingProgressBar].
 */
class FullscreenLoadingProgressBar : FrameLayout {
    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int = 0,
        @StyleRes defStyleRes: Int = 0,
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val circularProgressIndicator = CircularProgressIndicator(context).apply {
        isIndeterminate = true
    }

    private var startTime = -1L
    private var postedHide = false
    private var postedShow = false
    private var dismissed = false

    private val delayedHide = Runnable {
        postedHide = false
        startTime = -1
        isVisible = false
    }

    private val delayedShow = Runnable {
        postedShow = false
        if (!dismissed) {
            startTime = System.currentTimeMillis()
            isVisible = true
        }
    }

    init {
        setBackgroundColor(0x55000000)

        addView(
            circularProgressIndicator,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        setOnClickListener {
            // Do nothing
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeCallbacks()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks()
    }

    @UiThread
    fun show() {
        // Reset the start time.
        startTime = -1
        dismissed = false
        removeCallbacks(delayedHide)
        postedHide = false
        if (!postedShow) {
            postDelayed(delayedShow, DELAY_MS)
            postedShow = true
        }
    }

    @UiThread
    fun hide() {
        dismissed = true
        removeCallbacks(delayedShow)
        postedShow = false
        val diff: Long = System.currentTimeMillis() - startTime
        if (diff >= SHOW_TIME_MS || startTime == -1L) {
            // The progress spinner has been shown long enough
            // OR was not shown yet. If it wasn't shown yet,
            // it will just never be shown.
            visibility = GONE
        } else {
            // The progress spinner is shown, but not long enough,
            // so put a delayed message in to hide it when its been
            // shown long enough.
            if (!postedHide) {
                postDelayed(delayedHide, SHOW_TIME_MS - diff)
                postedHide = true
            }
        }
    }

    suspend fun withProgress(block: suspend () -> Unit) {
        show()
        block()
        hide()
    }

    private fun removeCallbacks() {
        removeCallbacks(delayedHide)
        removeCallbacks(delayedShow)
    }

    companion object {
        private const val SHOW_TIME_MS = 500L
        private const val DELAY_MS = 500L
    }
}
