/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.annotation.Px
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.lineageos.twelve.ext.ENABLE_OFFLOAD_KEY
import org.lineageos.twelve.ext.SKIP_SILENCE_KEY
import org.lineageos.twelve.ext.setOffset
import org.lineageos.twelve.viewmodels.SettingsViewModel
import kotlin.reflect.safeCast

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    private val appBarLayout by lazy { findViewById<AppBarLayout>(R.id.appBarLayout) }
    private val coordinatorLayout by lazy { findViewById<CoordinatorLayout>(R.id.coordinatorLayout) }
    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.toolbar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, RootSettingsFragment())
                .commit()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    abstract class SettingsFragment(
        @XmlRes private val preferencesResId: Int,
    ) : PreferenceFragmentCompat() {
        // View model
        protected val viewModel by viewModels<SettingsViewModel>()

        private val settingsActivity
            get() = SettingsActivity::class.safeCast(activity)

        @Px
        private var appBarOffset = -1

        private val offsetChangedListener = AppBarLayout.OnOffsetChangedListener { _, i ->
            appBarOffset = -i
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            settingsActivity?.let { settingsActivity ->
                val appBarLayout = settingsActivity.appBarLayout

                if (appBarOffset != -1) {
                    appBarLayout.setOffset(appBarOffset, settingsActivity.coordinatorLayout)
                } else {
                    appBarLayout.setExpanded(true, false)
                }

                appBarLayout.setLiftOnScrollTargetView(listView)

                appBarLayout.addOnOffsetChangedListener(offsetChangedListener)
            }
        }

        override fun onDestroyView() {
            settingsActivity?.appBarLayout?.apply {
                removeOnOffsetChangedListener(offsetChangedListener)

                setLiftOnScrollTargetView(null)
            }

            super.onDestroyView()
        }

        @CallSuper
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(preferencesResId, rootKey)
        }

        @CallSuper
        override fun onCreateRecyclerView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            savedInstanceState: Bundle?
        ) = super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            clipToPadding = false
            isVerticalScrollBarEnabled = false

            ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                updatePadding(
                    bottom = insets.bottom,
                    left = insets.left,
                    right = insets.right,
                )

                windowInsets
            }
        }
    }

    class RootSettingsFragment : SettingsFragment(R.xml.root_preferences) {
        // Preferences
        private val enableOffload by lazy { findPreference<SwitchPreference>(ENABLE_OFFLOAD_KEY)!! }
        private val resetLocalStats by lazy { findPreference<Preference>("reset_local_stats")!! }
        private val skipSilence by lazy { findPreference<SwitchPreference>(SKIP_SILENCE_KEY)!! }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            super.onCreatePreferences(savedInstanceState, rootKey)

            enableOffload.setOnPreferenceChangeListener { _, newValue ->
                lifecycleScope.launch {
                    viewModel.toggleOffload(newValue as Boolean)
                }
                true
            }

            skipSilence.setOnPreferenceChangeListener { _, newValue ->
                lifecycleScope.launch {
                    viewModel.toggleSkipSilence(newValue as Boolean)
                }
                true
            }

            resetLocalStats.setOnPreferenceClickListener {
                showResetLocalStatsDialog()
                true
            }
        }

        private fun showResetLocalStatsDialog() {
            val context = requireActivity()
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.reset_local_stats_confirm_title)
                .setMessage(R.string.reset_local_stats_confirm_message)
                .setPositiveButton(R.string.reset_local_stats_confirm_positive) { _, _ ->
                    lifecycleScope.launch {
                        viewModel.resetLocalStats()

                        Toast.makeText(
                            context,
                            R.string.reset_local_stats_success,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> /* Do nothing */ }
                .show()
        }
    }
}
