/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.datasources

import android.net.Uri
import kotlinx.coroutines.flow.flowOf
import com.bluen1nja1.twelve.models.ActivityTab
import com.bluen1nja1.twelve.models.Album
import com.bluen1nja1.twelve.models.Artist
import com.bluen1nja1.twelve.models.ArtistWorks
import com.bluen1nja1.twelve.models.Audio
import com.bluen1nja1.twelve.models.DataSourceInformation
import com.bluen1nja1.twelve.models.Error
import com.bluen1nja1.twelve.models.Genre
import com.bluen1nja1.twelve.models.GenreContent
import com.bluen1nja1.twelve.models.Lyrics
import com.bluen1nja1.twelve.models.MediaItem
import com.bluen1nja1.twelve.models.Playlist
import com.bluen1nja1.twelve.models.Result
import com.bluen1nja1.twelve.models.SortingRule

/**
 * A dummy data source that returns either empty lists, [Error.NOT_FOUND] responses for lookup
 * methods or [Error.NOT_IMPLEMENTED] for write methods.
 * No [Uri] is compatible as well.
 */
object DummyDataSource : MediaDataSource {
    override fun status() = flowOf(
        Result.Success<_, Error>(listOf<DataSourceInformation>())
    )

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = null

    override fun activity() = flowOf(
        Result.Success<_, Error>(listOf<ActivityTab>())
    )

    override fun albums(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Album>())
    )

    override fun artists(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Artist>())
    )

    override fun genres(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Genre>())
    )

    override fun playlists(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Playlist>())
    )

    override fun search(query: String) = flowOf(
        Result.Success<_, Error>(listOf<MediaItem<*>>())
    )

    override fun audio(audioUri: Uri) = flowOf(
        Result.Error<Audio, _>(Error.NOT_FOUND)
    )

    override fun album(albumUri: Uri) = flowOf(
        Result.Error<Pair<Album, List<Audio>>, _>(Error.NOT_FOUND)
    )

    override fun artist(artistUri: Uri) = flowOf(
        Result.Error<Pair<Artist, ArtistWorks>, _>(Error.NOT_FOUND)
    )

    override fun genre(genreUri: Uri) = flowOf(
        Result.Error<Pair<Genre, GenreContent>, _>(Error.NOT_FOUND)
    )

    override fun playlist(playlistUri: Uri) = flowOf(
        Result.Error<Pair<Playlist, List<Audio>>, _>(Error.NOT_FOUND)
    )

    override fun audioPlaylistsStatus(audioUri: Uri) = flowOf(
        Result.Error<List<Pair<Playlist, Boolean>>, _>(Error.NOT_FOUND)
    )

    override fun lyrics(audioUri: Uri) = flowOf(
        Result.Error<Lyrics, _>(Error.NOT_FOUND)
    )

    override suspend fun createPlaylist(name: String) =
        Result.Error<Uri, _>(Error.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun deletePlaylist(playlistUri: Uri) =
        Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun onAudioPlayed(audioUri: Uri) = Result.Success<_, Error>(Unit)

    override suspend fun setFavorite(
        audioUri: Uri,
        isFavorite: Boolean
    ) = Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)
}
