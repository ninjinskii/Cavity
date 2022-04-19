package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentLoginBinding
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentLogin : Fragment(R.layout.fragment_login) {
    companion object {
        const val LOGIN_SUCCESSFUL: String = "com.louis.app.cavity.LOGIN_SUCCESSFUL"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by activityViewModels()
    private lateinit var savedStateHandle: SavedStateHandle


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)
        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle.set(LOGIN_SUCCESSFUL, false)

        setupNavigation(binding.appBar.toolbar)

        observe()
        setListeners()
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

        accountViewModel.navigateToConfirm.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                val action = FragmentLoginDirections.loginToConfirm()
                findNavController().navigate(action)
            }
        }

        accountViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                savedStateHandle.set(LOGIN_SUCCESSFUL, true)
                findNavController().popBackStack()
            }
        }
    }

    private fun setListeners() {
        binding.buttonSubmit.setOnClickListener {
            with(binding) {
                val email = login.text.toString()
                val password = password.text.toString()
                val apiUrl = "http://${ip.text.toString()}"

                accountViewModel.submitIp(apiUrl)

                when (newAccount.isChecked) {
                    true -> accountViewModel.register(email, password)
                    else -> accountViewModel.login(email, password)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
