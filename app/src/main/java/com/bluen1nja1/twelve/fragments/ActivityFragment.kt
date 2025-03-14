/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.bluen1nja1.twelve.R
import com.bluen1nja1.twelve.ext.getViewProperty
import com.bluen1nja1.twelve.ext.navigateSafe
import com.bluen1nja1.twelve.ext.setProgressCompat
import com.bluen1nja1.twelve.models.ActivityTab
import com.bluen1nja1.twelve.models.Album
import com.bluen1nja1.twelve.models.Artist
import com.bluen1nja1.twelve.models.Audio
import com.bluen1nja1.twelve.models.FlowResult
import com.bluen1nja1.twelve.models.Genre
import com.bluen1nja1.twelve.models.Playlist
import com.bluen1nja1.twelve.ui.recyclerview.SimpleListAdapter
import com.bluen1nja1.twelve.ui.recyclerview.UniqueItemDiffCallback
import com.bluen1nja1.twelve.ui.views.ActivityTabView
import com.bluen1nja1.twelve.utils.PermissionsChecker
import com.bluen1nja1.twelve.utils.PermissionsUtils
import com.bluen1nja1.twelve.viewmodels.ActivityViewModel

/**
 * User activity, notifications and recommendations.
 */
class ActivityFragment : Fragment(R.layout.fragment_activity) {
    // View models
    private val viewModel by viewModels<ActivityViewModel>()

    // Views
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)

    // RecyclerView
    private val adapter by lazy {
        object : SimpleListAdapter<ActivityTab, ActivityTabView>(
            UniqueItemDiffCallback(),
            ::ActivityTabView,
        ) {
            override fun ViewHolder.onBindView(item: ActivityTab) {
                view.setOnItemClickListener { mediaItem ->
                    when (mediaItem) {
                        is Album -> findNavController().navigateSafe(
                            R.id.action_mainFragment_to_fragment_album,
                            AlbumFragment.createBundle(mediaItem.uri)
                        )

                        is Artist -> findNavController().navigateSafe(
                            R.id.action_mainFragment_to_fragment_artist,
                            ArtistFragment.createBundle(mediaItem.uri)
                        )

                        is Audio -> viewModel.playAudio(listOf(mediaItem), 0)

                        is Genre -> findNavController().navigateSafe(
                            R.id.action_mainFragment_to_fragment_genre,
                            GenreFragment.createBundle(mediaItem.uri)
                        )

                        is Playlist -> findNavController().navigateSafe(
                            R.id.action_mainFragment_to_fragment_playlist,
                            PlaylistFragment.createBundle(mediaItem.uri)
                        )
                    }
                }
                view.setOnItemLongClickListener { mediaItem ->
                    findNavController().navigateSafe(
                        R.id.action_mainFragment_to_fragment_media_item_bottom_sheet_dialog,
                        MediaItemBottomSheetDialogFragment.createBundle(mediaItem.uri)
                    )
                    true
                }

                view.setActivityTab(item)
            }
        }
    }

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    private suspend fun loadData() {
        viewModel.activity.collectLatest {
            linearProgressIndicator.setProgressCompat(it)

            when (it) {
                is FlowResult.Loading -> {
                    // Do nothing
                }

                is FlowResult.Success -> {
                    val data = it.data

                    adapter.submitList(data)

                    val isEmpty = it.data.isEmpty()
                    recyclerView.isVisible = !isEmpty
                    noElementsLinearLayout.isVisible = isEmpty
                }

                is FlowResult.Error -> {
                    Log.e(LOG_TAG, "Failed to load activity, error: ${it.error}", it.throwable)

                    recyclerView.isVisible = false
                    noElementsLinearLayout.isVisible = true
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ActivityFragment::class.simpleName!!
    }
}
