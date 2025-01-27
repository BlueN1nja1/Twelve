/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.preference.PreferenceManager
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import org.lineageos.twelve.MainActivity
import org.lineageos.twelve.R
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.enableFloatOutput
import org.lineageos.twelve.ext.enableOffload
import org.lineageos.twelve.ext.setOffloadEnabled
import org.lineageos.twelve.ext.skipSilence
import org.lineageos.twelve.ext.stopPlaybackOnTaskRemoved
import org.lineageos.twelve.ui.widgets.NowPlayingAppWidgetProvider
import kotlin.reflect.cast

@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService(), LifecycleOwner {
    enum class CustomCommand(val value: String, extras: Bundle) {
        /**
         * Toggles audio offload mode.
         *
         * Arguments:
         * - [ARG_VALUE] ([Boolean]): Whether to enable or disable offload
         */
        TOGGLE_OFFLOAD("toggle_offload", Bundle.EMPTY),

        /**
         * Toggles skip silence.
         *
         * Arguments:
         * - [ARG_VALUE] ([Boolean]): Whether to enable or disable skip silence
         */
        TOGGLE_SKIP_SILENCE("toggle_skip_silence", Bundle.EMPTY),

        /**
         * Get the audio session ID.
         *
         * Response:
         * - [RSP_VALUE] ([Int]): The audio session ID
         */
        GET_AUDIO_SESSION_ID("get_audio_session_id", Bundle.EMPTY);

        val sessionCommand = SessionCommand(value, extras)

        companion object {
            const val ARG_VALUE = "value"
            const val RSP_VALUE = "value"

            fun fromCustomAction(
                customAction: String
            ) = entries.firstOrNull { it.value == customAction }

            suspend fun MediaController.sendCustomCommand(
                customCommand: CustomCommand,
                extras: Bundle
            ) = sendCustomCommand(customCommand.sessionCommand, extras).await()
        }
    }

    private val dispatcher = ServiceLifecycleDispatcher(this)
    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    private val player: ExoPlayer
        get() = mediaLibrarySession?.player as ExoPlayer
    private var mediaLibrarySession: MediaLibrarySession? = null

    private val mediaRepositoryTree by lazy {
        MediaRepositoryTree(
            applicationContext,
            mediaRepository,
        )
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val resumptionPlaylistRepository by lazy {
        (application as TwelveApplication).resumptionPlaylistRepository
    }

    private val mediaRepository by lazy {
        (application as TwelveApplication).mediaRepository
    }

    private val audioSessionId by lazy {
        Util.generateAudioSessionIdV21(this)
    }

    private val mediaLibrarySessionCallback = object : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands =
                MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                    .apply {
                        for (command in CustomCommand.entries) {
                            add(command.sessionCommand)
                        }
                    }
                    .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ) = lifecycleScope.future {
            val resumptionPlaylist = resumptionPlaylistRepository.getResumptionPlaylist()

            var startIndex = resumptionPlaylist.startIndex
            var startPositionMs = resumptionPlaylist.startPositionMs

            val mediaItems = resumptionPlaylist.mediaItemIds.mapIndexed { index, itemId ->
                when (val mediaItem = mediaRepositoryTree.getItem(itemId)) {
                    null -> {
                        if (index == resumptionPlaylist.startIndex) {
                            // The playback position is now invalid
                            startPositionMs = 0

                            // Let's try the next item, this is done automatically since
                            // the next item will take this item's index
                        } else if (index < resumptionPlaylist.startIndex) {
                            // The missing media is before the start index, we have to offset
                            // the start by 1 entry
                            startIndex -= 1
                        }

                        null
                    }

                    else -> mediaItem
                }
            }.filterNotNull()

            // Shouldn't be needed, but just to be sure
            startIndex = startIndex.coerceIn(0, mediaItems.size - 1)

            MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ) = lifecycleScope.future {
            LibraryResult.ofItem(mediaRepositoryTree.rootMediaItem, params)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ) = lifecycleScope.future {
            mediaRepositoryTree.getItem(mediaId)?.let {
                LibraryResult.ofItem(it, null)
            } ?: LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
        }

        @OptIn(UnstableApi::class)
        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ) = lifecycleScope.future {
            LibraryResult.ofItemList(mediaRepositoryTree.getChildren(parentId), params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
        ) = lifecycleScope.future {
            mediaRepositoryTree.resolveMediaItems(mediaItems)
        }

        @OptIn(UnstableApi::class)
        override fun onSetMediaItems(
            mediaSession: MediaSession,
            browser: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
            startIndex: Int,
            startPositionMs: Long,
        ) = lifecycleScope.future {
            val resolvedMediaItems = mediaRepositoryTree.resolveMediaItems(mediaItems)

            launch {
                resumptionPlaylistRepository.onMediaItemsChanged(
                    resolvedMediaItems.map { it.mediaId },
                    startIndex,
                    startPositionMs,
                )
            }

            MediaSession.MediaItemsWithStartPosition(
                resolvedMediaItems,
                startIndex,
                startPositionMs
            )
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?,
        ) = lifecycleScope.future {
            session.notifySearchResultChanged(
                browser, query, mediaRepositoryTree.search(query).size, params
            )
            LibraryResult.ofVoid()
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ) = lifecycleScope.future {
            LibraryResult.ofItemList(mediaRepositoryTree.search(query), params)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ) = lifecycleScope.future {
            when (CustomCommand.fromCustomAction(customCommand.customAction)) {
                CustomCommand.TOGGLE_OFFLOAD -> {
                    args.getBoolean(CustomCommand.ARG_VALUE).let {
                        mediaLibrarySession?.player?.setOffloadEnabled(it)
                    }

                    SessionResult(SessionResult.RESULT_SUCCESS)
                }

                CustomCommand.TOGGLE_SKIP_SILENCE -> {
                    args.getBoolean(CustomCommand.ARG_VALUE).let {
                        ExoPlayer::class.cast(mediaLibrarySession?.player).skipSilenceEnabled = it
                    }

                    SessionResult(SessionResult.RESULT_SUCCESS)
                }

                CustomCommand.GET_AUDIO_SESSION_ID -> {
                    SessionResult(
                        SessionResult.RESULT_SUCCESS,
                        bundleOf(CustomCommand.RSP_VALUE to audioSessionId),
                    )
                }

                null -> SessionResult(SessionError.ERROR_NOT_SUPPORTED)
            }
        }
    }

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setRenderersFactory(
                TwelveRenderersFactory(
                    this,
                    sharedPreferences.enableFloatOutput,
                )
            )
            .setSkipSilenceEnabled(sharedPreferences.skipSilence)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        1000,
                        2000
                    )
                    .build()
            )
            .experimentalSetDynamicSchedulingEnabled(true)
            .build()

        exoPlayer.setOffloadEnabled(sharedPreferences.enableOffload)

        mediaLibrarySession = MediaLibrarySession.Builder(
            this, exoPlayer, mediaLibrarySessionCallback
        )
            .setSessionActivity(getSingleTopActivity())
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .build()
                .apply {
                    setSmallIcon(R.drawable.ic_notification_small_icon)
                }
        )

        exoPlayer.audioSessionId = audioSessionId
        openAudioEffectSession()

        lifecycleScope.launch {
            exoPlayer.listen { events ->
                // Update startIndex and startPositionMs in resumption playlist.
                if (events.containsAny(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    lifecycleScope.launch {
                        resumptionPlaylistRepository.onPlaybackPositionChanged(
                            player.currentMediaItemIndex,
                            player.currentPosition
                        )
                    }

                    lifecycleScope.launch {
                        player.currentMediaItem?.localConfiguration?.uri?.let {
                            mediaRepository.onAudioPlayed(it)
                        }
                    }
                }

                // Update the now playing widget
                if (events.containsAny(
                        Player.EVENT_MEDIA_METADATA_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    )
                ) {
                    lifecycleScope.launch {
                        NowPlayingAppWidgetProvider.update(this@PlaybackService)
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (sharedPreferences.stopPlaybackOnTaskRemoved || !isPlaybackOngoing) {
            lifecycleScope.launch {
                if (isPlaybackOngoing) {
                    resumptionPlaylistRepository.onPlaybackPositionChanged(
                        player.currentMediaItemIndex,
                        player.currentPosition
                    )
                }
                pauseAllPlayersAndStopSelf()
            }
        }
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()

        closeAudioEffectSession()

        mediaLibrarySession?.player?.release()
        mediaLibrarySession?.release()
        mediaLibrarySession = null

        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaLibrarySession

    private fun openAudioEffectSession() {
        Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, application.packageName)
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            sendBroadcast(this)
        }
    }

    private fun closeAudioEffectSession() {
        Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, application.packageName)
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            sendBroadcast(this)
        }
    }

    private fun getSingleTopActivity() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, true)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
