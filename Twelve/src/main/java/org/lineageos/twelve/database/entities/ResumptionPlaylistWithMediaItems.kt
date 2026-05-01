/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ResumptionPlaylistWithMediaItems(
    @Embedded val resumptionPlaylist: ResumptionPlaylist,
    @Relation(
        parentColumn = "resumption_id",
        entityColumn = "resumption_playlist_id",
    ) val items: List<ResumptionItem>,
)
