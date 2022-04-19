package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentConfirmAccountBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentConfirmAccount : Fragment(R.layout.fragment_confirm_account) {
    private var _binding: FragmentConfirmAccountBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConfirmAccountBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        observe()
        setListeners()
    }

    private fun observe() {
        accountViewModel.confirmedEvent.observe(viewLifecycleOwner) {
            val action = FragmentConfirmAccountDirections.cofirmToAccount()
            findNavController().navigate(action)
        }
    }

    private fun setListeners() {
        with(binding) {
            val inputs = listOf(digit1, digit2, digit3, digit4, digit5, digit6)
            inputs.forEachIndexed { index, input ->
                input.doAfterTextChanged {
                    try {
                        inputs[index + 1].requestFocus()
                    } catch (e: IndexOutOfBoundsException) {
                    }
                }
            }
        }

        binding.digit6.doAfterTextChanged {
            accountViewModel.confirmAccount("louiszimbabwe@gmail.com", loadInput())
        }
    }

    private fun loadInput(): String {
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
