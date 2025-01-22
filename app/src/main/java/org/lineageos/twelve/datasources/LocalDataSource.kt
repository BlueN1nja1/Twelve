/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.os.bundleOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.lineageos.twelve.R
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.database.entities.Item
import org.lineageos.twelve.ext.mapEachRow
import org.lineageos.twelve.ext.queryFlow
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.ColumnIndexCache
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.LocalizedString
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.RequestStatus.Companion.fold
import org.lineageos.twelve.models.RequestStatus.Companion.map
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import org.lineageos.twelve.models.Thumbnail
import org.lineageos.twelve.query.Query
import org.lineageos.twelve.query.and
import org.lineageos.twelve.query.eq
import org.lineageos.twelve.query.`in`
import org.lineageos.twelve.query.`is`
import org.lineageos.twelve.query.like
import org.lineageos.twelve.query.neq
import org.lineageos.twelve.query.query
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * [MediaStore.Audio] backed data source.
 *
 * @param contentResolver The [ContentResolver]
 * @param volumeName The volume name
 * @param database The app's database
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalDataSource(
    private val contentResolver: ContentResolver,
    private val volumeName: String,
    private val database: TwelveDatabase
) : MediaDataSource {
    private val albumsUri = MediaStore.Audio.Albums.getContentUri(volumeName)
    private val artistsUri = MediaStore.Audio.Artists.getContentUri(volumeName)
    private val genresUri = MediaStore.Audio.Genres.getContentUri(volumeName)
    private val audiosUri = MediaStore.Audio.Media.getContentUri(volumeName)

    private val mapAlbum = { columnIndexCache: ColumnIndexCache ->
        val albumId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns._ID)
        val album = columnIndexCache.getString(MediaStore.Audio.AlbumColumns.ALBUM)
        val artistId = columnIndexCache.getLong(MediaStore.Audio.AlbumColumns.ARTIST_ID)
        val artist = columnIndexCache.getString(MediaStore.Audio.AlbumColumns.ARTIST)
        val lastYear = columnIndexCache.getInt(MediaStore.Audio.AlbumColumns.LAST_YEAR)

        val uri = ContentUris.withAppendedId(albumsUri, albumId)
        val artistUri = ContentUris.withAppendedId(artistsUri, artistId)

        val thumbnail = Thumbnail.Builder()
            .setUri(uri)
            .setType(Thumbnail.Type.FRONT_COVER)
            .build()

        Album(
            uri,
            album.takeIf { it != MediaStore.UNKNOWN_STRING },
            artistUri,
            artist.takeIf { it != MediaStore.UNKNOWN_STRING },
            lastYear.takeIf { it != 0 },
            thumbnail,
        )
    }

    private val mapArtist = { columnIndexCache: ColumnIndexCache ->
        val artistId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns._ID)
        val artist = columnIndexCache.getString(MediaStore.Audio.ArtistColumns.ARTIST)

        val uri = ContentUris.withAppendedId(artistsUri, artistId)

        val thumbnail = Thumbnail.Builder()
            .setUri(uri)
            .setType(Thumbnail.Type.BAND_ARTIST_LOGO)
            .build()

        Artist(
            uri,
            artist.takeIf { it != MediaStore.UNKNOWN_STRING },
            thumbnail,
        )
    }

    private val mapGenre = { columnIndexCache: ColumnIndexCache ->
        val genreId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns._ID)
        val name = columnIndexCache.getStringOrNull(MediaStore.Audio.GenresColumns.NAME)

        val uri = ContentUris.withAppendedId(genresUri, genreId)

        Genre(
            uri,
            name,
        )
    }

    private val mapAudio = { columnIndexCache: ColumnIndexCache ->
        val audioId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns._ID)
        val mimeType = columnIndexCache.getString(MediaStore.Audio.AudioColumns.MIME_TYPE)
        val title = columnIndexCache.getString(MediaStore.Audio.AudioColumns.TITLE)
        val isMusic = columnIndexCache.getBoolean(MediaStore.Audio.AudioColumns.IS_MUSIC)
        val isPodcast = columnIndexCache.getBoolean(MediaStore.Audio.AudioColumns.IS_PODCAST)
        val isAudiobook = columnIndexCache.getBoolean(MediaStore.Audio.AudioColumns.IS_AUDIOBOOK)
        val duration = columnIndexCache.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val artistId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artist = columnIndexCache.getString(MediaStore.Audio.AudioColumns.ARTIST)
        val albumId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val album = columnIndexCache.getString(MediaStore.Audio.AudioColumns.ALBUM)
        val track = columnIndexCache.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val genreId = columnIndexCache.getLong(MediaStore.Audio.AudioColumns.GENRE_ID)
        val genre = columnIndexCache.getStringOrNull(MediaStore.Audio.AudioColumns.GENRE)
        val year = columnIndexCache.getInt(MediaStore.Audio.AudioColumns.YEAR)

        val isRecording = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            columnIndexCache.getBoolean(MediaStore.Audio.AudioColumns.IS_RECORDING)
        } else {
            false
        }

        val uri = ContentUris.withAppendedId(audiosUri, audioId)
        val artistUri = ContentUris.withAppendedId(artistsUri, artistId)
        val albumUri = ContentUris.withAppendedId(albumsUri, albumId)
        val genreUri = ContentUris.withAppendedId(genresUri, genreId)

        val audioType = when {
            isMusic -> Audio.Type.MUSIC
            isPodcast -> Audio.Type.PODCAST
            isAudiobook -> Audio.Type.AUDIOBOOK
            isRecording -> Audio.Type.RECORDING
            else -> Audio.Type.MUSIC
        }

        val (discNumber, discTrack) = track.takeUnless { it == 0 }?.let {
            when (track > 1000) {
                true -> track / 1000 to track % 1000
                false -> null to track
            }
        } ?: (null to null)

        Audio(
            uri,
            uri,
            mimeType,
            title,
            audioType,
            duration,
            artistUri,
            artist.takeIf { it != MediaStore.UNKNOWN_STRING },
            albumUri,
            album.takeIf { it != MediaStore.UNKNOWN_STRING },
            discNumber,
            discTrack,
            genreUri,
            genre,
            year.takeIf { it != 0 },
        )
    }

    override fun status() = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<DataSourceInformation>())
    )

    override fun isMediaItemCompatible(mediaItemUri: Uri) = listOf(
        albumsUri,
        artistsUri,
        genresUri,
        audiosUri,
        playlistsBaseUri,
    ).any {
        mediaItemUri.toString().startsWith(it.toString())
    }

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = with(mediaItemUri.toString()) {
        when {
            startsWith(albumsUri.toString()) -> MediaType.ALBUM
            startsWith(artistsUri.toString()) -> MediaType.ARTIST
            startsWith(genresUri.toString()) -> MediaType.GENRE
            startsWith(audiosUri.toString()) -> MediaType.AUDIO
            startsWith(playlistsBaseUri.toString()) -> MediaType.PLAYLIST
            else -> null
        }?.let {
            RequestStatus.Success<_, MediaError>(it)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun activity() = combine(
        lastPlayedMediaItems(),
        mostPlayedAlbums(),
        albums(SortingRule(SortingStrategy.NAME)),
        artists(SortingRule(SortingStrategy.NAME)),
        genres(SortingRule(SortingStrategy.NAME)),
    ) { lastPlayed, mostPlayed, albums, artists, genres ->
        val now = LocalDateTime.now()

        RequestStatus.Success<_, MediaError>(
            listOf(
                lastPlayed.map {
                    ActivityTab(
                        "last_played",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.activity_last_played,
                        ),
                        it,
                    )
                },
                mostPlayed.map {
                    ActivityTab(
                        "most_played_albums",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.activity_most_played_albums
                        ),
                        it,
                    )
                },
                albums.map {
                    ActivityTab(
                        "random_albums",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.activity_random_albums
                        ),
                        it.shuffled(Random(now.dayOfYear)),
                    )
                },
                artists.map {
                    ActivityTab(
                        "random_artists",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.activity_random_artists
                        ),
                        it.shuffled(Random(now.dayOfYear)),
                    )
                },
                genres.map {
                    ActivityTab(
                        "random_genres",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.activity_random_genres
                        ),
                        it.shuffled(Random(now.dayOfYear)),
                    )
                },
            ).mapNotNull {
                (it as? RequestStatus.Success)?.data?.takeIf { activityTab ->
                    activityTab.items.isNotEmpty()
                }
            }
        )
    }

    override fun albums(sortingRule: SortingRule) = contentResolver.queryFlow(
        albumsUri,
        albumsProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SORT_COLUMNS to listOfNotNull(
                when (sortingRule.strategy) {
                    SortingStrategy.ARTIST_NAME -> MediaStore.Audio.AlbumColumns.ARTIST
                    SortingStrategy.CREATION_DATE -> MediaStore.Audio.AlbumColumns.LAST_YEAR
                    SortingStrategy.NAME -> MediaStore.Audio.AlbumColumns.ALBUM
                    else -> null
                }?.let { column ->
                    when (sortingRule.reverse) {
                        true -> "$column DESC"
                        false -> column
                    }
                },
                MediaStore.Audio.AlbumColumns.ALBUM.takeIf {
                    sortingRule.strategy != SortingStrategy.NAME
                },
            ).toTypedArray(),
        )
    ).mapEachRow(mapAlbum).map {
        RequestStatus.Success<_, MediaError>(it)
    }

    override fun artists(sortingRule: SortingRule) = contentResolver.queryFlow(
        artistsUri,
        artistsProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SORT_COLUMNS to listOfNotNull(
                when (sortingRule.strategy) {
                    SortingStrategy.NAME -> MediaStore.Audio.ArtistColumns.ARTIST
                    else -> null
                }?.let { column ->
                    when (sortingRule.reverse) {
                        true -> "$column DESC"
                        false -> column
                    }
                },
                MediaStore.Audio.ArtistColumns.ARTIST.takeIf {
                    sortingRule.strategy != SortingStrategy.NAME
                },
            ).toTypedArray(),
        )
    ).mapEachRow(mapArtist).map {
        RequestStatus.Success<_, MediaError>(it)
    }

    override fun genres(sortingRule: SortingRule) = contentResolver.queryFlow(
        genresUri,
        genresProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SORT_COLUMNS to listOfNotNull(
                when (sortingRule.strategy) {
                    SortingStrategy.NAME -> MediaStore.Audio.GenresColumns.NAME
                    else -> null
                }?.let { column ->
                    when (sortingRule.reverse) {
                        true -> "$column DESC"
                        false -> column
                    }
                },
                MediaStore.Audio.GenresColumns.NAME.takeIf {
                    sortingRule.strategy != SortingStrategy.NAME
                },
            ).toTypedArray(),
        )
    ).mapEachRow(mapGenre).map {
        RequestStatus.Success<_, MediaError>(it)
    }

    override fun playlists(sortingRule: SortingRule) = database.getPlaylistDao().getAll()
        .mapLatest { playlists ->
            RequestStatus.Success<_, MediaError>(playlists.map { it.toModel() })
        }

    override fun search(query: String) = combine(
        contentResolver.queryFlow(
            albumsUri,
            albumsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AlbumColumns.ALBUM like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(mapAlbum),
        contentResolver.queryFlow(
            artistsUri,
            artistsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.ArtistColumns.ARTIST like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(mapArtist),
        contentResolver.queryFlow(
            audiosUri,
            audiosProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns.TITLE like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(mapAudio),
        contentResolver.queryFlow(
            genresUri,
            genresProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.GenresColumns.NAME like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(mapGenre),
    ) { albums, artists, audios, genres ->
        albums + artists + audios + genres
    }.map { RequestStatus.Success<_, MediaError>(it) }

    override fun audio(audioUri: Uri) = contentResolver.queryFlow(
        audiosUri,
        audiosProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                MediaStore.Audio.AudioColumns._ID eq Query.ARG
            },
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                ContentUris.parseId(audioUri).toString(),
            ),
        )
    ).mapEachRow(mapAudio).mapLatest { audios ->
        audios.firstOrNull()?.let {
            RequestStatus.Success<_, MediaError>(it)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun album(albumUri: Uri) = combine(
        contentResolver.queryFlow(
            albumsUri,
            albumsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns._ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(albumUri).toString(),
                ),
            )
        ).mapEachRow(mapAlbum),
        contentResolver.queryFlow(
            audiosUri,
            audiosProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns.ALBUM_ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(albumUri).toString(),
                ),
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(
                    MediaStore.Audio.AudioColumns.TRACK,
                )
            )
        ).mapEachRow(mapAudio)
    ) { albums, audios ->
        albums.firstOrNull()?.let { album ->
            RequestStatus.Success<_, MediaError>(album to audios)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun artist(artistUri: Uri) = combine(
        contentResolver.queryFlow(
            artistsUri,
            artistsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns._ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(artistUri).toString(),
                ),
            )
        ).mapEachRow(mapArtist),
        contentResolver.queryFlow(
            albumsUri,
            albumsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AlbumColumns.ARTIST_ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(artistUri).toString(),
                ),
            )
        ).mapEachRow(mapAlbum),
        contentResolver.queryFlow(
            audiosUri,
            audioAlbumIdsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns.ARTIST_ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(artistUri).toString(),
                ),
                ContentResolver.QUERY_ARG_SQL_GROUP_BY to MediaStore.Audio.AudioColumns.ALBUM_ID,
            )
        ).mapEachRow {
            it.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        }.flatMapLatest { albumIds ->
            contentResolver.queryFlow(
                albumsUri,
                albumsProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        (MediaStore.Audio.AudioColumns.ARTIST_ID neq Query.ARG) and
                                (MediaStore.Audio.AudioColumns._ID `in` List(albumIds.size) {
                                    Query.ARG
                                })
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        ContentUris.parseId(artistUri).toString(),
                        *albumIds
                            .map { it.toString() }
                            .toTypedArray(),
                    ),
                )
            ).mapEachRow(mapAlbum)
        }
    ) { artists, albums, appearsInAlbum ->
        artists.firstOrNull()?.let { artist ->
            val artistWorks = ArtistWorks(
                albums,
                appearsInAlbum,
                listOf(),
            )

            RequestStatus.Success<_, MediaError>(artist to artistWorks)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun genre(genreUri: Uri) = ContentUris.parseId(genreUri).let { genreId ->
        val (genreSelection, genreSelectionArgs) = when (genreId) {
            0L -> (MediaStore.Audio.AudioColumns.GENRE_ID `is` Query.NULL) to arrayOf()

            else -> (MediaStore.Audio.AudioColumns.GENRE_ID eq Query.ARG) to
                    arrayOf(genreId.toString())
        }

        combine(
            contentResolver.queryFlow(
                genresUri,
                genresProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        when (genreId) {
                            0L -> MediaStore.Audio.AudioColumns._ID `is` Query.NULL
                            else -> MediaStore.Audio.AudioColumns._ID eq Query.ARG
                        }
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        *when (genreId) {
                            0L -> arrayOf()
                            else -> arrayOf(genreId.toString())
                        }
                    ),
                )
            ).mapEachRow(mapGenre),
            contentResolver.queryFlow(
                audiosUri,
                audioAlbumIdsProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        genreSelection
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        *genreSelectionArgs,
                    ),
                    ContentResolver.QUERY_ARG_SQL_GROUP_BY to
                            MediaStore.Audio.AudioColumns.ALBUM_ID,
                )
            ).mapEachRow {
                it.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
            }.flatMapLatest { albumIds ->
                contentResolver.queryFlow(
                    albumsUri,
                    albumsProjection,
                    bundleOf(
                        ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                            MediaStore.Audio.AudioColumns._ID `in` List(albumIds.size) {
                                Query.ARG
                            }
                        },
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                            *albumIds
                                .map { it.toString() }
                                .toTypedArray(),
                        ),
                    )
                ).mapEachRow(mapAlbum)
            },
            contentResolver.queryFlow(
                audiosUri,
                audiosProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        genreSelection
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        *genreSelectionArgs,
                    ),
                )
            ).mapEachRow(mapAudio)
        ) { genres, appearsInAlbums, audios ->
            val genre = genres.firstOrNull() ?: when (genreId) {
                0L -> Genre(genreUri, null)
                else -> null
            }

            genre?.let {
                val genreContent = GenreContent(
                    appearsInAlbums,
                    listOf(),
                    audios,
                )

                RequestStatus.Success<_, MediaError>(it to genreContent)
            } ?: RequestStatus.Error(MediaError.NOT_FOUND)
        }
    }

    override fun playlist(playlistUri: Uri) = database.getPlaylistDao().getPlaylistWithItems(
        ContentUris.parseId(playlistUri)
    ).flatMapLatest { data ->
        data?.let { playlistWithItems ->
            val playlist = playlistWithItems.playlist.toModel()

            audios(playlistWithItems.items.map(Item::audioUri))
                .mapLatest { items ->
                    RequestStatus.Success<_, MediaError>(playlist to items.filterNotNull())
                }
        } ?: flowOf(
            RequestStatus.Error(
                MediaError.NOT_FOUND
            )
        )
    }

    override fun audioPlaylistsStatus(audioUri: Uri) =
        database.getPlaylistWithItemsDao().getPlaylistsWithItemStatus(
            audioUri
        ).mapLatest { data ->
            RequestStatus.Success<_, MediaError>(
                data.map {
                    it.playlist.toModel() to it.value
                }
            )
        }

    override fun lastPlayedAudio() = database.getLastPlayedDao()
        .get(LAST_PLAYED_KEY)
        .flatMapLatest { uri ->
            if (uri == null) {
                flowOf(listOf())
            } else {
                contentResolver.queryFlow(
                    audiosUri,
                    audiosProjection,
                    bundleOf(
                        ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                            MediaStore.Audio.AudioColumns._ID eq Query.ARG
                        },
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to listOf(
                            ContentUris.parseId(uri).toString()
                        ).toTypedArray(),
                    ),
                ).mapEachRow(mapAudio)
            }
        }
        .mapLatest { audios ->
            if (audios.isEmpty()) {
                RequestStatus.Error<Audio, MediaError>(MediaError.NOT_FOUND)
            } else {
                RequestStatus.Success(audios.first())
            }
        }

    override suspend fun createPlaylist(name: String) = database.getPlaylistDao().create(
        name
    ).let {
        RequestStatus.Success<_, MediaError>(ContentUris.withAppendedId(playlistsBaseUri, it))
    }

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        database.getPlaylistDao().rename(
            ContentUris.parseId(playlistUri), name
        ).let {
            RequestStatus.Success<_, MediaError>(Unit)
        }

    override suspend fun deletePlaylist(playlistUri: Uri) = database.getPlaylistDao().delete(
        ContentUris.parseId(playlistUri)
    ).let {
        RequestStatus.Success<_, MediaError>(Unit)
    }

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri,
    ) = database.getPlaylistWithItemsDao().addItemToPlaylist(
        ContentUris.parseId(playlistUri),
        audioUri
    ).let {
        RequestStatus.Success<_, MediaError>(Unit)
    }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri,
    ) = database.getPlaylistWithItemsDao().removeItemFromPlaylist(
        ContentUris.parseId(playlistUri),
        audioUri
    ).let {
        RequestStatus.Success<_, MediaError>(Unit)
    }

    override suspend fun onAudioPlayed(
        audioUri: Uri
    ): RequestStatus<Unit, MediaError> {
        database.getLocalMediaStatsProviderDao().increasePlayCount(audioUri)
        database.getLastPlayedDao().set(LAST_PLAYED_KEY, audioUri)
        return RequestStatus.Success(Unit)
    }

    fun audios() = contentResolver.queryFlow(
        audiosUri,
        audiosProjection
    ).mapEachRow(mapAudio)

    /**
     * Given a list of audio URIs, return a list of [Audio], where null if the audio hasn't been
     * found.
     */
    private fun audios(audioUris: List<Uri>) = contentResolver.queryFlow(
        audiosUri,
        audiosProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                MediaStore.Audio.AudioColumns._ID `in` List(audioUris.size) {
                    Query.ARG
                }
            },
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to audioUris.map {
                ContentUris.parseId(it).toString()
            }.toTypedArray(),
        )
    )
        .mapEachRow(mapAudio)
        .mapLatest { audios ->
            audioUris.map { audioUri ->
                audios.firstOrNull { it.uri == audioUri }
            }
        }

    private fun mostPlayedAlbums(nTopTracks: Int = 100) =
        database.getLocalMediaStatsProviderDao()
            .getAllByPlayCount(nTopTracks)
            .map { stats -> stats.map { it.mediaUri } }
            .flatMapLatest { uris ->
                contentResolver.queryFlow(
                    audiosUri,
                    arrayOf(MediaStore.Audio.AlbumColumns.ALBUM_ID),
                    bundleOf(
                        ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                            MediaStore.Audio.AudioColumns._ID `in` List(uris.size) {
                                Query.ARG
                            }
                        },
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to uris.map {
                            ContentUris.parseId(it).toString()
                        }.toTypedArray(),
                    )
                )
            }
            .mapEachRow { it.getLong(MediaStore.Audio.AlbumColumns.ALBUM_ID) }
            .map { it.distinct() }
            .flatMapLatest { uris ->
                contentResolver.queryFlow(
                    albumsUri,
                    albumsProjection,
                    bundleOf(
                        ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                            MediaStore.Audio.AlbumColumns.ALBUM_ID `in` List(uris.size) {
                                Query.ARG
                            }
                        },
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to uris.map {
                            it.toString()
                        }.toTypedArray(),
                    )
                ).mapEachRow(mapAlbum)
            }
            .mapLatest {
                RequestStatus.Success<List<Album>, MediaError>(it)
            }

    private fun lastPlayedMediaItems() = lastPlayedAudio().flatMapLatest { rs ->
        rs.fold(
            onSuccess = { audio ->
                contentResolver.queryFlow(
                    albumsUri,
                    albumsProjection,
                    bundleOf(
                        ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                            MediaStore.Audio.AlbumColumns.ALBUM_ID eq Query.ARG
                        },
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to listOf(
                            ContentUris.parseId(audio.albumUri).toString()
                        ).toTypedArray(),
                    )
                ).mapEachRow(mapAlbum).mapLatest { albums ->
                    RequestStatus.Success<List<MediaItem<*>>, MediaError>(
                        listOf(audio as MediaItem<*>) + albums,
                    )
                }
            },
            onLoading = { flowOf(RequestStatus.Error(MediaError.NOT_FOUND)) },
            onError = { flowOf(RequestStatus.Error(MediaError.NOT_FOUND)) },
        )
    }

    companion object {
        private const val LAST_PLAYED_KEY = "local"

        private val albumsProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST_ID,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.LAST_YEAR,
        )

        private val artistsProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.ArtistColumns.ARTIST,
        )

        private val genresProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.GenresColumns.NAME,
        )

        private val audiosProjection = mutableListOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.IS_MUSIC,
            MediaStore.Audio.AudioColumns.IS_PODCAST,
            MediaStore.Audio.AudioColumns.IS_AUDIOBOOK,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.GENRE_ID,
            MediaStore.Audio.AudioColumns.GENRE,
            MediaStore.Audio.AudioColumns.YEAR,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(MediaStore.Audio.AudioColumns.IS_RECORDING)
            }
        }.toTypedArray()

        private val audioAlbumIdsProjection = arrayOf(
            MediaStore.Audio.AudioColumns.ALBUM_ID,
        )

        /**
         * Dummy internal database scheme.
         */
        private const val DATABASE_SCHEME = "twelve_database"

        /**
         * Dummy database playlists authority.
         */
        private const val PLAYLISTS_AUTHORITY = "playlists"

        /**
         * Dummy internal database playlists [Uri].
         */
        private val playlistsBaseUri = Uri.Builder()
            .scheme(DATABASE_SCHEME)
            .authority(PLAYLISTS_AUTHORITY)
            .build()

        private fun org.lineageos.twelve.database.entities.Playlist.toModel() = Playlist(
            ContentUris.withAppendedId(playlistsBaseUri, id),
            name,
        )
    }
}
