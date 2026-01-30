/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import org.lineageos.twelve.R
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import java.util.SortedMap

class SortingChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipStyle
) : Chip(context, attrs, defStyleAttr) {
    private var sortingStrategies: SortedMap<SortingStrategy, Int> = sortedMapOf()
    private var sortingRule: SortingRule? = null

    private var onSortingRuleSelected: ((SortingRule) -> Unit)? = null

    private val popupMenu = PopupMenu(context, this).apply {
        setForceShowIcon(true)

        setOnMenuItemClickListener {
            val strategy = SortingStrategy.entries[it.itemId]
            val reverse = sortingRule?.takeIf { sortingRule ->
                sortingRule.strategy == strategy
            }?.reverse?.not() ?: sortingRule?.reverse ?: false

            onSortingRuleSelected?.invoke(SortingRule(strategy, reverse))

            true
        }
    }

    init {
        chipBackgroundColor = MaterialColors.getColorStateListOrNull(
            context,
            com.google.android.material.R.attr.colorPrimaryContainer
        )
        val chipContentColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnPrimaryContainer
        )
        chipStrokeWidth = 0f

        setText(R.string.sort_by_unknown)
        setTextColor(chipContentColor)

        setCloseIcon(R.drawable.ic_arrow_drop_down)

        setOnClickListener {
            openPopupMenu()
        }
    }

    fun setSortingStrategies(sortingStrategies: SortedMap<SortingStrategy, Int>) {
        this.sortingStrategies = sortingStrategies

        popupMenu.menu.clear()
        sortingStrategies.forEach { (sortingStrategy, stringResId) ->
            popupMenu.menu.add(
                0,
                sortingStrategy.ordinal,
                0,
                stringResId
            )
            popupMenu.menu.findItem(sortingStrategy.ordinal).let {
                it.iconTintList = MaterialColors.getColorStateListOrNull(
                    context,
                    com.google.android.material.R.attr.colorOnSurface
                )
            }
        }
    }

    fun setOnSortingRuleSelectedListener(listener: (SortingRule) -> Unit) {
        onSortingRuleSelected = listener
    }

    fun setSortingRule(sortingRule: SortingRule) {
        this.sortingRule = sortingRule

        setText(
            sortingStrategies[sortingRule.strategy] ?: R.string.sort_by_unknown
        )

        setChipIcon(
            when (sortingRule.reverse) {
                true -> R.drawable.ic_sort_alphabetical_descending
                false -> R.drawable.ic_sort_alphabetical_ascending
            }
        )

        popupMenu.menu.forEach {
            val isCurrentStrategy = it.itemId == sortingRule.strategy.ordinal

            if (isCurrentStrategy) {
                it.setIcon(
                    when (sortingRule.reverse) {
                        true -> R.drawable.ic_sort_alphabetical_descending
                        false -> R.drawable.ic_sort_alphabetical_ascending
                    }
                )
            } else {
                it.icon = null
            }
        }
    }

    private fun setChipIcon(@DrawableRes icon: Int) {
        setChipIconResource(icon)
        isChipIconVisible = true
        chipIconTint = MaterialColors.getColorStateListOrNull(
            context,
            com.google.android.material.R.attr.colorOnPrimaryContainer
        )
    }

    private fun setCloseIcon(@DrawableRes icon: Int) {
        setCloseIconResource(icon)
        isCloseIconVisible = true
        closeIconTint = MaterialColors.getColorStateListOrNull(
            context,
            com.google.android.material.R.attr.colorOnPrimaryContainer
        )
    }

    private fun openPopupMenu() {
        when (sortingStrategies.size) {
            0 -> return

            1 -> {
                val strategy = sortingStrategies.keys.first()
                val reverse = sortingRule?.takeIf { sortingRule ->
                    sortingRule.strategy == strategy
                }?.reverse?.not() ?: sortingRule?.reverse ?: false

                onSortingRuleSelected?.invoke(SortingRule(strategy, reverse))
            }

            else -> popupMenu.show()
        }
    }
}
