/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.lineageos.twelve.ext.Bundle
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.ext.mapEachRow
import org.lineageos.twelve.ext.queryFlow
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.utils.MimeUtils
import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Generic file data source.
 * Supported protocols:
 * - `file`
 * - `content`
 * - `http`
 * - `https`
 * - `rtsp`
 */
class FileDataSource(
    private val contentResolver: ContentResolver,
    cache: Cache? = null,
) : MediaDataSource {
    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .build()

    override fun status(
        providerIdentifier: ProviderIdentifier,
    ) = flowOf(Result.Success(listOf<DataSourceInformation>()))

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = getMimeType(mediaItemUri)?.let {
        MimeUtils.mimeTypeToMediaType(it)
    }

    override fun providerOf(
        mediaItemUri: Uri
    ) = flowOf(Result.Failure(Error.NOT_FOUND))

    override fun activity(
        providerIdentifier: ProviderIdentifier,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    override fun albums(
        providerIdentifier: ProviderIdentifier,
        sortingRule: SortingRule,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    override fun artists(
        providerIdentifier: ProviderIdentifier,
        sortingRule: SortingRule,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    override fun audios(
        providerIdentifier: ProviderIdentifier,
        sortingRule: SortingRule,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    override fun genres(
        providerIdentifier: ProviderIdentifier,
        sortingRule: SortingRule,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    override fun playlists(
        providerIdentifier: ProviderIdentifier,
        sortingRule: SortingRule,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    override fun search(
        providerIdentifier: ProviderIdentifier,
        query: String,
    ) = flowOf(Result.Failure(Error.NOT_IMPLEMENTED))

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun audio(audioUri: Uri) = when (audioUri.scheme) {
        SCHEME_FILE -> suspend {
            Result.Success(
                Audio.Builder(audioUri)
                    .setPlaybackUri(audioUri)
                    .setMimeType(audioUri.determineFileMimeType())
                    .build()
            )
        }.asFlow()

        SCHEME_CONTENT -> contentResolver.queryFlow(
            audioUri,
            contentQueryProjection,
            Bundle {
                putInt(ContentResolver.QUERY_ARG_SQL_LIMIT, 1)
            },
        ).mapEachRow { columnIndexCache ->
            val displayName = columnIndexCache.getString(OpenableColumns.DISPLAY_NAME)

            Audio.Builder(audioUri)
                .setPlaybackUri(audioUri)
                .setMimeType(contentResolver.getCompatibleType(audioUri))
                .setTitle(displayName)
                .build()
        }.mapLatest { audios ->
            audios.firstOrNull()?.let {
                Result.Success(it)
            } ?: Result.Failure(Error.NOT_FOUND)
        }

        SCHEME_HTTP, SCHEME_HTTPS -> suspend {
            val request = Request.Builder()
                .url(audioUri.toString())
                .get() // Could use HEAD, but we want to peek the body
                .build()

            okHttpClient.newCall(request).executeAsync().use { response ->
                when (response.isSuccessful) {
                    true -> Result.Success(
                        Audio.Builder(audioUri)
                            .setPlaybackUri(audioUri)
                            .setMimeType(response.getContentType())
                            .setTitle(audioUri.lastPathSegment)
                            .build()
                    )

                    false -> Result.Failure(Error.IO)
                }
            }
        }.asFlow()

        SCHEME_RTSP -> suspend {
            Result.Success(
                Audio.Builder(audioUri)
                    .setPlaybackUri(audioUri)
                    .build()
            )
        }.asFlow()

        else -> flowOf(Result.Failure(Error.NOT_FOUND))
    }

    override fun album(albumUri: Uri) = flowOf(
        Result.Failure(Error.NOT_FOUND)
    )

    override fun artist(artistUri: Uri) = flowOf(
        Result.Failure(Error.NOT_FOUND)
    )

    override fun genre(genreUri: Uri) = flowOf(
        Result.Failure(Error.NOT_FOUND)
    )

    override fun playlist(playlistUri: Uri) = flowOf(
        Result.Failure(Error.NOT_FOUND)
    )

    override fun audioPlaylistsStatus(audioUri: Uri) = flowOf(
        Result.Failure(Error.NOT_FOUND)
    )

    override fun lyrics(audioUri: Uri) = flowOf(Result.Failure(Error.NOT_FOUND))

    override suspend fun createPlaylist(
        providerIdentifier: ProviderIdentifier,
        name: String,
    ) = Result.Failure(Error.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(
        playlistUri: Uri,
        name: String,
    ) = Result.Failure(Error.NOT_FOUND)

    override suspend fun deletePlaylist(
        playlistUri: Uri,
    ) = Result.Failure(Error.NOT_FOUND)

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri,
    ) = Result.Failure(Error.NOT_FOUND)

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri,
    ) = Result.Failure(Error.NOT_FOUND)

    override suspend fun onAudioPlayed(
        audioUri: Uri,
        positionMs: Long,
    ): MediaRequestStatus<Unit> = Result.Success(Unit)

    override suspend fun setFavorite(
        audioUri: Uri,
        isFavorite: Boolean,
    ) = Result.Failure(Error.NOT_IMPLEMENTED)

    private suspend fun getMimeType(uri: Uri) = withContext(Dispatchers.IO) {
        when (uri.scheme) {
            SCHEME_FILE -> uri.determineFileMimeType()

            SCHEME_CONTENT -> contentResolver.getCompatibleType(uri)

            SCHEME_HTTP, SCHEME_HTTPS -> {
                val request = Request.Builder()
                    .url(uri.toString())
                    .get() // Could use HEAD, but we want to peek the body
                    .build()

                okHttpClient.newCall(request).executeAsync().use { response ->
                    response.getContentType()
                }
            }

            SCHEME_RTSP -> "audio/*" // This is either audio-only or A/V, fine either way

            else -> null
        }
    }

    private fun Uri.determineFileMimeType() = path?.let { path ->
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(
                Uri.fromFile(File(path)).toString()
            )
        ) ?: Files.probeContentType(Paths.get(path))
    }

    private fun ContentResolver.getCompatibleType(
        url: Uri,
    ) = getStreamTypes(url, "*/*")?.firstOrNull {
        MimeUtils.mimeTypeToMediaType(it) == MediaType.AUDIO
    }

    private fun Response.getContentType() = when (isSuccessful) {
        true -> header("Content-Type") ?: body?.let {
            URLConnection.guessContentTypeFromStream(it.byteStream())
        }

        false -> null
    }

    companion object {
        private const val SCHEME_FILE = ContentResolver.SCHEME_FILE
        private const val SCHEME_CONTENT = ContentResolver.SCHEME_CONTENT
        private const val SCHEME_HTTP = "http"
        private const val SCHEME_HTTPS = "https"
        private const val SCHEME_RTSP = "rtsp"

        private val contentQueryProjection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
        )
    }
}
