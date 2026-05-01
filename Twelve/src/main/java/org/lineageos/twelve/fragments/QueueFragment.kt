/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.updatePadding
import org.lineageos.twelve.models.QueueItem
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.QueueViewModel
import java.util.Collections

/**
 * Playback service queue.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class QueueFragment : Fragment(R.layout.fragment_queue) {
    // View models
    private val viewModel by viewModels<QueueViewModel>()

    // Views
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // RecyclerView
    private val adapter by lazy {
        object : SimpleListAdapter<QueueItem, ListItem>(
            UniqueItemDiffCallback(),
            ::ListItem,
        ) {
            var currentQueue = listOf<QueueItem>()
            var scrolled = false

            override fun ViewHolder.onPrepareView() {
                view.setTrailingIconImage(R.drawable.ic_drag_handle)
            }

            override fun ViewHolder.onBindView(item: QueueItem) {
                val (mediaItem, isCurrent) = item

                view.setLeadingIconImage(
                    when (isCurrent) {
                        true -> R.drawable.ic_play_arrow
                        false -> R.drawable.ic_music_note
                    }
                )
                mediaItem.mediaMetadata.title?.also {
                    view.headlineText = it
                } ?: view.setHeadlineText(R.string.unknown)
                mediaItem.mediaMetadata.artist?.also {
                    view.supportingText = it
                } ?: view.setSupportingText(R.string.artist_unknown)
                view.trailingSupportingText = mediaItem.mediaMetadata.durationMs?.let {
                    TimestampFormatter.formatTimestampMillis(it)
                }

                view.setOnClickListener {
                    viewModel.playItem(bindingAdapterPosition)
                }
            }
        }
    }
    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.START or ItemTouchHelper.END,
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition

            // First update our adapter list
            Collections.swap(adapter.currentQueue, from, to)
            adapter.notifyItemMoved(from, to)

            // Then update the queue
            viewModel.moveItem(from, to)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.removeItem(viewHolder.bindingAdapterPosition)
        }
    }
    private val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            v.updatePadding(
                insets,
                start = true,
                end = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(noElementsNestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        toolbar.setupWithNavController(findNavController())

        recyclerView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.queue.collectLatest { queue ->
                    queue.toMutableList().let {
                        adapter.currentQueue = it
                        adapter.submitList(it) {
                            if (it.isNotEmpty() && !adapter.scrolled) {
                                recyclerView.scrollToPosition(
                                    queue.indexOfFirst { item -> item.isCurrent }
                                )
                                adapter.scrolled = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        itemTouchHelper.attachToRecyclerView(null)
        recyclerView.adapter = null

        super.onDestroyView()
    }
}
