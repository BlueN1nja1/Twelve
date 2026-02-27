/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.Px
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import org.lineageos.twelve.ext.setOffset

abstract class CollapsingToolbarLayoutFragment : Fragment {
    // Views
    protected abstract val appBarLayout: AppBarLayout
    protected abstract val coordinatorLayout: CoordinatorLayout

    @Px
    private var appBarOffset = -1

    private val offsetChangedListener = AppBarLayout.OnOffsetChangedListener { _, i ->
        appBarOffset = -i
    }

    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (appBarOffset != -1) {
            appBarLayout.setOffset(appBarOffset, coordinatorLayout)
        } else {
            appBarLayout.setExpanded(true, false)
        }

        appBarLayout.addOnOffsetChangedListener(offsetChangedListener)
    }

    @CallSuper
    override fun onDestroyView() {
        appBarLayout.removeOnOffsetChangedListener(offsetChangedListener)

        super.onDestroyView()
    }
}
