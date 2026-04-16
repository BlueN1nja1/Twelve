/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache

import androidx.core.net.toUri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.ampache.models.Album
import org.lineageos.twelve.datasources.ampache.models.Albums
import org.lineageos.twelve.datasources.ampache.models.Artist
import org.lineageos.twelve.datasources.ampache.models.Artists
import org.lineageos.twelve.datasources.ampache.models.Genre
import org.lineageos.twelve.datasources.ampache.models.Genres
import org.lineageos.twelve.datasources.ampache.models.Lyrics
import org.lineageos.twelve.datasources.ampache.models.Ping
import org.lineageos.twelve.datasources.ampache.models.Playlist
import org.lineageos.twelve.datasources.ampache.models.Playlists
import org.lineageos.twelve.datasources.ampache.models.Preferences
import org.lineageos.twelve.datasources.ampache.models.Search
import org.lineageos.twelve.datasources.ampache.models.Song
import org.lineageos.twelve.datasources.ampache.models.Songs
import org.lineageos.twelve.datasources.ampache.models.Stats
import org.lineageos.twelve.datasources.ampache.models.Success
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest
import org.lineageos.twelve.utils.mapToError
import java.security.MessageDigest
import java.time.Instant

/**
 * Ampache client.
 *
 * Compliant with API6.
 */
class AmpacheClient(
    server: String,
    username: String,
    password: String,
    applicationName: String,
    private val tokenGetter: () -> Pair<String, Instant>?,
    private val tokenSetter: (Pair<String, Instant>?) -> Unit,
    cache: Cache? = null,
) {
    private var tokenProperty: Pair<String, Instant>?
        get() = tokenGetter()
        set(value) {
            tokenSetter(value)
        }

    private val interceptor = AmpacheInterceptor(
        username,
        password,
        applicationName,
        ::tokenProperty,
    )

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .cache(cache)
        .build()

    private val serverUri = server.toUri().buildUpon()
        .appendPath("server")
        .appendPath("json.server.php")
        .build()

    private val api = Api(okHttpClient, serverUri)

    /**
     * This can be called without being authenticated, it is useful for determining if what the
     * status of the server is, and what version it is running/compatible with.
     */
    suspend fun ping() = action<Ping>("ping")

    /**
     * Check Ampache for updates and run the update if there is one.
     */
    suspend fun systemUpdate() = action<Success>("system_update")

    /**
     * Get your server preferences.
     *
     * Access required: 100 (Admin)
     */
    suspend fun systemPreferences() = action<Preferences>("system_preferences")

    /**
     * Get your user preferences.
     */
    suspend fun userPreferences() = action<Preferences>("user_preferences")

    /**
     * This returns albums based on the provided search filters.
     *
     * @param filter Filter results to match this string
     * @param includeAlbums Whether to include albums in the response
     * @param includeSongs Whether to include songs in the response
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun albums(
        filter: String? = null,
        includeAlbums: Boolean = false,
        includeSongs: Boolean = false,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Albums>(
        "albums",
        listOf(
            "filter" to filter,
            "include" to when {
                includeAlbums && includeSongs -> "albums,songs"
                includeAlbums -> "albums"
                includeSongs -> "songs"
                else -> null
            },
            "exact" to when (exact) {
                true -> 1
                false -> 0
                null -> null
            },
            "add" to add,
            "update" to update,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single album based on the UID provided.
     *
     * @param filter UID of Album, returns album JSON
     * @param includeSongs Whether to include songs in the response
     */
    suspend fun album(
        filter: String,
        includeSongs: Boolean = false,
    ) = action<Album>(
        "album",
        listOf(
            "filter" to filter,
            "include" to when {
                includeSongs -> "songs"
                else -> null
            },
        ),
    )

    /**
     * This returns the songs of a specified album.
     *
     * @param filter UID of Album, returns song JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated
     *             comma string pairs (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun albumSongs(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Songs>(
        "album_songs",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This takes a collection of inputs and returns artist objects.
     *
     * @param filter Filter results to match this string
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param includeAlbums Whether to include albums in the response
     * @param includeSongs Whether to include songs in the response
     * @param albumArtist if true filter for album artists only
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun artists(
        filter: String? = null,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        includeAlbums: Boolean = false,
        includeSongs: Boolean = false,
        albumArtist: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Artists>(
        "artists",
        listOf(
            "filter" to filter,
            "exact" to when (exact) {
                true -> 1
                false -> 0
                null -> null
            },
            "add" to add,
            "update" to update,
            "include" to when {
                includeAlbums && includeSongs -> "albums,songs"
                includeAlbums -> "albums"
                includeSongs -> "songs"
                else -> null
            },
            "album_artist" to when (albumArtist) {
                true -> 1
                false -> 0
                null -> null
            },
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single artist based on the UID of said artist.
     *
     * @param filter UID of Artist, returns artist JSON
     * @param includeAlbums Whether to include albums in the response
     * @param includeSongs Whether to include songs in the response
     */
    suspend fun artist(
        filter: String,
        includeAlbums: Boolean = false,
        includeSongs: Boolean = false,
    ) = action<Artist>(
        "artist",
        listOf(
            "filter" to filter,
            "include" to when {
                includeAlbums && includeSongs -> "albums,songs"
                includeAlbums -> "albums"
                includeSongs -> "songs"
                else -> null
            }
        ),
    )

    /**
     * This flags a library item as a favorite.
     *
     * @param type `song`, `album`, `artist`, `playlist`, `podcast`
     * @param filter UID of item to flag
     * @param flag Flag the item
     */
    suspend fun flag(
        type: String,
        filter: String,
        flag: Boolean,
    ) = action<Success>(
        "flag",
        listOf(
            "type" to type,
            "filter" to filter,
            "flag" to when (flag) {
                true -> 1
                false -> 0
            },
        ),
    )

    /**
     * This returns the genres (Tags) based on the specified filter
     *
     * @param filter Filter results to match this string
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genres(
        filter: String? = null,
        exact: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Genres>(
        "genres",
        listOf(
            "filter" to filter,
            "exact" to when (exact) {
                true -> 1
                false -> 0
                null -> null
            },
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single genre based on UID.
     *
     * @param filter UID of genre, returns genre JSON
     */
    suspend fun genre(
        filter: String,
    ) = action<Genre>(
        "genre",
        listOf(
            "filter" to filter,
        ),
    )

    /**
     * This returns the albums associated with the genre in question.
     *
     * @param filter UID of genre, returns album JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genreAlbums(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Albums>(
        "genre_albums",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns the artists associated with the genre in question as defined by the UID.
     *
     * @param filter UID of genre, returns artist JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genreArtists(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Artists>(
        "genre_artists",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * Returns the songs for this genre.
     *
     * @param filter UID of genre, returns song JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genreSongs(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Songs>(
        "genre_songs",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * Return Database lyrics or search with plugins by Song id.
     *
     * @param filter song_id to find
     * @param plugins if false disable plugin lookup (default: 1)
     */
    suspend fun getLyrics(
        filter: String,
        plugins: Boolean,
    ) = action<Lyrics>(
        "get_lyrics",
        listOf(
            "filter" to filter,
            "plugins" to when (plugins) {
                true -> 1
                false -> 0
            },
        ),
    )

    /**
     * This returns playlists based on the specified filter.
     *
     * @param filter Filter results to match this string
     * @param hideSearch if true do not include searches/smartlists in the result
     * @param showDupes if true if true ignore 'api_hide_dupe_searches' setting
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun playlists(
        filter: String? = null,
        hideSearch: Boolean? = null,
        showDupes: Boolean? = null,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Playlists>(
        "playlists",
        listOf(
            "filter" to filter,
            "hide_search" to when (hideSearch) {
                true -> 1
                false -> 0
                null -> null
            },
            "show_dupes" to when (showDupes) {
                true -> 1
                false -> 0
                null -> null
            },
            "exact" to when (exact) {
                true -> 1
                false -> 0
                null -> null
            },
            "add" to add,
            "update" to update,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single playlist.
     *
     * @param filter UID of playlist, returns playlist JSON
     */
    suspend fun playlist(
        filter: String,
    ) = action<Playlist>(
        "playlist",
        listOf(
            "filter" to filter,
        ),
    )

    /**
     * This adds a song to a playlist.
     *
     * @param filter UID of Playlist
     * @param id UID of the object to add to playlist
     * @param type `song`, `album`, `artist`, `playlist`
     * @param check setting check=1 will not add duplicates to the playlist
     */
    suspend fun playlistAdd(
        filter: String,
        id: String,
        type: String,
        check: Boolean? = null,
    ) = action<Success>(
        "playlist_add",
        listOf(
            "filter" to filter,
            "id" to id,
            "type" to type,
            "check" to when (check) {
                true -> 1
                false -> 0
                null -> null
            },
        ),
    )

    /**
     * This create a new playlist and return it
     *
     * @param name Playlist name
     * @param type `public`, `private` (Playlist type)
     */
    suspend fun playlistCreate(
        name: String,
        type: Playlist.Type? = null,
    ) = action<Playlist>(
        "playlist_create",
        listOf(
            "name" to name,
            "type" to type?.value,
        ),
    )

    /**
     * This deletes a playlist.
     *
     * @param filter UID of Playlist
     */
    suspend fun playlistDelete(
        filter: String,
    ) = action<Success>(
        "playlist_delete",
        listOf(
            "filter" to filter,
        ),
    )

    /**
     * This modifies name and type of a playlist Previously name and type were mandatory while
     * filter wasn't. This has been reversed.
     *
     * @param filter UID of Playlist
     * @param name Playlist name
     * @param type `public`, `private` (Playlist type)
     * @param owner Change playlist owner to the user id (-1 = System playlist)
     * @param items comma-separated song_id's (replaces existing items with a new id)
     * @param tracks comma-separated playlisttrack numbers matched to 'items' in order
     */
    suspend fun playlistEdit(
        filter: String,
        name: String? = null,
        type: Playlist.Type? = null,
        owner: String? = null,
        items: List<String>? = null,
        tracks: List<Int>? = null,
    ) = action<Success>(
        "playlist_edit",
        listOf(
            "filter" to filter,
            "name" to name,
            "type" to type?.value,
            "owner" to owner,
            "items" to items?.joinToString(","),
            "tracks" to tracks?.joinToString(","),
        )
    )

    /**
     * This remove a song from a playlist. Previous versions required 'track' instead of 'song'.
     *
     * @param filter UID of Playlist
     * @param song UID of song to remove from playlist
     * @param track Track number to remove from playlist
     */
    suspend fun playlistRemoveSong(
        filter: String,
        song: String? = null,
        track: Int? = null,
    ) = action<Success>(
        "playlist_remove_song",
        listOf(
            "filter" to filter,
            "song" to song,
            "track" to track,
        ),
    )

    /**
     * This returns the songs for a playlist.
     *
     * @param filter UID of Playlist, returns song JSON
     * @param random if true get random songs using limit
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     */
    suspend fun playlistSongs(
        filter: String,
        random: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
    ) = action<Songs>(
        "playlist_songs",
        listOf(
            "filter" to filter,
            "random" to random,
            "offset" to offset,
            "limit" to limit,
        ),
    )

    /**
     * Perform a group search given passed rules. This function will return multiple object types if
     * the rule names match the object type. You can pass multiple rules as well as joins to create
     * in depth search results.
     *
     * Limit and offset are applied per object type. Meaning with a limit of 10 you will return 10
     * objects of each type not 10 results total.
     *
     * Rules must be sent in groups of 3 using an int (starting from 1) to designate which rules are
     * combined. Use operator ('and', 'or') to choose whether to join or separate each rule when
     * searching.
     *
     * Refer to the Advanced Search page for details about creating searches.
     *
     * @param operator `and`, `or` (whether to match one rule or all)
     * @param rules List of rules to search with (rule, operator, input)
     * @param type `all`, `music`, `song_artist`, `album_artist`, `podcast`, `video`
     *             (all by default)
     * @param random random order of results; default to 0
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     */
    suspend fun searchGroup(
        operator: String,
        rules: List<Triple<String, Int, String>>,
        type: String? = null,
        random: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
    ) = action<Search>(
        "search_group",
        listOf(
            "operator" to operator,
            "type" to type,
            "random" to when (random) {
                true -> 1
                false -> 0
                null -> null
            },
            "offset" to offset,
            "limit" to limit,
        ) + rules.mapIndexed { index, (rule, operator, input) ->
            listOf(
                "rule_${index + 1}" to rule,
                "rule_${index + 1}_operator" to operator,
                "rule_${index + 1}_input" to input,
            )
        }.flatten()
    )

    /**
     * Returns songs based on the specified filter.
     *
     * @param filter Filter results to match this string
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun songs(
        filter: String? = null,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Songs>(
        "songs",
        listOf(
            "filter" to filter,
            "exact" to exact,
            "add" to add,
            "update" to update,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * Returns a single song.
     *
     * @param filter UID of Song, returns song JSON
     */
    suspend fun song(
        filter: String,
    ) = action<Song>(
        "song",
        listOf(
            "filter" to filter,
        ),
    )

    /**
     * Get some items based on some simple search types and filters. (Random by default). This
     * method HAD partial backwards compatibility with older api versions but it has now been
     * removed. Pass -1 limit to get all results. (0 will fall back to the popular_threshold value).
     *
     * @param type `song`, `album`, `artist`, `video`, `playlist`, `podcast`, `podcast_episode`
     * @param filter `newest`, `highest`, `frequent`, `recent`, `forgotten`, `flagged`, `random`
     * @param userId User ID to filter by
     * @param username Username to filter by
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results (Use popular_threshold when missing; default 10)
     */
    suspend fun stats(
        type: String,
        filter: String? = null,
        userId: Int? = null,
        username: String? = null,
        offset: Int? = null,
        limit: Int? = null,
    ) = action<Stats>(
        "stats",
        listOf(
            "type" to type,
            "filter" to filter,
            "user_id" to userId,
            "username" to username,
            "offset" to offset,
            "limit" to limit,
        ),
    )

    private suspend inline fun <reified T> action(
        action: String,
        parameters: List<Pair<String, Any?>> = emptyList(),
    ): Result<T, Error> = ApiRequest.get<T>(
        listOf(),
        listOf(
            "action" to action,
        ) + parameters
    ).execute(api).mapToError()

    companion object {
        const val API_VERSION = "6.0.0"

        @OptIn(ExperimentalStdlibApi::class)
        fun calculatePassphrase(
            password: String,
            instant: Instant,
        ): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val time = instant.epochSecond
            val key = digest.digest(password.toByteArray()).toHexString()
            return digest.digest((time.toString() + key).toByteArray()).toHexString()
        }
    }
}
