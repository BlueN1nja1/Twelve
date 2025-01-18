/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.navigateSafe
import org.lineageos.twelve.models.Provider
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.viewmodels.ProvidersViewModel

/**
 * Fragment used to select a media provider.
 */
class ProviderSelectorDialogFragment : DialogFragment(R.layout.fragment_provider_selector_dialog) {
    // View models
    private val viewModel by viewModels<ProvidersViewModel>()

    // Views
    private val addProviderMaterialButton by getViewProperty<MaterialButton>(R.id.addProviderMaterialButton)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)

    // Recyclerview
    private val adapter = object : SimpleListAdapter<Provider, ListItem>(
        UniqueItemDiffCallback(),
        ::ListItem,
    ) {
        override fun ViewHolder.onPrepareView() {
            view.setOnClickListener {
                item?.let {
                    viewModel.setNavigationProvider(it)
                    findNavController().navigateUp()
                }
            }

            view.setOnLongClickListener {
                item?.let {
                    findNavController().navigateSafe(
                        R.id.action_providerSelectorDialogFragment_to_fragment_provider_information_bottom_sheet_dialog,
                        ManageProviderFragment.createBundle(it.type, it.typeId),
                        NavOptions.Builder()
                            .setPopUpTo(R.id.mainFragment, false)
                            .build(),
                    )
                }

                true
            }
        }

        override fun ViewHolder.onBindView(item: Provider) {
            view.setLeadingIconImage(item.type.iconDrawableResId)
            view.headlineText = item.name
            view.setSupportingText(item.type.nameStringResId)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(
        requireContext()
    ).show()!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter

        addProviderMaterialButton.setOnClickListener {
            findNavController().navigateSafe(
                R.id.action_providerSelectorDialogFragment_to_fragment_manage_provider,
                ManageProviderFragment.createBundle()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providers.collect {
                    when (it) {
                        is RequestStatus.Loading -> {
                            // Do nothing
                        }

                        is RequestStatus.Success -> {
                            adapter.submitList(it.data)
                        }

                        is RequestStatus.Error -> throw Exception(
                            "Error while loading providers"
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }
}
