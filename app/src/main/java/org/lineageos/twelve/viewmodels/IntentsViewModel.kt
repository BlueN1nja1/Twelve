/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.MainActivity
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.asArray
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.utils.MimeUtils

/**
 * A view model used by activities to handle intents.
 */
class IntentsViewModel(application: Application) : AndroidViewModel(application) {
    data class ParsedIntent(
        val action: Action,
        val contents: List<Content> = listOf(),
    ) {
        enum class Action {
            /**
             * Open the app's home page.
             * No [contents] is required.
             */
            MAIN,

            /**
             * Open the now playing fragment.
             * No [contents] is required.
             */
            OPEN_NOW_PLAYING,

            /**
             * View a content.
             * [contents] must contain at least one element.
             */
            VIEW,
        }

        data class Content(
            val uri: Uri,
            val type: MediaType,
        )

        private var handled = false

        suspend fun handle(
            consumer: suspend (parsedIntent: ParsedIntent) -> Unit,
        ) = when (handled) {
            true -> false
            false -> {
                consumer(this)
                handled = true
                true
            }
        }
    }

    private val mediaRepository = getApplication<TwelveApplication>().mediaRepository

    private val currentIntent = MutableStateFlow<Intent?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val parsedIntent = currentIntent
        .mapLatest { currentIntent ->
            val intent = currentIntent ?: run {
                Log.i(LOG_TAG, "No intent")
                return@mapLatest null
            }

            val action = when (intent.action) {
                null,
                Intent.ACTION_MAIN -> when (
                    intent.getBooleanExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, false)
                ) {
                    true -> ParsedIntent.Action.OPEN_NOW_PLAYING
                    false -> ParsedIntent.Action.MAIN
                }

                Intent.ACTION_VIEW,
                MediaStore.ACTION_REVIEW,
                MediaStore.ACTION_REVIEW_SECURE -> ParsedIntent.Action.VIEW

                else -> run {
                    Log.e(LOG_TAG, "Unknown intent action ${intent.action}")
                    return@mapLatest null
                }
            }

            val contents = buildList {
                intent.data?.let { data ->
                    uriToContent(
                        data,
                        intent.type?.let { MimeUtils.mimeTypeToMediaType(it) }
                    )?.let {
                        add(it)
                    }
                }

                intent.clipData?.let { clipData ->
                    // Do a best effort to get a valid media type from the clip data
                    var mediaType: MediaType? = null
                    for (i in 0 until clipData.description.mimeTypeCount) {
                        val mimeType = clipData.description.getMimeType(i)
                        MimeUtils.mimeTypeToMediaType(mimeType)?.let { type ->
                            mediaType = type
                        }
                    }

                    clipData.asArray().forEach { item ->
                        uriToContent(item.uri, mediaType)?.let {
                            add(it)
                        }
                    }
                }
            }

            ParsedIntent(
                action = action,
                contents = contents,
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null,
        )

    fun onIntent(intent: Intent?) {
        currentIntent.value = intent
    }

    /**
     * Given a URI and a pre-parsed media type, get a [ParsedIntent.Content] object.
     */
    private suspend fun uriToContent(uri: Uri, mediaType: MediaType?): ParsedIntent.Content? {
        val type = mediaType ?: uriToType(uri) ?: run {
            Log.e(LOG_TAG, "Cannot get media type of $uri")
            return null
        }

        return ParsedIntent.Content(uri, type)
    }

    /**
     * Run the URI over the available data sources and check if one of them understands it.
     * Get the media type of the URI if found.
     */
    private suspend fun uriToType(uri: Uri) = mediaRepository.mediaTypeOf(uri) ?: run {
        Log.e(LOG_TAG, "Cannot get media type of $uri")
        null
    }

    companion object {
        private val LOG_TAG = IntentsViewModel::class.simpleName!!
    }
}
