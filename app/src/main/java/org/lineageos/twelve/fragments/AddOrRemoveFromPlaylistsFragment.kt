/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.setProgressCompat
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.dialogs.EditTextMaterialAlertDialogBuilder
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.AddOrRemoveFromPlaylistsViewModel

/**
 * Fragment from which you can add or remove a specific audio from a list of playlists.
 */
class AddOrRemoveFromPlaylistsFragment : Fragment(R.layout.fragment_add_or_remove_from_playlists) {
    // View models
    private val viewModel by viewModels<AddOrRemoveFromPlaylistsViewModel>()

    // Views
    private val createNewPlaylistButton by getViewProperty<Button>(R.id.createNewPlaylistButton)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // Recyclerview
    private val addNewPlaylistItem = Pair(Playlist(Uri.EMPTY, ""), false)
    private val adapter by lazy {
        object : SimpleListAdapter<Pair<Playlist, Boolean>, ListItem>(
            diffCallback,
            ::ListItem,
        ) {
            override fun ViewHolder.onPrepareView() {
                view.setOnClickListener {
                    item?.let {
                        when (it === addNewPlaylistItem) {
                            true -> openCreateNewPlaylistDialog()
                            false -> viewLifecycleOwner.lifecycleScope.launch {
                                fullscreenLoadingProgressBar.withProgress {
                                    when (it.second) {
                                        true -> viewModel.removeFromPlaylist(it.first.uri)
                                        false -> viewModel.addToPlaylist(it.first.uri)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun ViewHolder.onBindView(item: Pair<Playlist, Boolean>) {
                when (item === addNewPlaylistItem) {
                    true -> {
                        view.setLeadingIconImage(R.drawable.ic_playlist_add)
                        view.setHeadlineText(R.string.create_playlist)
                        view.trailingIconImage = null
                    }

                    false -> {
                        view.setLeadingIconImage(R.drawable.ic_playlist_play)
                        view.headlineText = item.first.name
                        view.setTrailingIconImage(
                            when (item.second) {
                                true -> R.drawable.ic_check_circle
                                false -> R.drawable.ic_circle
                            }
                        )
                    }
                }

            }
        }
    }

    // Arguments
    private val audioUri: Uri
        get() = requireArguments().getParcelable(ARG_AUDIO_URI, Uri::class)!!

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setupWithNavController(findNavController())

        recyclerView.adapter = adapter

        createNewPlaylistButton.setOnClickListener {
            openCreateNewPlaylistDialog()
        }

        viewModel.loadAudio(audioUri)

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
        viewModel.playlistToHasAudio.collect {
            linearProgressIndicator.setProgressCompat(it, true)

            when (it) {
                is RequestStatus.Loading -> {
                    // Do nothing
                }

                is RequestStatus.Success -> {
                    val isEmpty = it.data.isEmpty()

                    adapter.submitList(
                        when (isEmpty) {
                            true -> emptyList()
                            false -> listOf(
                                addNewPlaylistItem,
                                *it.data.toTypedArray(),
                            )
                        }
                    )

                    recyclerView.isVisible = !isEmpty
                    noElementsLinearLayout.isVisible = isEmpty
                }

                is RequestStatus.Error -> {
                    Log.e(LOG_TAG, "Failed to load data, error: ${it.error}", it.throwable)

                    adapter.submitList(emptyList())

                    recyclerView.isVisible = false
                    noElementsLinearLayout.isVisible = true
                }
            }
        }
    }

    private fun openCreateNewPlaylistDialog() {
        EditTextMaterialAlertDialogBuilder(requireContext())
            .setPositiveButton(R.string.create_playlist_confirm) { text ->
                viewLifecycleOwner.lifecycleScope.launch {
                    fullscreenLoadingProgressBar.withProgress {
                        viewModel.createPlaylist(text)
                    }
                }
            }
            .setTitle(R.string.create_playlist)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private val LOG_TAG = AddOrRemoveFromPlaylistsFragment::class.simpleName!!

        private const val ARG_AUDIO_URI = "audio_uri"

        private val diffCallback = object : DiffUtil.ItemCallback<Pair<Playlist, Boolean>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Playlist, Boolean>,
                newItem: Pair<Playlist, Boolean>
            ) = oldItem.first.areItemsTheSame(newItem.first)

            override fun areContentsTheSame(
                oldItem: Pair<Playlist, Boolean>,
                newItem: Pair<Playlist, Boolean>
            ) = oldItem.first.areContentsTheSame(newItem.first) && oldItem.second == newItem.second
        }

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param audioUri The URI of the audio to manage
         */
        fun createBundle(
            audioUri: Uri,
        ) = bundleOf(
            ARG_AUDIO_URI to audioUri,
        )
    }
}
