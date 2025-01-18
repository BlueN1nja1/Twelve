/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDivider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getSerializable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.navigateSafe
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.RequestStatus.Companion.fold
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.viewmodels.ProviderViewModel

/**
 * Fragment used to show useful information regarding a provider.
 */
class ProviderInformationBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_provider_information_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<ProviderViewModel>()

    // Views
    private val deleteProviderMaterialButton by getViewProperty<MaterialButton>(R.id.deleteProviderMaterialButton)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val manageButtonsHorizontalScrollView by getViewProperty<HorizontalScrollView>(R.id.manageButtonsHorizontalScrollView)
    private val manageProviderMaterialButton by getViewProperty<MaterialButton>(R.id.manageProviderMaterialButton)
    private val providerIconImageView by getViewProperty<ImageView>(R.id.providerIconImageView)
    private val providerTypeTextView by getViewProperty<TextView>(R.id.providerTypeTextView)
    private val statusMaterialDivider by getViewProperty<MaterialDivider>(R.id.statusMaterialDivider)
    private val statusRecyclerView by getViewProperty<RecyclerView>(R.id.statusRecyclerView)
    private val titleTextView by getViewProperty<TextView>(R.id.titleTextView)

    // RecyclerView
    private val statusAdapter by lazy {
        object : SimpleListAdapter<DataSourceInformation, ListItem>(
            UniqueItemDiffCallback(),
            { context -> ListItem(context) }
        ) {
            override fun ViewHolder.onBindView(item: DataSourceInformation) {
                view.headlineText = item.keyLocalizedString.getString(view.context)
                view.supportingText = item.value.getString(view.context)
            }
        }
    }

    // Arguments
    private val providerType: ProviderType
        get() = requireArguments().getSerializable(ARG_PROVIDER_TYPE, ProviderType::class)!!
    private val providerTypeId: Long
        get() = requireArguments().getLong(ARG_PROVIDER_TYPE_ID, -1L).takeIf {
            it != -1L
        }!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manageProviderMaterialButton.setOnClickListener {
            viewModel.provider.value.fold(
                onLoading = {},
                onSuccess = {
                    findNavController().navigateSafe(
                        R.id.action_providerInformationBottomSheetDialogFragment_to_fragment_manage_provider,
                        ManageProviderFragment.createBundle(providerType, providerTypeId),
                    )
                },
                onError = {},
            )
        }

        deleteProviderMaterialButton.setOnClickListener {
            showDeleteDialog()
        }

        statusRecyclerView.adapter = statusAdapter

        viewModel.setProviderIds(providerType to providerTypeId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadData()
            }
        }
    }

    override fun onDestroyView() {
        statusRecyclerView.adapter = null

        super.onDestroyView()
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.provider.collectLatest {
                when (it) {
                    is RequestStatus.Loading -> {
                        // Do nothing
                    }

                    is RequestStatus.Success -> {
                        val provider = it.data

                        titleTextView.text = provider.name
                        providerTypeTextView.setText(provider.type.nameStringResId)
                        providerIconImageView.setImageResource(provider.type.iconDrawableResId)
                    }

                    is RequestStatus.Error -> {
                        Log.e(LOG_TAG, "Failed to load data, error: ${it.error}", it.throwable)

                        titleTextView.text = ""
                        providerTypeTextView.text = ""
                        providerIconImageView.setImageResource(R.drawable.ic_warning)
                    }
                }
            }
        }

        launch {
            viewModel.canBeManaged.collectLatest {
                manageButtonsHorizontalScrollView.isVisible = it
            }
        }

        launch {
            viewModel.status.collectLatest {
                when (it) {
                    is RequestStatus.Loading -> {
                        // Do nothing
                    }

                    is RequestStatus.Success -> {
                        val data = it.data

                        statusAdapter.submitList(data)

                        val isEmpty = data.isEmpty()

                        statusRecyclerView.isVisible = !isEmpty
                        statusMaterialDivider.isVisible = !isEmpty
                    }

                    is RequestStatus.Error -> {
                        Log.e(LOG_TAG, "Failed to load data, error: ${it.error}", it.throwable)

                        statusAdapter.submitList(emptyList())

                        statusRecyclerView.isVisible = false
                        statusMaterialDivider.isVisible = false
                    }
                }
            }
        }
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_provider_confirmation)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    fullscreenLoadingProgressBar.withProgress {
                        viewModel.deleteProvider()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                // Do nothing
            }
            .show()
    }

    companion object {
        private val LOG_TAG = ProviderInformationBottomSheetDialogFragment::class.simpleName!!

        private const val ARG_PROVIDER_TYPE = "provider_type"
        private const val ARG_PROVIDER_TYPE_ID = "provider_type_id"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param providerType The [ProviderType] of the provider to manage
         * @param providerTypeId The type specific ID of the provider to manage
         */
        fun createBundle(
            providerType: ProviderType,
            providerTypeId: Long,
        ) = bundleOf(
            ARG_PROVIDER_TYPE to providerType,
            ARG_PROVIDER_TYPE_ID to providerTypeId,
        )
    }
}
