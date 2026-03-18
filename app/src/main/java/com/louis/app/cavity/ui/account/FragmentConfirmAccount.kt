package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentConfirmAccountBinding
import com.louis.app.cavity.ui.navigation.NavigationDestination
import com.louis.app.cavity.ui.navigation.popBackStack
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation

class FragmentConfirmAccount : Fragment(R.layout.fragment_confirm_account), NavigationDestination {
    private var _binding: FragmentConfirmAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModel.Factory }

    override val menuDestinationId = R.id.account_dest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConfirmAccountBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        applyInsets()
        observe()
        setListeners()
    }

    private fun applyInsets() {
        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observe() {
        loginViewModel.confirmedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                popBackStack()
            }
        }

        loginViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.setVisible(it, invisible = true)
        }
    }

    private fun setListeners() {
        with(binding) {
            val inputs = listOf(digit1, digit2, digit3, digit4, digit5, digit6)
            for (i in 0 until 5) {
                inputs[i].doAfterTextChanged {
                    if (it.toString().isNotEmpty()) {
                        inputs[i + 1].requestFocus()
                    }
                }
            }
        }

        binding.digit6.doAfterTextChanged {
            if (it.toString().isNotEmpty()) {
                loginViewModel.confirmAccount(loadConfirmationCode())
            }
        }

        binding.buttonSubmit.setOnClickListener {
            loginViewModel.confirmAccount(loadConfirmationCode())
        }
    }

    private fun loadConfirmationCode(): String {
        with(binding) {
            val inputs = listOf(digit1, digit2, digit3, digit4, digit5, digit6)
            var code = ""

            for (input in inputs) {
                code += input.text.toString()
            }

            return code
        }
    }
}
