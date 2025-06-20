/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * An activity that hosts the [SettingsFragment].
 *
 * This activity's primary role is to display the app's settings fragment
 * and provide a toolbar for navigation.
 */
class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.toolbar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Set up the toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // Display the SettingsFragment if this is the first time the activity is created
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Handle the "up" button in the toolbar
        android.R.id.home -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
