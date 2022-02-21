package com.louis.app.cavity.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentSettingsBinding
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentSettings : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        observe()
        setListeners()
        setupCurrencyButtons()
    }

    private fun observe() {
        settingsViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        settingsViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.setVisible(it)
        }
    }

    private fun setListeners() {
        binding.toggleSkew.apply {
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            isChecked = settingsViewModel.getSkewBottle()

            setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setSkewBottle(isChecked)
                clickCount++
            }

            setOnLongClickListener {
                if (clickCount == 10) {
                    val externalDir = requireContext().getExternalFilesDir(null)!!.path
                    settingsViewModel.importDbFromExternalDir(externalDir)
                }

                true
            }
        }

        binding.skewBottle.apply {
            setOnClickListener {
                binding.toggleSkew.toggle()
            }

            setOnLongClickListener {
                if (clickCount == 10) {
                    val externalDir = requireContext().getExternalFilesDir(null)!!.path
                    settingsViewModel.importDbFromExternalDir(externalDir)
                }

                true
            }
        }
    }

    private fun setupCurrencyButtons() {
        val currencies = resources.getStringArray(R.array.currencies)
        val defaultCurrency = settingsViewModel.getDefaultCurrency()
        val defaultIndex = currencies.indexOf(defaultCurrency)

        binding.rbGroupCurrency.children.forEachIndexed { index, view ->
            val button = view as MaterialButton

            if (index == defaultIndex) {
                button.isChecked = true
            }

            button.text = currencies[index]
        }
    }

    override fun onPause() {
        super.onPause()

        val checkedButtonId = binding.rbGroupCurrency.checkedButtonId
        val checkedButton = binding.rbGroupCurrency.findViewById<Button>(checkedButtonId)

        settingsViewModel.setDefaultCurrency(checkedButton.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
