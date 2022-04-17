package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentLoginBinding
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentLogin : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        observe()
    }

    private fun observe() {
        accountViewModel.isLoading.observe(viewLifecycleOwner) {
            // Update progress bar
        }

        accountViewModel.userFeedback.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }

        accountViewModel.userFeedbackString.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { string ->
                binding.coordinator.showSnackbar(string)
            }
        }

        accountViewModel.confirmedEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                // redirect to logged in start page
            }
        }

        accountViewModel.isLogged.observe(viewLifecycleOwner) {
            if (it) {
                // redirect to logged in start page
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
