package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentConfirmAccountBinding
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation

class FragmentConfirmAccount : Fragment(R.layout.fragment_confirm_account) {
    private var _binding: FragmentConfirmAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConfirmAccountBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        observe()
        setListeners()
    }

    private fun observe() {
        loginViewModel.confirmedEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                val action = FragmentConfirmAccountDirections.confirmToAccount()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.fragmentLogin, true)
                    .build()

                findNavController().navigate(action, navOptions)
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
                loginViewModel.confirmAccount(loadConfimrationCode())
            }
        }

        binding.buttonSubmit.setOnClickListener {
            loginViewModel.confirmAccount(loadConfimrationCode())
        }
    }

    private fun loadConfimrationCode(): String {
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
