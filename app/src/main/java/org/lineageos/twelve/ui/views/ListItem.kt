/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import org.lineageos.twelve.R

/**
 * A poor man's Material Design 3 ListItem implementation.
 * @see <a href="https://m3.material.io/components/lists/overview">Material Design 3 docs</a>
 */
class ListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
) : MaterialCardView(context, attrs, defStyleAttr) {
    private val headlineTextView by lazy { findViewById<TextView>(R.id.headlineTextView) }
    private val leadingIconImageView by lazy { findViewById<ImageView>(R.id.leadingIconImageView) }
    private val leadingTextView by lazy { findViewById<TextView>(R.id.leadingTextView) }
    private val leadingViewContainerFrameLayout by lazy { findViewById<FrameLayout>(R.id.leadingViewContainerFrameLayout) }
    private val supportingTextView by lazy { findViewById<TextView>(R.id.supportingTextView) }
    private val trailingIconImageView by lazy { findViewById<ImageView>(R.id.trailingIconImageView) }
    private val trailingSupportingTextView by lazy { findViewById<TextView>(R.id.trailingSupportingTextView) }
    private val trailingViewContainerFrameLayout by lazy { findViewById<FrameLayout>(R.id.trailingViewContainerFrameLayout) }

    private var cardCornerRadius: Float = 0f

    var leadingIconImage: Drawable?
        get() = leadingIconImageView.drawable
        set(value) {
            leadingIconImageView.setImageAndUpdateVisibility(value)
        }

    var leadingText: CharSequence?
        get() = leadingTextView.text
        set(value) {
            leadingTextView.setTextAndUpdateVisibility(value)
        }

    var leadingView: View?
        get() = leadingViewContainerFrameLayout.getChildAt(0)
        set(value) {
            leadingViewContainerFrameLayout.setChildAndUpdateVisibility(value)
        }

    var leadingViewIsVisible: Boolean
        get() = leadingViewContainerFrameLayout.isVisible
        set(value) {
            leadingViewContainerFrameLayout.updateVisibility(value)
        }

    var headlineText: CharSequence?
        get() = headlineTextView.text
        set(value) {
            headlineTextView.setTextAndUpdateVisibility(value)
        }

    var supportingText: CharSequence?
        get() = supportingTextView.text
        set(value) {
            supportingTextView.setTextAndUpdateVisibility(value)
        }

    var trailingIconImage: Drawable?
        get() = trailingIconImageView.drawable
        set(value) {
            trailingIconImageView.setImageAndUpdateVisibility(value)
        }

    var trailingSupportingText: CharSequence?
        get() = trailingSupportingTextView.text
        set(value) {
            trailingSupportingTextView.setTextAndUpdateVisibility(value)
        }

    var trailingView: View?
        get() = trailingViewContainerFrameLayout.getChildAt(0)
        set(value) {
            trailingViewContainerFrameLayout.setChildAndUpdateVisibility(value)
        }

    var trailingViewIsVisible: Boolean
        get() = trailingViewContainerFrameLayout.isVisible
        set(value) {
            leadingViewContainerFrameLayout.updateVisibility(value)
        }

    var isDimmed: Boolean
        get() = !headlineTextView.isEnabled
        set(value) = setViewsProperty(View::setEnabled, !value)

    var hasRoundedCorners: Boolean = false
        set(value) {
            field = value

            super.setRadius(
                when (value) {
                    true -> cardCornerRadius
                    false -> 0f
                }
            )
        }

    init {
        setCardBackgroundColor(
            resources.getColorStateList(R.color.list_item_background, context.theme)
        )
        cardElevation = 0f
        radius = 0f
        strokeWidth = 0

        inflate(context, R.layout.list_item, this)

        context.obtainStyledAttributes(
            attrs, androidx.cardview.R.styleable.CardView, defStyleAttr, 0
        ).use {
            cardCornerRadius = it.getDimension(
                androidx.cardview.R.styleable.CardView_cardCornerRadius,
                resources.getDimension(R.dimen.list_item_default_corner_radius)
            )
        }

        context.obtainStyledAttributes(attrs, R.styleable.ListItem, 0, 0).apply {
            try {
                leadingIconImage = getDrawable(R.styleable.ListItem_leadingIconImage)
                leadingText = getString(R.styleable.ListItem_leadingText)
                getResourceId(R.styleable.ListItem_leadingViewLayout, 0).takeUnless {
                    it == 0
                }?.let {
                    setLeadingView(it)
                }
                leadingViewIsVisible = getBoolean(R.styleable.ListItem_leadingViewIsVisible, true)
                headlineText = getString(R.styleable.ListItem_headlineText)
                supportingText = getString(R.styleable.ListItem_supportingText)
                trailingIconImage = getDrawable(R.styleable.ListItem_trailingIconImage)
                trailingSupportingText = getString(R.styleable.ListItem_trailingSupportingText)
                getResourceId(R.styleable.ListItem_trailingViewLayout, 0).takeUnless {
                    it == 0
                }?.let {
                    setTrailingView(it)
                }
                trailingViewIsVisible = getBoolean(R.styleable.ListItem_trailingViewIsVisible, true)
                isDimmed = getBoolean(R.styleable.ListItem_isDimmed, false)
                hasRoundedCorners = getBoolean(R.styleable.ListItem_hasRoundedCorners, false)
            } finally {
                recycle()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        setViewsProperty(View::setEnabled, enabled)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        setViewsProperty(View::setSelected, selected)
    }

    override fun setRadius(radius: Float) {
        super.setRadius(radius)

        cardCornerRadius = radius
    }

    fun setHeadlineText(@StringRes resId: Int) = headlineTextView.setTextAndUpdateVisibility(resId)
    fun setHeadlineText(@StringRes resId: Int, vararg formatArgs: Any) =
        headlineTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    fun setLeadingIconImage(bm: Bitmap) = leadingIconImageView.setImageAndUpdateVisibility(bm)
    fun setLeadingIconImage(icon: Icon) = leadingIconImageView.setImageAndUpdateVisibility(icon)
    fun setLeadingIconImage(@DrawableRes resId: Int) =
        leadingIconImageView.setImageAndUpdateVisibility(resId)

    fun setLeadingIconImage(uri: Uri) = leadingIconImageView.setImageAndUpdateVisibility(uri)

    fun setLeadingText(@StringRes resId: Int) = leadingTextView.setTextAndUpdateVisibility(resId)
    fun setLeadingText(@StringRes resId: Int, vararg formatArgs: Any) =
        leadingTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    fun setLeadingView(@LayoutRes resId: Int) =
        leadingViewContainerFrameLayout.setChildAndUpdateVisibility(resId)

    fun setSupportingText(@StringRes resId: Int) =
        supportingTextView.setTextAndUpdateVisibility(resId)

    fun setSupportingText(@StringRes resId: Int, vararg formatArgs: Any) =
        supportingTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    fun setTrailingIconImage(bm: Bitmap) = trailingIconImageView.setImageAndUpdateVisibility(bm)
    fun setTrailingIconImage(icon: Icon) = trailingIconImageView.setImageAndUpdateVisibility(icon)
    fun setTrailingIconImage(@DrawableRes resId: Int) =
        trailingIconImageView.setImageAndUpdateVisibility(resId)

    fun setTrailingIconImage(uri: Uri) = trailingIconImageView.setImageAndUpdateVisibility(uri)

    fun setTrailingSupportingText(@StringRes resId: Int) =
        trailingSupportingTextView.setTextAndUpdateVisibility(resId)

    fun setTrailingSupportingText(@StringRes resId: Int, vararg formatArgs: Any) =
        trailingSupportingTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    fun setTrailingView(@LayoutRes resId: Int) =
        trailingViewContainerFrameLayout.setChildAndUpdateVisibility(resId)

    private inline fun <T> setViewsProperty(
        setter: View.(T) -> Unit,
        value: T,
    ) {
        headlineTextView.setter(value)
        leadingIconImageView.setter(value)
        leadingTextView.setter(value)
        leadingView?.setter(value)
        supportingTextView.setter(value)
        trailingIconImageView.setter(value)
        trailingSupportingTextView.setter(value)
        trailingView?.setter(value)
    }

    // FrameLayout utils

    private fun FrameLayout.updateVisibility(isVisible: Boolean) {
        this.isVisible = isVisible && isNotEmpty()
    }

    private fun FrameLayout.setChildAndUpdateVisibility(child: View?) {
        removeAllViews()
        child?.let {
            addView(it)
            // Sync up states
            it.isEnabled = isEnabled
            it.isSelected = isSelected
        }
        updateVisibility(true)
    }

    private fun FrameLayout.setChildAndUpdateVisibility(@LayoutRes resId: Int) {
        setChildAndUpdateVisibility(LayoutInflater.from(context).inflate(resId, this, false))
    }

    // ImageView utils

    private fun ImageView.setImageAndUpdateVisibility(bm: Bitmap) {
        setImageBitmap(bm)
        isVisible = true
    }

    private fun ImageView.setImageAndUpdateVisibility(drawable: Drawable?) {
        setImageDrawable(drawable)
        isVisible = drawable != null
    }

    private fun ImageView.setImageAndUpdateVisibility(icon: Icon) {
        setImageIcon(icon)
        isVisible = true
    }

    private fun ImageView.setImageAndUpdateVisibility(@DrawableRes resId: Int) {
        setImageResource(resId)
        isVisible = true
    }

    private fun ImageView.setImageAndUpdateVisibility(uri: Uri) {
        setImageURI(uri)
        isVisible = true
    }

    // TextView utils

    private fun TextView.setTextAndUpdateVisibility(text: CharSequence?) {
        this.text = text.also {
            isVisible = it != null
        }
    }

    private fun TextView.setTextAndUpdateVisibility(@StringRes resId: Int) =
        setTextAndUpdateVisibility(resources.getText(resId))

    private fun TextView.setTextAndUpdateVisibility(@StringRes resId: Int, vararg formatArgs: Any) =
        setTextAndUpdateVisibility(resources.getString(resId, *formatArgs))
}
