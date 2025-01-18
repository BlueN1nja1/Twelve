/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import kotlinx.coroutines.flow.flowOf
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule

/**
 * A dummy data source that returns either empty lists, [MediaError.NOT_FOUND] responses for lookup
 * methods or [MediaError.NOT_IMPLEMENTED] for write methods.
 * No [Uri] is compatible as well.
 */
object DummyDataSource : MediaDataSource {
    override fun status() = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<DataSourceInformation>())
    )

    override fun isMediaItemCompatible(mediaItemUri: Uri) = false

    override suspend fun mediaTypeOf(mediaItemUri: Uri) =
        RequestStatus.Error<MediaType, _>(MediaError.NOT_FOUND)

    override fun activity() = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<ActivityTab>())
    )

    override fun albums(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Album>())
    )

    override fun artists(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Artist>())
    )

    override fun genres(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Genre>())
    )

    override fun playlists(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Playlist>())
    )

    override fun search(query: String) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<MediaItem<*>>())
    )

    override fun audio(audioUri: Uri) = flowOf(
        RequestStatus.Error<Audio, _>(MediaError.NOT_FOUND)
    )

    override fun album(albumUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Album, List<Audio>>, _>(MediaError.NOT_FOUND)
    )

    override fun artist(artistUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Artist, ArtistWorks>, _>(MediaError.NOT_FOUND)
    )

    override fun genre(genreUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Genre, GenreContent>, _>(MediaError.NOT_FOUND)
    )

    override fun playlist(playlistUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Playlist, List<Audio>>, _>(MediaError.NOT_FOUND)
    )

    override fun audioPlaylistsStatus(audioUri: Uri) = flowOf(
        RequestStatus.Error<List<Pair<Playlist, Boolean>>, _>(MediaError.NOT_FOUND)
    )

    override fun lastPlayedAudio() = flowOf(
        RequestStatus.Error<Audio, _>(MediaError.NOT_FOUND)
    )

    override suspend fun createPlaylist(name: String) =
        RequestStatus.Error<Uri, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun deletePlaylist(playlistUri: Uri) =
        RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun onAudioPlayed(audioUri: Uri) = RequestStatus.Success<_, MediaError>(Unit)
}
