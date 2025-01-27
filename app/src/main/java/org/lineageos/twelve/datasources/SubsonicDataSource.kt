/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.subsonic.SubsonicClient
import org.lineageos.twelve.datasources.subsonic.models.AlbumID3
import org.lineageos.twelve.datasources.subsonic.models.ArtistID3
import org.lineageos.twelve.datasources.subsonic.models.Child
import org.lineageos.twelve.datasources.subsonic.models.Error
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.LocalizedString
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.ProviderArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.RequestStatus.Companion.map
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import org.lineageos.twelve.models.Thumbnail
import org.lineageos.twelve.utils.toRequestStatus
import org.lineageos.twelve.utils.toResult

/**
 * Subsonic based data source.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubsonicDataSource(
    arguments: Bundle,
    private val lastPlayedGetter: (String) -> Flow<Uri?>,
    private val lastPlayedSetter: suspend (String, Uri) -> Long,
    cache: Cache? = null,
) : MediaDataSource {
    private val server = arguments.requireArgument(ARG_SERVER)
    private val username = arguments.requireArgument(ARG_USERNAME)
    private val password = arguments.requireArgument(ARG_PASSWORD)
    private val useLegacyAuthentication = arguments.requireArgument(ARG_USE_LEGACY_AUTHENTICATION)

    private val subsonicClient = SubsonicClient(
        server, username, password, "Twelve", useLegacyAuthentication, cache
    )

    private val dataSourceBaseUri = Uri.parse(server)

    private val albumsUri = dataSourceBaseUri.buildUpon()
        .appendPath(ALBUMS_PATH)
        .build()
    private val artistsUri = dataSourceBaseUri.buildUpon()
        .appendPath(ARTISTS_PATH)
        .build()
    private val audiosUri = dataSourceBaseUri.buildUpon()
        .appendPath(AUDIOS_PATH)
        .build()
    private val genresUri = dataSourceBaseUri.buildUpon()
        .appendPath(GENRES_PATH)
        .build()
    private val playlistsUri = dataSourceBaseUri.buildUpon()
        .appendPath(PLAYLISTS_PATH)
        .build()

    /**
     * This flow is used to signal a change in the playlists.
     */
    private val _playlistsChanged = MutableStateFlow(Any())

    override fun status() = suspend {
        val ping = subsonicClient.ping().toRequestStatus { this }
        val license = subsonicClient.getLicense().toResult { this }

        ping.map {
            listOfNotNull(
                DataSourceInformation(
                    "version",
                    LocalizedString.StringResIdLocalizedString(
                        R.string.subsonic_version,
                    ),
                    LocalizedString.StringResIdLocalizedString(
                        R.string.subsonic_version_format,
                        listOf(it.version.major, it.version.minor, it.version.revision)
                    )
                ),
                it.type?.let { type ->
                    DataSourceInformation(
                        "server_type",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.subsonic_server_type,
                        ),
                        LocalizedString.StringLocalizedString(type)
                    )
                },
                it.serverVersion?.let { serverVersion ->
                    DataSourceInformation(
                        "server_version",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.subsonic_server_version,
                        ),
                        LocalizedString.StringLocalizedString(serverVersion)
                    )
                },
                it.openSubsonic?.let { openSubsonic ->
                    DataSourceInformation(
                        "supports_opensubsonic",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.subsonic_supports_opensubsonic,
                        ),
                        LocalizedString.of(openSubsonic)
                    )
                },
                license?.let { lic ->
                    DataSourceInformation(
                        "license",
                        LocalizedString.StringResIdLocalizedString(R.string.subsonic_license),
                        LocalizedString.StringResIdLocalizedString(
                            when (lic.valid) {
                                true -> R.string.subsonic_license_valid
                                false -> R.string.subsonic_license_invalid
                            }
                        )
                    )
                },
            )
        }
    }.asFlow()

    override fun isMediaItemCompatible(mediaItemUri: Uri) = mediaItemUri.toString().startsWith(
        dataSourceBaseUri.toString()
    )

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = with(mediaItemUri.toString()) {
        when {
            startsWith(albumsUri.toString()) -> MediaType.ALBUM
            startsWith(artistsUri.toString()) -> MediaType.ARTIST
            startsWith(audiosUri.toString()) -> MediaType.AUDIO
            startsWith(genresUri.toString()) -> MediaType.GENRE
            startsWith(playlistsUri.toString()) -> MediaType.PLAYLIST
            else -> null
        }?.let {
            RequestStatus.Success<_, MediaError>(it)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun activity() = suspend {
        val mostPlayedAlbums = subsonicClient.getAlbumList2(
            "frequent",
            10
        ).toRequestStatus {
            ActivityTab(
                "most_played_albums",
                LocalizedString.StringResIdLocalizedString(
                    R.string.activity_most_played_albums,
                ),
                album.sortedByDescending { it.playCount }.map { it.toMediaItem() }
            )
        }

        val randomAlbums = subsonicClient.getAlbumList2(
            "random",
            10
        ).toRequestStatus {
            ActivityTab(
                "random_albums",
                LocalizedString.StringResIdLocalizedString(
                    R.string.activity_random_albums,
                ),
                album.map { it.toMediaItem() }
            )
        }

        val randomSongs = subsonicClient.getRandomSongs(20).toRequestStatus {
            ActivityTab(
                "random_songs",
                LocalizedString.StringResIdLocalizedString(
                    R.string.activity_random_songs,
                ),
                song.map { it.toMediaItem() }
            )
        }

        RequestStatus.Success<_, MediaError>(
            listOf(
                mostPlayedAlbums,
                randomAlbums,
                randomSongs,
            ).mapNotNull {
                (it as? RequestStatus.Success)?.data?.takeIf { activityTab ->
                    activityTab.items.isNotEmpty()
                }
            }
        )
    }.asFlow()

    override fun albums(sortingRule: SortingRule) = suspend {
        subsonicClient.getAlbumList2(
            "alphabeticalByName",
            500
        ).toRequestStatus {
            album.maybeSortedBy(
                sortingRule.reverse,
                when (sortingRule.strategy) {
                    SortingStrategy.ARTIST_NAME -> { album -> album.artist }
                    SortingStrategy.CREATION_DATE -> { album -> album.year }
                    SortingStrategy.NAME -> { album -> album.name }
                    SortingStrategy.PLAY_COUNT -> { album -> album.playCount }
                    else -> null
                }
            ).map { it.toMediaItem() }
        }
    }.asFlow()

    override fun artists(sortingRule: SortingRule) = suspend {
        subsonicClient.getArtists().toRequestStatus {
            index.flatMap { it.artist }.maybeSortedBy(
                sortingRule.reverse,
                when (sortingRule.strategy) {
                    SortingStrategy.NAME -> { artist -> artist.name }

                    else -> null
                }
            ).map { it.toMediaItem() }
        }
    }.asFlow()

    override fun genres(sortingRule: SortingRule) = suspend {
        subsonicClient.getGenres().toRequestStatus {
            genre.maybeSortedBy(
                sortingRule.reverse,
                when (sortingRule.strategy) {
                    SortingStrategy.NAME -> { genre -> genre.value }

                    else -> null
                }
            ).map { it.toMediaItem() }
        }
    }.asFlow()

    override fun playlists(sortingRule: SortingRule) = _playlistsChanged.mapLatest {
        subsonicClient.getPlaylists().toRequestStatus {
            playlist.maybeSortedBy(
                sortingRule.reverse,
                when (sortingRule.strategy) {
                    SortingStrategy.CREATION_DATE -> { playlist ->
                        playlist.created
                    }

                    SortingStrategy.MODIFICATION_DATE -> { playlist ->
                        playlist.changed
                    }

                    SortingStrategy.NAME -> { playlist ->
                        playlist.name
                    }

                    else -> null
                }
            ).map { it.toMediaItem() }
        }
    }

    override fun search(query: String) = suspend {
        subsonicClient.search3(query).toRequestStatus {
            song.orEmpty().map { it.toMediaItem() } +
                    artist.orEmpty().map { it.toMediaItem() } +
                    album.orEmpty().map { it.toMediaItem() }
        }
    }.asFlow()

    override fun audio(audioUri: Uri) = suspend {
        subsonicClient.getSong(audioUri.lastPathSegment!!).toRequestStatus {
            toMediaItem()
        }
    }.asFlow()

    override fun album(albumUri: Uri) = suspend {
        subsonicClient.getAlbum(albumUri.lastPathSegment!!).toRequestStatus {
            toAlbumID3().toMediaItem() to song.map {
                it.toMediaItem()
            }
        }
    }.asFlow()

    override fun artist(artistUri: Uri) = suspend {
        subsonicClient.getArtist(artistUri.lastPathSegment!!).toRequestStatus {
            toArtistID3().toMediaItem() to ArtistWorks(
                albums = album.map { it.toMediaItem() },
                appearsInAlbum = listOf(),
                appearsInPlaylist = listOf(),
            )
        }
    }.asFlow()

    override fun genre(genreUri: Uri) = suspend {
        val genreName = genreUri.lastPathSegment!!

        val appearsInAlbums = subsonicClient.getAlbumList2(
            "byGenre",
            size = 500,
            genre = genreName
        ).toRequestStatus {
            album.map { it.toMediaItem() }
        }.let {
            when (it) {
                is RequestStatus.Success -> it.data
                else -> null
            }
        }

        val audios = subsonicClient.getSongsByGenre(genreName).toRequestStatus {
            song.map { it.toMediaItem() }
        }.let {
            when (it) {
                is RequestStatus.Success -> it.data
                else -> null
            }
        }

        val exists = listOf(
            appearsInAlbums,
            audios,
        ).any { it != null }

        if (exists) {
            RequestStatus.Success<_, MediaError>(
                Genre(genreUri, genreName) to GenreContent(
                    appearsInAlbums.orEmpty(),
                    listOf(),
                    audios.orEmpty(),
                )
            )
        } else {
            RequestStatus.Error(MediaError.NOT_FOUND)
        }
    }.asFlow()

    override fun playlist(playlistUri: Uri) = _playlistsChanged.mapLatest {
        subsonicClient.getPlaylist(playlistUri.lastPathSegment!!).toRequestStatus {
            toPlaylist().toMediaItem() to entry.orEmpty().map {
                it.toMediaItem()
            }
        }
    }

    override fun audioPlaylistsStatus(audioUri: Uri) = _playlistsChanged.mapLatest {
        val audioId = audioUri.lastPathSegment!!

        subsonicClient.getPlaylists().toRequestStatus {
            playlist.map { playlist ->
                playlist.toMediaItem() to subsonicClient.getPlaylist(playlist.id).toRequestStatus {
                    entry.orEmpty().any { child -> child.id == audioId }
                }.let { requestStatus ->
                    (requestStatus as? RequestStatus.Success)?.data ?: false
                }
            }
        }
    }

    override fun lastPlayedAudio() = lastPlayedGetter(lastPlayedKey())
        .flatMapLatest { uri ->
            uri?.let(this::audio) ?: flowOf(RequestStatus.Error(MediaError.NOT_FOUND))
        }

    override suspend fun createPlaylist(name: String) = subsonicClient.createPlaylist(
        null, name, listOf()
    ).toRequestStatus {
        onPlaylistsChanged()
        getPlaylistUri(id)
    }

    override suspend fun renamePlaylist(
        playlistUri: Uri, name: String
    ) = subsonicClient.updatePlaylist(playlistUri.lastPathSegment!!, name).toRequestStatus {
        onPlaylistsChanged()
    }

    override suspend fun deletePlaylist(playlistUri: Uri) = subsonicClient.deletePlaylist(
        playlistUri.lastPathSegment!!.toInt()
    ).toRequestStatus {
        onPlaylistsChanged()
    }

    override suspend fun addAudioToPlaylist(playlistUri: Uri, audioUri: Uri) =
        subsonicClient.updatePlaylist(
            playlistUri.lastPathSegment!!,
            songIdsToAdd = listOf(audioUri.lastPathSegment!!)
        ).toRequestStatus {
            onPlaylistsChanged()
        }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = subsonicClient.getPlaylist(
        playlistUri.lastPathSegment!!
    ).toRequestStatus {
        val audioId = audioUri.lastPathSegment!!

        val audioIndexes = entry.orEmpty().mapIndexedNotNull { index, child ->
            index.takeIf { child.id == audioId }
        }

        if (audioIndexes.isNotEmpty()) {
            subsonicClient.updatePlaylist(
                playlistUri.lastPathSegment!!,
                songIndexesToRemove = audioIndexes,
            ).toRequestStatus {
                onPlaylistsChanged()
            }
        }
    }

    override suspend fun onAudioPlayed(audioUri: Uri) = lastPlayedSetter(lastPlayedKey(), audioUri)
        .let { RequestStatus.Success<Unit, MediaError>(Unit) }

    private fun AlbumID3.toMediaItem() = Album(
        uri = getAlbumUri(id),
        title = name,
        artistUri = artistId?.let { getArtistUri(it) } ?: Uri.EMPTY,
        artistName = artist,
        year = year,
        thumbnail = Thumbnail.Builder()
            .setUri(Uri.parse(subsonicClient.getCoverArt(id)))
            .setType(Thumbnail.Type.FRONT_COVER)
            .build(),
    )

    private fun ArtistID3.toMediaItem() = Artist(
        uri = getArtistUri(id),
        name = name,
        thumbnail = Thumbnail.Builder()
            .setUri(Uri.parse(subsonicClient.getCoverArt(id)))
            .setType(Thumbnail.Type.BAND_ARTIST_LOGO)
            .build(),
    )

    private fun Child.toMediaItem() = Audio(
        uri = getAudioUri(id),
        playbackUri = Uri.parse(subsonicClient.stream(id)),
        mimeType = contentType ?: "",
        title = title,
        type = type.toAudioType(),
        durationMs = (duration?.toLong()?.let { it * 1000 }) ?: 0,
        artistUri = artistId?.let { getArtistUri(it) } ?: Uri.EMPTY,
        artistName = artist,
        albumUri = albumId?.let { getAlbumUri(it) } ?: Uri.EMPTY,
        albumTitle = album,
        discNumber = discNumber,
        trackNumber = track,
        genreUri = genre?.let { getGenreUri(it) },
        genreName = genre,
        year = year,
    )

    private fun org.lineageos.twelve.datasources.subsonic.models.Genre.toMediaItem() = Genre(
        uri = getGenreUri(value),
        name = value,
    )

    private fun org.lineageos.twelve.datasources.subsonic.models.Playlist.toMediaItem() = Playlist(
        uri = getPlaylistUri(id),
        name = name,
    )

    private fun org.lineageos.twelve.datasources.subsonic.models.MediaType?.toAudioType() = when (
        this
    ) {
        org.lineageos.twelve.datasources.subsonic.models.MediaType.MUSIC -> Audio.Type.MUSIC
        org.lineageos.twelve.datasources.subsonic.models.MediaType.PODCAST -> Audio.Type.PODCAST
        org.lineageos.twelve.datasources.subsonic.models.MediaType.AUDIOBOOK -> Audio.Type.AUDIOBOOK
        org.lineageos.twelve.datasources.subsonic.models.MediaType.VIDEO -> throw Exception(
            "Invalid media type, got VIDEO"
        )

        else -> Audio.Type.MUSIC
    }

    private fun Error.Code.toRequestStatusType() = when (this) {
        Error.Code.GENERIC_ERROR -> MediaError.IO
        Error.Code.REQUIRED_PARAMETER_MISSING -> MediaError.IO
        Error.Code.OUTDATED_CLIENT -> MediaError.IO
        Error.Code.OUTDATED_SERVER -> MediaError.IO
        Error.Code.WRONG_CREDENTIALS -> MediaError.INVALID_CREDENTIALS
        Error.Code.TOKEN_AUTHENTICATION_NOT_SUPPORTED -> MediaError.INVALID_CREDENTIALS
        Error.Code.AUTHENTICATION_MECHANISM_NOT_SUPPORTED -> MediaError.INVALID_CREDENTIALS
        Error.Code.MULTIPLE_CONFLICTING_AUTHENTICATION_MECHANISMS -> MediaError.INVALID_CREDENTIALS
        Error.Code.INVALID_API_KEY -> MediaError.INVALID_CREDENTIALS
        Error.Code.USER_NOT_AUTHORIZED -> MediaError.INVALID_CREDENTIALS
        Error.Code.SUBSONIC_PREMIUM_TRIAL_ENDED -> MediaError.INVALID_CREDENTIALS
        Error.Code.NOT_FOUND -> MediaError.NOT_FOUND
    }

    private fun getAlbumUri(albumId: String) = albumsUri.buildUpon()
        .appendPath(albumId)
        .build()

    private fun getArtistUri(artistId: String) = artistsUri.buildUpon()
        .appendPath(artistId)
        .build()

    private fun getAudioUri(audioId: String) = audiosUri.buildUpon()
        .appendPath(audioId)
        .build()

    private fun getGenreUri(genre: String) = genresUri.buildUpon()
        .appendPath(genre)
        .build()

    private fun getPlaylistUri(playlistId: String) = playlistsUri.buildUpon()
        .appendPath(playlistId)
        .build()

    private fun onPlaylistsChanged() {
        _playlistsChanged.value = Any()
    }

    /**
     * Apply [List.asReversed] if [condition] is true.
     * Reminder that [List.asReversed] returns a new list view, thus being O(1).
     */
    private fun <T> List<T>.asMaybeReversed(
        condition: Boolean,
    ) = when (condition) {
        true -> asReversed()
        else -> this
    }

    /**
     * Sort this list by the [selector] and apply [List.asReversed] if [reverse] is true.
     * If [selector] is null, return the original list.
     */
    private fun <T> List<T>.maybeSortedBy(
        reverse: Boolean,
        selector: ((T) -> Comparable<*>?)?,
    ) = selector?.let {
        @Suppress("UNCHECKED_CAST")
        sortedBy { t -> it(t) as? Comparable<Any?> }.asMaybeReversed(reverse)
    } ?: this

    private fun lastPlayedKey() = "subsonic:$username@$server"

    companion object {
        private const val ALBUMS_PATH = "albums"
        private const val ARTISTS_PATH = "artists"
        private const val AUDIOS_PATH = "audio"
        private const val GENRES_PATH = "genres"
        private const val PLAYLISTS_PATH = "playlists"

        val ARG_SERVER = ProviderArgument(
            "server",
            String::class,
            R.string.provider_argument_server,
            required = true,
            hidden = false,
            validate = {
                when (it.toHttpUrlOrNull()) {
                    null -> ProviderArgument.ValidationError(
                        "Invalid URL",
                        R.string.provider_argument_validation_error_malformed_http_uri,
                    )

                    else -> null
                }
            }
        )

        val ARG_USERNAME = ProviderArgument(
            "username",
            String::class,
            R.string.provider_argument_username,
            required = true,
            hidden = false,
        )

        val ARG_PASSWORD = ProviderArgument(
            "password",
            String::class,
            R.string.provider_argument_password,
            required = true,
            hidden = true,
        )

        val ARG_USE_LEGACY_AUTHENTICATION = ProviderArgument(
            "use_legacy_authentication",
            Boolean::class,
            R.string.provider_argument_use_legacy_authentication,
            required = true,
            hidden = false,
            defaultValue = false,
        )
    }
}
