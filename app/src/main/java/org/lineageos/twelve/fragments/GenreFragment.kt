/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.loadThumbnail
import org.lineageos.twelve.ext.navigateSafe
import org.lineageos.twelve.ext.setProgressCompat
import org.lineageos.twelve.ext.updatePadding
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.FlowResult
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.HorizontalMediaItemView
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.GenreViewModel

/**
 * Single genre viewer.
 */
class GenreFragment : Fragment(R.layout.fragment_genre) {
    // View models
    private val viewModel by viewModels<GenreViewModel>()

    // Views
    private val appearsInAlbumsLinearLayout by getViewProperty<LinearLayout>(R.id.appearsInAlbumsLinearLayout)
    private val appearsInAlbumsRecyclerView by getViewProperty<RecyclerView>(R.id.appearsInAlbumsRecyclerView)
    private val appearsInPlaylistsLinearLayout by getViewProperty<LinearLayout>(R.id.appearsInPlaylistsLinearLayout)
    private val appearsInPlaylistsRecyclerView by getViewProperty<RecyclerView>(R.id.appearsInPlaylistsRecyclerView)
    private val audiosLinearLayout by getViewProperty<LinearLayout>(R.id.audiosLinearLayout)
    private val audiosRecyclerView by getViewProperty<RecyclerView>(R.id.audiosRecyclerView)
    private val genreNameTextView by getViewProperty<TextView>(R.id.genreNameTextView)
    private val infoNestedScrollView by getViewProperty<NestedScrollView?>(R.id.infoNestedScrollView)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val nestedScrollView by getViewProperty<NestedScrollView>(R.id.nestedScrollView)
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val thumbnailImageView by getViewProperty<ImageView>(R.id.thumbnailImageView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // RecyclerView
    private val appearsInAlbumsAdapter by lazy {
        object : SimpleListAdapter<Album, HorizontalMediaItemView>(
            UniqueItemDiffCallback(),
            ::HorizontalMediaItemView,
        ) {
            override fun ViewHolder.onBindView(item: Album) {
                view.setOnClickListener {
                    findNavController().navigateSafe(
                        R.id.action_genreFragment_to_fragment_album,
                        AlbumFragment.createBundle(item.uri)
                    )
                }
                view.setOnLongClickListener {
                    findNavController().navigateSafe(
                        R.id.action_genreFragment_to_fragment_media_item_bottom_sheet_dialog,
                        MediaItemBottomSheetDialogFragment.createBundle(item.uri)
                    )
                    true
                }

                view.setItem(item)
            }
        }
    }
    private val appearsInPlaylistsAdapter by lazy {
        object : SimpleListAdapter<Playlist, HorizontalMediaItemView>(
            UniqueItemDiffCallback(),
            ::HorizontalMediaItemView,
        ) {
            override fun ViewHolder.onBindView(item: Playlist) {
                view.setOnClickListener {
                    findNavController().navigateSafe(
                        R.id.action_genreFragment_to_fragment_playlist,
                        PlaylistFragment.createBundle(item.uri)
                    )
                }
                view.setOnLongClickListener {
                    findNavController().navigateSafe(
                        R.id.action_genreFragment_to_fragment_media_item_bottom_sheet_dialog,
                        MediaItemBottomSheetDialogFragment.createBundle(item.uri)
                    )
                    true
                }

                view.setItem(item)
            }
        }
    }
    private val audiosAdapter by lazy {
        object : SimpleListAdapter<Audio, HorizontalMediaItemView>(
            UniqueItemDiffCallback(),
            ::HorizontalMediaItemView,
        ) {
            override fun ViewHolder.onBindView(item: Audio) {
                view.setOnClickListener {
                    viewModel.playAudio(currentList, bindingAdapterPosition)
                    findNavController().navigateSafe(
                        R.id.action_genreFragment_to_fragment_now_playing
                    )
                }
                view.setOnLongClickListener {
                    findNavController().navigateSafe(
                        R.id.action_genreFragment_to_fragment_media_item_bottom_sheet_dialog,
                        MediaItemBottomSheetDialogFragment.createBundle(
                            item.uri,
                            fromGenre = true,
                        )
                    )
                    true
                }

                view.setItem(item)
            }
        }
    }

    // Arguments
    private val genreUri: Uri
        get() = requireArguments().getParcelable(ARG_GENRE_URI, Uri::class)!!

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

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

        infoNestedScrollView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                v.updatePadding(
                    insets,
                    bottom = true,
                )

                windowInsets
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(nestedScrollView) { v, windowInsets ->
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

        appearsInAlbumsRecyclerView.adapter = appearsInAlbumsAdapter
        appearsInPlaylistsRecyclerView.adapter = appearsInPlaylistsAdapter
        audiosRecyclerView.adapter = audiosAdapter

        viewModel.loadGenre(genreUri)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    override fun onDestroyView() {
        appearsInAlbumsRecyclerView.adapter = null
        appearsInPlaylistsRecyclerView.adapter = null
        audiosRecyclerView.adapter = null

        super.onDestroyView()
    }

    private suspend fun loadData() {
        viewModel.genre.collectLatest {
            linearProgressIndicator.setProgressCompat(it)

            when (it) {
                is FlowResult.Loading -> {
                    // Do nothing
                }

                is FlowResult.Success -> {
                    val (genre, genreContent) = it.data

                    genre.name?.also { genreName ->
                        toolbar.title = genreName
                        genreNameTextView.text = genreName
                    } ?: run {
                        toolbar.setTitle(R.string.genre_unknown)
                        genreNameTextView.setText(R.string.genre_unknown)
                    }

                    thumbnailImageView.loadThumbnail(
                        genre.thumbnail,
                        placeholder = R.drawable.ic_genres
                    )

                    appearsInAlbumsAdapter.submitList(genreContent.appearsInAlbums)
                    appearsInPlaylistsAdapter.submitList(genreContent.appearsInPlaylists)
                    audiosAdapter.submitList(genreContent.audios)

                    val isAppearsInAlbumsEmpty = genreContent.appearsInAlbums.isEmpty()
                    appearsInAlbumsLinearLayout.isVisible = !isAppearsInAlbumsEmpty

                    val isAppearsInPlaylistsEmpty = genreContent.appearsInPlaylists.isEmpty()
                    appearsInPlaylistsLinearLayout.isVisible = !isAppearsInPlaylistsEmpty

                    val isAudiosEmpty = genreContent.audios.isEmpty()
                    audiosLinearLayout.isVisible = !isAudiosEmpty

                    val isEmpty = listOf(
                        isAppearsInAlbumsEmpty,
                        isAppearsInPlaylistsEmpty,
                        isAudiosEmpty,
                    ).all { isEmpty -> isEmpty }
                    nestedScrollView.isVisible = !isEmpty
                    noElementsNestedScrollView.isVisible = isEmpty
                }

                is FlowResult.Error -> {
                    Log.e(
                        LOG_TAG,
                        "Error loading genre, error: ${it.error}",
                        it.throwable
                    )

                    toolbar.title = ""
                    genreNameTextView.text = ""

                    appearsInAlbumsAdapter.submitList(listOf())
                    appearsInPlaylistsAdapter.submitList(listOf())
                    audiosAdapter.submitList(listOf())

                    nestedScrollView.isVisible = false
                    noElementsNestedScrollView.isVisible = true

                    if (it.error == Error.NOT_FOUND) {
                        // Get out of here
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = GenreFragment::class.simpleName!!

        private const val ARG_GENRE_URI = "genre_uri"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param genreUri The URI of the genre to display
         */
        fun createBundle(
            genreUri: Uri,
        ) = bundleOf(
            ARG_GENRE_URI to genreUri,
        )
    }
}
