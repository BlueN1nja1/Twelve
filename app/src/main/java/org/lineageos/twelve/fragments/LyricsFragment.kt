/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.updateMargin
import org.lineageos.twelve.ext.updatePadding
import org.lineageos.twelve.models.Lyrics
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.recyclerview.CenterSmoothScroller
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.viewmodels.LyricsViewModel
import org.lineageos.twelve.viewmodels.NowPlayingViewModel

typealias LineToState = Pair<Lyrics.Line, NowPlayingViewModel.LyricsLineState>

/**
 * Show lyrics of currently playing audio.
 */
class LyricsFragment : Fragment(R.layout.fragment_lyrics) {
    // View models
    private val viewModel by viewModels<LyricsViewModel>()

    // Views
    private val followCurrentLineExtendedFloatingActionButton by getViewProperty<ExtendedFloatingActionButton>(
        R.id.followCurrentLineExtendedFloatingActionButton
    )
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // RecyclerView
    private val adapter by lazy {
        object : SimpleListAdapter<LineToState, TextView>(
            diffCallback,
            { layoutInflater.inflate(R.layout.lyrics_line, null, false) as TextView }
        ) {
            override fun ViewHolder.onBindView(item: LineToState) {
                val (line, lineState) = item

                view.text = line.text
                view.isSelected = lineState == NowPlayingViewModel.LyricsLineState.ACTIVE
                view.isActivated = lineState != NowPlayingViewModel.LyricsLineState.PAST

                view.setOnClickListener {
                    viewModel.seekToLine(line)
                }
            }
        }
    }
    private val scrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val isScrolling = newState == RecyclerView.SCROLL_STATE_DRAGGING
                        || newState == RecyclerView.SCROLL_STATE_SETTLING

                if (isScrolling && recyclerView.layoutManager?.isSmoothScrolling == false) {
                    viewModel.setPositionSynced(false)
                }
            }
        }
    }

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
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            followCurrentLineExtendedFloatingActionButton
        ) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updateMargin(
                insets,
                bottom = true,
            )

            windowInsets
        }

        toolbar.setupWithNavController(findNavController())

        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(scrollListener)

        followCurrentLineExtendedFloatingActionButton.setOnClickListener {
            viewModel.setPositionSynced(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadData()
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.clearOnScrollListeners()
        recyclerView.adapter = null

        super.onDestroyView()
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.lyricsLines.collectLatest {
                when (it) {
                    is RequestStatus.Loading -> {
                        // Do nothing
                    }

                    is RequestStatus.Success -> {
                        val (lyricsWithState, currentIndex) = it.data

                        adapter.submitList(lyricsWithState)
                        currentIndex.takeIf { viewModel.positionSynced.value }?.let { index ->
                            CenterSmoothScroller(recyclerView.context).apply {
                                targetPosition = index
                            }.also { smoothScroller ->
                                recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
                            }
                        }

                        val isEmpty = lyricsWithState.isEmpty()

                        recyclerView.isVisible = !isEmpty
                        noElementsNestedScrollView.isVisible = isEmpty
                    }

                    is RequestStatus.Error -> {
                        Log.e(
                            LOG_TAG,
                            "Error while loading lyrics, error: ${it.error}",
                            it.throwable
                        )

                        adapter.submitList(null)

                        recyclerView.isVisible = false
                        noElementsNestedScrollView.isVisible = true
                    }
                }
            }
        }

        launch {
            viewModel.positionSynced.collectLatest {
                followCurrentLineExtendedFloatingActionButton.isVisible = !it
            }
        }
    }

    companion object {
        private val LOG_TAG = LyricsFragment::class.simpleName!!

        private val diffCallback = object : DiffUtil.ItemCallback<LineToState>() {
            override fun areItemsTheSame(
                oldItem: LineToState,
                newItem: LineToState
            ) = oldItem.first === newItem.first

            override fun areContentsTheSame(
                oldItem: LineToState,
                newItem: LineToState
            ) = oldItem.second == newItem.second
        }
    }
}
