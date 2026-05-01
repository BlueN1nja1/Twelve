/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.coil

import coil3.map.Mapper
import coil3.request.Options
import org.lineageos.twelve.models.Thumbnail

object ThumbnailMapper : Mapper<Thumbnail, Any> {
    override fun map(data: Thumbnail, options: Options) = data.bitmap ?: data.uri
}
