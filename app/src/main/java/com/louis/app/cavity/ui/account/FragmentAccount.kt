package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAccountBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentAccount : Fragment(R.layout.fragment_account) {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle

        savedStateHandle.getLiveData<Boolean>(FragmentLogin.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) {
                if (!it) {
                    val startDestination = findNavController().graph.startDestination
                    val action = FragmentAccountDirections.accountToHome()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()

                    findNavController().navigate(action, navOptions)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)

        setupNavigation(binding.toolbar)

        observe()
        setupToolbar()
    }

    private fun observe() {
        loginViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.email.text = it
            } else {
                val action = FragmentAccountDirections.accountToLogin()
                findNavController().navigate(action)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.logout) {
                loginViewModel.logout()
                return@setOnMenuItemClickListener true
            }

            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
