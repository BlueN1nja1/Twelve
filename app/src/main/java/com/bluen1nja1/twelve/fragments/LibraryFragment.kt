/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.bluen1nja1.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.bluen1nja1.twelve.R
import com.bluen1nja1.twelve.ext.getViewProperty

/**
 * Music library.
 */
class LibraryFragment : Fragment(R.layout.fragment_library) {
    // Views
    private val tabLayout by getViewProperty<TabLayout>(R.id.tabLayout)
    private val viewPager2 by getViewProperty<ViewPager2>(R.id.viewPager2)

    // ViewPager2
    private enum class Menus(
        @StringRes val titleStringResId: Int,
        @DrawableRes val iconDrawableResId: Int,
        val fragment: () -> Fragment,
    ) {
        ALBUMS(
            R.string.library_fragment_menu_albums,
            R.drawable.ic_album,
            { AlbumsFragment() },
        ),
        ARTISTS(
            R.string.library_fragment_menu_artists,
            R.drawable.ic_person,
            { ArtistsFragment() },
        ),
        GENRES(
            R.string.library_fragment_menu_genres,
            R.drawable.ic_genres,
            { GenresFragment() },
        ),
        PLAYLISTS(
            R.string.library_fragment_menu_playlists,
            R.drawable.ic_playlist_play,
            { PlaylistsFragment() },
        ),
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager2.adapter = object : FragmentStateAdapter(
            childFragmentManager, viewLifecycleOwner.lifecycle
        ) {
            override fun getItemCount() = Menus.entries.size
            override fun createFragment(position: Int) = Menus.entries[position].fragment()
        }
        viewPager2.offscreenPageLimit = Menus.entries.size

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            val menu = Menus.entries[position]

            tab.setText(menu.titleStringResId)
            tab.setContentDescription(menu.titleStringResId)
            tab.setIcon(menu.iconDrawableResId)
        }.attach()
    }

    override fun onDestroyView() {
        viewPager2.adapter = null

        super.onDestroyView()
    }
}
