/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getSerializable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.navigateSafe
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.MediaItemViewModel

/**
 * Audio information.
 */
class MediaItemBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_media_item_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<MediaItemViewModel>()

    // Views
    private val addOrRemoveFromPlaylistsListItem by getViewProperty<ListItem>(R.id.addOrRemoveFromPlaylistsListItem)
    private val addToQueueListItem by getViewProperty<ListItem>(R.id.addToQueueListItem)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val albumTitleTextView by getViewProperty<TextView>(R.id.albumTitleTextView)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val openAlbumListItem by getViewProperty<ListItem>(R.id.openAlbumListItem)
    private val openArtistListItem by getViewProperty<ListItem>(R.id.openArtistListItem)
    private val openGenreListItem by getViewProperty<ListItem>(R.id.openGenreListItem)
    private val placeholderImageView by getViewProperty<ImageView>(R.id.placeholderImageView)
    private val playNowListItem by getViewProperty<ListItem>(R.id.playNowListItem)
    private val playNextListItem by getViewProperty<ListItem>(R.id.playNextListItem)
    private val removeFromPlaylistListItem by getViewProperty<ListItem>(R.id.removeFromPlaylistListItem)
    private val thumbnailImageView by getViewProperty<ImageView>(R.id.thumbnailImageView)
    private val titleTextView by getViewProperty<TextView>(R.id.titleTextView)

    // Arguments
    private val uri: Uri
        get() = requireArguments().getParcelable(ARG_URI, Uri::class)!!
    private val mediaType: MediaType
        get() = requireArguments().getSerializable(ARG_MEDIA_TYPE, MediaType::class)!!
    private val fromAlbum: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_ALBUM)
    private val fromArtist: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_ARTIST)
    private val fromGenre: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_GENRE)
    private val fromNowPlaying: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_NOW_PLAYING)
    private val playlistUri: Uri?
        get() = requireArguments().getParcelable(ARG_PLAYLIST_URI, Uri::class)

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playNowListItem.setOnClickListener {
            viewModel.tracks.value.takeIf { it.isNotEmpty() }?.let { tracks ->
                viewModel.playAudio(tracks.toList(), 0)
            }

            findNavController().navigateUp()
        }

        addToQueueListItem.setOnClickListener {
            viewModel.tracks.value.takeIf { it.isNotEmpty() }?.let { tracks ->
                viewModel.addToQueue(*tracks.toTypedArray())

                findNavController().navigateUp()
            }
        }

        playNextListItem.setOnClickListener {
            viewModel.tracks.value.takeIf { it.isNotEmpty() }?.let { tracks ->
                viewModel.playNext(*tracks.toTypedArray())

                findNavController().navigateUp()
            }
        }

        removeFromPlaylistListItem.isVisible = mediaType == MediaType.AUDIO && playlistUri != null
        removeFromPlaylistListItem.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                fullscreenLoadingProgressBar.withProgress {
                    playlistUri?.let {
                        viewModel.removeAudioFromPlaylist(it)

                        findNavController().navigateUp()
                    }
                }
            }
        }

        addOrRemoveFromPlaylistsListItem.isVisible = mediaType == MediaType.AUDIO
        addOrRemoveFromPlaylistsListItem.setOnClickListener {
            findNavController().navigateSafe(
                R.id.action_mediaItemBottomSheetDialogFragment_to_fragment_add_or_remove_from_playlists,
                AddOrRemoveFromPlaylistsFragment.createBundle(uri)
            )
        }

        openAlbumListItem.setOnClickListener {
            viewModel.albumUri.value?.let { albumUri ->
                findNavController().navigateSafe(
                    R.id.action_mediaItemBottomSheetDialogFragment_to_fragment_album,
                    AlbumFragment.createBundle(albumUri)
                )
            }
        }

        openArtistListItem.setOnClickListener {
            viewModel.artistUri.value?.let { artistUri ->
                findNavController().navigateSafe(
                    R.id.action_mediaItemBottomSheetDialogFragment_to_fragment_artist,
                    ArtistFragment.createBundle(artistUri)
                )
            }
        }

        openGenreListItem.setOnClickListener {
            viewModel.genreUri.value?.let { genreUri ->
                findNavController().navigateSafe(
                    R.id.action_mediaItemBottomSheetDialogFragment_to_fragment_genre,
                    GenreFragment.createBundle(genreUri)
                )
            }
        }

        viewModel.loadMediaItem(uri, mediaType)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.mediaItem.collectLatest {
                when (it) {
                    is RequestStatus.Loading -> {
                        // Do nothing
                    }

                    is RequestStatus.Success -> {
                        val mediaItem = it.data

                        titleTextView.text = when (mediaItem) {
                            is Album -> mediaItem.title ?: getString(R.string.album_unknown)
                            is Artist -> mediaItem.name ?: getString(R.string.artist_unknown)
                            is Audio -> mediaItem.title
                            is Genre -> mediaItem.name ?: getString(R.string.genre_unknown)
                            is Playlist -> mediaItem.name
                        }
                        artistNameTextView.text = when (mediaItem) {
                            is Album -> mediaItem.artistName
                            is Audio -> mediaItem.artistName
                            else -> null
                        }.also { artistName ->
                            artistNameTextView.isVisible = artistName != null
                        }
                        albumTitleTextView.text = when (mediaItem) {
                            is Audio -> mediaItem.albumTitle
                            else -> null
                        }.also { albumTitle ->
                            albumTitleTextView.isVisible = albumTitle != null
                        }

                        placeholderImageView.setImageResource(
                            when (mediaItem) {
                                is Album -> R.drawable.ic_album
                                is Artist -> R.drawable.ic_person
                                is Audio -> R.drawable.ic_music_note
                                is Genre -> R.drawable.ic_genres
                                is Playlist -> R.drawable.ic_playlist_play
                            }
                        )

                        when (mediaItem) {
                            is Album -> mediaItem.thumbnail
                            is Artist -> mediaItem.thumbnail
                            is Audio -> null
                            is Genre -> mediaItem.thumbnail
                            is Playlist -> mediaItem.thumbnail
                        }.let { thumbnail ->
                            thumbnail?.also {
                                thumbnailImageView.load(thumbnail) {
                                    listener(
                                        onCancel = {
                                            placeholderImageView.isVisible = true
                                            thumbnailImageView.isVisible = false
                                        },
                                        onSuccess = { _, _ ->
                                            placeholderImageView.isVisible = false
                                            thumbnailImageView.isVisible = true
                                        },
                                        onError = { _, _ ->
                                            placeholderImageView.isVisible = true
                                            thumbnailImageView.isVisible = false
                                        }
                                    )
                                }
                            } ?: run {
                                placeholderImageView.isVisible = true
                                thumbnailImageView.isVisible = false
                            }
                        }
                    }

                    is RequestStatus.Error -> {
                        Log.e(
                            LOG_TAG,
                            "Failed to load media item, error: ${it.error}",
                            it.throwable
                        )

                        if (it.error == MediaError.NOT_FOUND) {
                            // Get out of here
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }

        launch {
            viewModel.tracks.collectLatest { tracks ->
                val isNotEmpty = tracks.isNotEmpty()

                playNowListItem.isVisible = isNotEmpty && !fromNowPlaying
                addToQueueListItem.isVisible = isNotEmpty && !fromNowPlaying
                playNextListItem.isVisible = isNotEmpty && !fromNowPlaying
            }
        }

        launch {
            viewModel.albumUri.collectLatest { albumUri ->
                openAlbumListItem.isVisible = !fromAlbum && albumUri != null
            }
        }

        launch {
            viewModel.artistUri.collectLatest { artistUri ->
                openArtistListItem.isVisible = !fromArtist && artistUri != null
            }
        }

        launch {
            viewModel.genreUri.collectLatest { genreUri ->
                openGenreListItem.isVisible = !fromGenre && genreUri != null
            }
        }
    }

    companion object {
        private val LOG_TAG = MediaItemBottomSheetDialogFragment::class.simpleName!!

        private const val ARG_URI = "uri"
        private const val ARG_MEDIA_TYPE = "media_type"
        private const val ARG_FROM_ALBUM = "from_album"
        private const val ARG_FROM_ARTIST = "from_artist"
        private const val ARG_FROM_GENRE = "from_genre"
        private const val ARG_FROM_NOW_PLAYING = "from_now_playing"
        private const val ARG_PLAYLIST_URI = "playlist_uri"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param uri The URI of the media item to display
         * @param mediaType The [MediaType] of the media item to display
         * @param fromAlbum Whether this fragment was opened from an album
         * @param fromArtist Whether this fragment was opened from an artist
         * @param fromGenre Whether this fragment was opened from a genre
         * @param fromNowPlaying Whether this fragment was opened from the now playing fragment
         * @param playlistUri If the audio has been opened from a playlist, the URI of the playlist
         */
        fun createBundle(
            uri: Uri,
            mediaType: MediaType,
            fromAlbum: Boolean = false,
            fromArtist: Boolean = false,
            fromGenre: Boolean = false,
            fromNowPlaying: Boolean = false,
            playlistUri: Uri? = null,
        ) = bundleOf(
            ARG_URI to uri,
            ARG_MEDIA_TYPE to mediaType,
            ARG_FROM_ALBUM to fromAlbum,
            ARG_FROM_ARTIST to fromArtist,
            ARG_FROM_GENRE to fromGenre,
            ARG_FROM_NOW_PLAYING to fromNowPlaying,
            ARG_PLAYLIST_URI to playlistUri,
        )
    }
}
