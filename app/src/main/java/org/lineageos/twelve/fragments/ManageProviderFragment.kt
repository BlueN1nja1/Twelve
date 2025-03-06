/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getSerializable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.selectItem
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.FlowResult
import org.lineageos.twelve.models.ProviderArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.validateArgument
import org.lineageos.twelve.models.ProviderIdentifier
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.viewmodels.ManageProviderViewModel

/**
 * Fragment used to add, modify or delete a provider.
 */
class ManageProviderFragment : Fragment(R.layout.fragment_manage_provider) {
    // View models
    private val viewModel by viewModels<ManageProviderViewModel>()

    // Views
    private val argumentsRecyclerView by getViewProperty<RecyclerView>(R.id.argumentsRecyclerView)
    private val confirmMaterialButton by getViewProperty<MaterialButton>(R.id.confirmMaterialButton)
    private val deleteMaterialButton by getViewProperty<MaterialButton>(R.id.deleteMaterialButton)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val providerNameTextInputLayout by getViewProperty<TextInputLayout>(R.id.providerNameTextInputLayout)
    private val providerTypeAutoCompleteTextView by getViewProperty<MaterialAutoCompleteTextView>(R.id.providerTypeAutoCompleteTextView)
    private val providerTypeTextInputLayout by getViewProperty<TextInputLayout>(R.id.providerTypeTextInputLayout)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // Arguments
    private val providerIdentifier: ProviderIdentifier?
        get() = arguments?.getParcelable(ARG_PROVIDER_IDENTIFIER, ProviderIdentifier::class)
    private val providerType: ProviderType?
        get() = arguments?.getSerializable(ARG_PROVIDER_TYPE, ProviderType::class)

    // Providers
    private var selectedProviderType: ProviderType? = null
    private val providerArguments = Bundle()

    // Recyclerview
    private val argumentsAdapter = object : SimpleListAdapter<ProviderArgument<*>, View>(
        argumentsDiffCallback,
        { layoutInflater.inflate(R.layout.argument_item, null) }
    ) {
        // Views
        private val ViewHolder.booleanCheckBox
            get() = view.findViewById<MaterialCheckBox>(R.id.booleanMaterialCheckBox)!!
        private val ViewHolder.stringTextInputLayout
            get() = view.findViewById<TextInputLayout>(R.id.stringTextInputLayout)!!

        override fun ViewHolder.onBindView(item: ProviderArgument<*>) {
            booleanCheckBox.setOnCheckedChangeListener { _, isChecked ->
                providerArguments.putBoolean(item.key, isChecked)
            }
            stringTextInputLayout.editText?.doAfterTextChanged { inputText ->
                inputText.toString().takeIf { it.isNotBlank() }?.also {
                    providerArguments.putString(item.key, it)
                } ?: providerArguments.remove(item.key)
            }

            booleanCheckBox.isVisible = false
            stringTextInputLayout.isVisible = false

            when (item.type) {
                Boolean::class -> {
                    val value = providerArguments.getBoolean(
                        item.key, (item.defaultValue as? Boolean) ?: false
                    )

                    booleanCheckBox.setText(item.nameStringResId)
                    booleanCheckBox.isChecked = value
                    booleanCheckBox.isVisible = true
                }

                String::class -> {
                    val value = providerArguments.getString(item.key)

                    stringTextInputLayout.setHint(item.nameStringResId)
                    stringTextInputLayout.editText?.setText(value)
                    stringTextInputLayout.endIconMode = when (item.hidden) {
                        true -> TextInputLayout.END_ICON_PASSWORD_TOGGLE
                        false -> TextInputLayout.END_ICON_CLEAR_TEXT
                    }
                    stringTextInputLayout.editText?.inputType = when (item.hidden) {
                        true -> android.text.InputType.TYPE_CLASS_TEXT or
                                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

                        false -> android.text.InputType.TYPE_CLASS_TEXT
                    }
                    stringTextInputLayout.isVisible = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setProviderIdentifier(providerIdentifier)

        selectedProviderType = providerType?.also {
            require(manageableProviderTypes.contains(it)) { "Invalid provider type: $it" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setupWithNavController(findNavController())

        argumentsRecyclerView.adapter = argumentsAdapter

        confirmMaterialButton.setOnClickListener {
            val name = providerNameTextInputLayout.editText?.text?.toString()?.takeIf {
                it.isNotBlank()
            }?.also {
                providerNameTextInputLayout.error = null
            } ?: run {
                providerNameTextInputLayout.error = getString(R.string.provider_name_error)
                return@setOnClickListener
            }

            val providerType = selectedProviderType?.also {
                providerTypeTextInputLayout.error = null
            } ?: run {
                providerTypeTextInputLayout.error = getString(R.string.provider_type_error)
                return@setOnClickListener
            }

            val wrongArguments = providerType.arguments.mapNotNull { argument ->
                providerArguments.validateArgument(argument)?.let {
                    argument to it
                }
            }

            if (wrongArguments.isNotEmpty()) {
                showArgumentValidationErrorDialog(wrongArguments)
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                fullscreenLoadingProgressBar.withProgress {
                    if (viewModel.inEditMode.value) {
                        viewModel.updateProvider(name, providerArguments)
                    } else {
                        viewModel.addProvider(providerType, name, providerArguments)
                    }

                    findNavController().navigateUp()
                }
            }
        }

        deleteMaterialButton.setOnClickListener {
            showDeleteDialog()
        }

        providerTypeAutoCompleteTextView.setSimpleItems(
            manageableProviderTypes.map {
                getString(it.nameStringResId)
            }.toTypedArray()
        )

        providerTypeAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setProviderType(manageableProviderTypes[position])
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.inEditMode.collectLatest { inEditMode ->
                        toolbar.setTitle(
                            when (inEditMode) {
                                true -> R.string.manage_provider
                                false -> R.string.add_provider
                            }
                        )

                        confirmMaterialButton.contentDescription = getString(
                            when (inEditMode) {
                                true -> R.string.save_provider_action
                                false -> R.string.add_provider_action
                            }
                        )

                        deleteMaterialButton.isVisible = inEditMode

                        providerTypeTextInputLayout.isEnabled = !inEditMode
                    }
                }

                launch {
                    viewModel.provider.collectLatest {
                        when (it) {
                            is FlowResult.Loading -> {
                                // Do nothing
                            }

                            is FlowResult.Success -> {
                                val provider = it.data

                                providerNameTextInputLayout.editText?.setText(provider.name)
                            }

                            is FlowResult.Error -> {
                                Log.e(LOG_TAG, "Failed to load provider")

                                if (it.error == Error.NOT_FOUND) {
                                    // Get out of here
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.providerTypeWithArguments.collectLatest { providerTypeWithArguments ->
                        val (providerType, providerArguments) = providerTypeWithArguments

                        if (providerType != selectedProviderType) {
                            // Clear the provided arguments regardless since they aren't
                            // valid anymore
                            this@ManageProviderFragment.providerArguments.clear()
                        }

                        providerArguments?.let {
                            // Load the values from the database as defaults
                            this@ManageProviderFragment.providerArguments.clear()
                            this@ManageProviderFragment.providerArguments.putAll(it)
                        }

                        if (providerType != selectedProviderType) {
                            selectedProviderType = providerType

                            providerType?.also {
                                providerTypeAutoCompleteTextView.selectItem(
                                    manageableProviderTypes.indexOf(it)
                                )

                                providerTypeTextInputLayout.setStartIconDrawable(
                                    it.iconDrawableResId
                                )

                                argumentsAdapter.submitList(it.arguments)
                            } ?: run {
                                providerTypeTextInputLayout.startIconDrawable = null

                                argumentsAdapter.submitList(listOf())
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        argumentsRecyclerView.adapter = null

        super.onDestroyView()
    }

    private fun showArgumentValidationErrorDialog(
        wrongArguments: List<Pair<ProviderArgument<*>, ProviderArgument.ValidationError>>
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.argument_validation_error_title)
            .setMessage(
                getString(
                    R.string.argument_validation_error_message,
                    wrongArguments.joinToString(separator = "\n") {
                        getString(
                            R.string.argument_validation_error_item,
                            getString(it.first.nameStringResId),
                            getString(it.second.messageStringResId),
                        )
                    }
                )
            )
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // Do nothing
            }
            .show()
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
        private val LOG_TAG = ManageProviderFragment::class.simpleName!!

        private const val ARG_PROVIDER_IDENTIFIER = "provider_identifier"
        private const val ARG_PROVIDER_TYPE = "provider_type"

        private val argumentsDiffCallback = object : DiffUtil.ItemCallback<ProviderArgument<*>>() {
            override fun areItemsTheSame(
                oldItem: ProviderArgument<*>,
                newItem: ProviderArgument<*>
            ) = oldItem.key == newItem.key && oldItem.type == newItem.type

            override fun areContentsTheSame(
                oldItem: ProviderArgument<*>,
                newItem: ProviderArgument<*>
            ) = false // Reload all items
        }

        private val manageableProviderTypes = ProviderType.entries.filter { it.canBeManaged }

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param providerIdentifier The identifier of the provider to edit or delete
         * @param providerType A [ProviderType] to use as an hint for the creation of a new
         *   instance, ignored when [providerIdentifier] is provided
         */
        fun createBundle(
            providerIdentifier: ProviderIdentifier? = null,
            providerType: ProviderType? = null,
        ) = bundleOf(
            ARG_PROVIDER_IDENTIFIER to providerIdentifier,
            ARG_PROVIDER_TYPE to providerType,
        )
    }
}
