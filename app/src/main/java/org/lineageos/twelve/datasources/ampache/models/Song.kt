/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Song.
 *
 * @param id The ID
 * @param title The title
 * @param name The name
 * @param artist The artist
 * @param artists The artists
 * @param album The album
 * @param albumArtist The album artist
 * @param disk The disk
 * @param diskSubtitle The disk subtitle
 * @param track The track
 * @param filename The filename
 * @param genre The genre
 * @param playlistTrack The playlist track
 * @param time The time
 * @param year The year
 * @param format The format
 * @param streamFormat The stream format
 * @param bitrate The bitrate
 * @param streamBitrate The stream bitrate
 * @param rate The rate
 * @param mode The mode
 * @param mime The mime
 * @param streamMime The stream mime
 * @param url The URL
 * @param size The size
 * @param mbid MusicBrainz Identifier
 * @param art The artwork URL
 * @param hasArt Whether this song has an artwork available
 * @param flag The flag
 * @param rating The rating
 * @param averageRating The average rating
 * @param playCount The play count
 * @param catalog The catalog
 * @param composer The composer
 * @param channels The channels
 * @param comment The comment
 * @param license The license
 * @param publisher The publisher
 * @param language The language
 * @param lyrics The lyrics
 * @param replayGainAlbumGain The replay gain album gain
 * @param replayGainAlbumPeak The replay gain album peak
 * @param replayGainTrackGain The replay gain track gain
 * @param replayGainTrackPeak The replay gain track peak
 * @param r128AlbumGain The r128 album gain
 * @param r128TrackGain The r128 track gain
 */
@Serializable
data class Song(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("name") val name: String,
    @SerialName("artist") val artist: Artist,
    @SerialName("artists") val artists: List<Artist>,
    @SerialName("album") val album: Album,
    @SerialName("albumartist") val albumArtist: Artist,
    @SerialName("disk") val disk: Int? = null,
    @SerialName("disksubtitle") val diskSubtitle: String? = null,
    @SerialName("track") val track: Int?,
    @SerialName("filename") val filename: String,
    @SerialName("genre") val genre: List<Genre>,
    @SerialName("playlisttrack") val playlistTrack: Int?,
    @SerialName("time") val time: Int,
    @SerialName("year") val year: Int,
    @SerialName("format") val format: String,
    @SerialName("stream_format") val streamFormat: String,
    @SerialName("bitrate") val bitrate: Int,
    @SerialName("stream_bitrate") val streamBitrate: Int,
    @SerialName("rate") val rate: Int?,
    @SerialName("mode") val mode: String?,
    @SerialName("mime") val mime: String,
    @SerialName("stream_mime") val streamMime: String,
    @SerialName("url") val url: String,
    @SerialName("size") val size: Int,
    @SerialName("mbid") val mbid: String? = null,
    @SerialName("art") val art: String,
    @SerialName("has_art") val hasArt: Boolean,
    @SerialName("flag") val flag: Boolean,
    @SerialName("rating") val rating: Int?,
    @SerialName("averagerating") val averageRating: Double? = null,
    @SerialName("playcount") val playCount: Int,
    @SerialName("catalog") val catalog: Int? = null,
    @SerialName("composer") val composer: String? = null,
    @SerialName("channels") val channels: Int? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("license") val license: String? = null,
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("language") val language: String?,
    @SerialName("lyrics") val lyrics: String?,
    @SerialName("replaygain_album_gain") val replayGainAlbumGain: Double?,
    @SerialName("replaygain_album_peak") val replayGainAlbumPeak: Double?,
    @SerialName("replaygain_track_gain") val replayGainTrackGain: Double?,
    @SerialName("replaygain_track_peak") val replayGainTrackPeak: Double?,
    @SerialName("r128_album_gain") val r128AlbumGain: Double?,
    @SerialName("r128_track_gain") val r128TrackGain: Double?,
)
