package com.louis.app.cavity.ui.account

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentLoginBinding
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.navigation.LoginRoute
import com.louis.app.cavity.ui.navigation.NavigationDestination
import com.louis.app.cavity.ui.navigation.navigate
import com.louis.app.cavity.ui.navigation.popBackStack
import com.louis.app.cavity.ui.navigation.popUpTo
import com.louis.app.cavity.ui.widget.Rule
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupToolbar

class FragmentLogin : Fragment(R.layout.fragment_login), NavigationDestination {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModel.Factory }
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override val menuDestinationId = R.id.account_dest

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = requireActivity().onBackPressedDispatcher.addCallback {
            remove()
            popUpTo(R.id.home_dest, inclusive = false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)
        loginViewModel.saveLoginResult(false)
        setupToolbar(binding.appBar.toolbar, R.string.cavity_account)

        (binding.icon.drawable as AnimatedVectorDrawable).start()

        applyInsets()
        observe()
        initFields()
        setListeners()

        loginViewModel.tryConnectWithSavedToken()
    }

    private fun applyInsets() {
        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            WindowInsetsCompat.CONSUMED
        }

        binding.scrollView.prepareWindowInsets { view, _, _, _, _, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observe() {
        loginViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.setVisible(it, invisible = true)
        }

        loginViewModel.navigateToConfirm.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                navigate(LoginRoute.ConfirmAccount)
            }
        }

        loginViewModel.account.observe(viewLifecycleOwner) {
            if (it != null) {
                loginViewModel.saveLoginResult(loginSuccessful = true)
                popBackStack()
            }
        }
    }

    private fun initFields() {
        binding.loginLayout.addRules(Rule(R.string.invalid_email) {
            Patterns.EMAIL_ADDRESS.matcher(it).matches()
        })

        val lastLogin = loginViewModel.getLastLogin()
        binding.login.setText(lastLogin)

        if (lastLogin.isNotBlank()) {
            binding.login.requestFocus()
        }

        binding.passwordLayout.addRules(Rule(R.string.weak_pwd) {
            val passwordMatcher = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$")
            passwordMatcher.find(it) != null
        })
    }

    private fun setListeners() {
        binding.buttonSubmit.setOnClickListener {
            with(binding) {
                val email = login.text.toString()
                val password = password.text.toString()

                when (newAccount.isChecked) {
                    true -> {
                        if (passwordLayout.validate() and loginLayout.validate()) {
                            loginViewModel.register(email, password)
                        }
                    }

                    else -> {
                        loginLayout.error = null
                        passwordLayout.error = null
                        loginViewModel.login(email, password)
                    }
                }
            }
        }

        binding.forgottenPassword.setOnClickListener {
            val resource = SimpleInputDialog.DialogContent(
                R.string.forgotten_password,
                null,
                R.string.email,
                null
            ) { email ->
                loginViewModel.declareLostPassword(email)
            }

            SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner).show(resource)
        }
    }

    override fun onDetach() {
        super.onDetach()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
