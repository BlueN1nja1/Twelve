/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.selectItem
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.viewmodels.CreatePlaylistViewModel

class CreatePlaylistDialogFragment : MaterialDialogFragment(
    R.layout.fragment_create_playlist_dialog
) {
    // View models
    private val viewModel by viewModels<CreatePlaylistViewModel>()

    // Views
    private val cancelMaterialButton by getViewProperty<MaterialButton>(R.id.cancelMaterialButton)
    private val createMaterialButton by getViewProperty<MaterialButton>(R.id.createMaterialButton)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val playlistNameTextInputLayout by getViewProperty<TextInputLayout>(R.id.playlistNameTextInputLayout)
    private val providerAutoCompleteTextView by getViewProperty<MaterialAutoCompleteTextView>(R.id.providerAutoCompleteTextView)
    private val providerTextInputLayout by getViewProperty<TextInputLayout>(R.id.providerTextInputLayout)

    // Arguments
    private val providerIdentifier: ProviderIdentifier?
        get() = arguments?.getParcelable(ARG_PROVIDER_IDENTIFIER, ProviderIdentifier::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setProviderIdentifier(providerIdentifier)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providerAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setProviderPosition(position)
        }

        playlistNameTextInputLayout.editText!!.apply {
            setText(viewModel.getPlaylistName())
            doOnTextChanged { text, _, _, _ ->
                playlistNameTextInputLayout.error = null
                viewModel.setPlaylistName(text?.toString() ?: "")
            }
        }

        cancelMaterialButton.setOnClickListener {
            findNavController().navigateUp()
        }

        createMaterialButton.setOnClickListener {
            if (viewModel.isPlaylistNameEmpty()) {
                playlistNameTextInputLayout.error = getString(
                    R.string.create_playlist_error_empty_name
                )
                return@setOnClickListener
            }

            playlistNameTextInputLayout.error = null

            viewLifecycleOwner.lifecycleScope.launch {
                fullscreenLoadingProgressBar.withProgress {
                    viewModel.createPlaylist()
                    findNavController().navigateUp()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadData()
            }
        }
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.providersWithSelection.collectLatest { providersWithSelection ->
                val (providers, position) = providersWithSelection

                providerAutoCompleteTextView.setSimpleItems(
                    providers.map { provider ->
                        getString(
                            R.string.provider_format,
                            provider.name,
                            getString(provider.type.nameStringResId),
                        )
                    }.toTypedArray()
                )

                position?.also {
                    val provider = providers[it]

                    providerAutoCompleteTextView.selectItem(it)
                    providerTextInputLayout.setStartIconDrawable(
                        provider.type.iconDrawableResId
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_PROVIDER_IDENTIFIER = "provider_identifier"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param providerIdentifier A [ProviderIdentifier] to pre-fill the provider field
         */
        fun createBundle(
            providerIdentifier: ProviderIdentifier? = null,
        ) = bundleOf(
            ARG_PROVIDER_IDENTIFIER to providerIdentifier,
        )
    }
}
