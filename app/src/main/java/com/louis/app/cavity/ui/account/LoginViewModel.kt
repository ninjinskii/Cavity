package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.Environment
import com.louis.app.cavity.domain.repository.AccountRepository
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.network.response.LoginResponse
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.util.save
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginEvent {
    data class UserFeedback(@StringRes val resId: Int) : LoginEvent
    data class UserFeedbackString(val message: String) : LoginEvent
    data object NavigateToConfirm : LoginEvent
    data object Confirmed : LoginEvent
    data object Deleted : LoginEvent
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val account: LoginResponse? = null
)

class LoginViewModel(
    app: Application,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<LoginUiState, LoginEvent>(app, LoginUiState()) {

    private val prefsRepository = PrefsRepository.getInstance(app)
    private val accountRepository = AccountRepository.getInstance(app)
    private val errorReporter = SentryErrorReporter.getInstance(app)

    private var savedLoginResult: Boolean? by savedStateHandle save "login-result"
    private var inConfirmationUser: String? = null
    private var sneakyTryCount = 0

    fun login(email: String, password: String) {
        inConfirmationUser = email

        doApiCall(
            call = { accountRepository.login(email, password) },
            onSuccess = {
                prefsRepository.setApiToken(it.value.token)
                prefsRepository.setLastLogin(email)
                errorReporter.setScopeTag(ErrorReporter.USERNAME_ERROR_TAG, email)
                viewState = viewState.copy(account = it.value)
            }
        )
    }

    fun register(email: String, password: String) {
        doApiCall(
            call = { accountRepository.register(email, password) },
            onSuccess = {
                emitEvent(LoginEvent.UserFeedback(R.string.confirm_mail_sent))
                inConfirmationUser = email
                emitEvent(LoginEvent.NavigateToConfirm)
            }
        )
    }

    fun confirmAccount(registrationCode: String) {
        val email = inConfirmationUser

        if (email == null) {
            emitEvent(LoginEvent.UserFeedback(R.string.base_error))
            return
        }

        doApiCall(
            call = { accountRepository.confirmAccount(email, registrationCode) },
            onSuccess = {
                prefsRepository.setLastLogin(inConfirmationUser ?: "")
                inConfirmationUser = null
                prefsRepository.setApiToken(it.value.token)
                emitEvent(LoginEvent.Confirmed)
                viewState = viewState.copy(account = it.value)
            }
        )
    }

    fun tryConnectWithSavedToken() {
        val token = prefsRepository.getApiToken()

        if (token.isBlank() || sneakyTryCount >= 1) {
            return
        }

        sneakyTryCount++

        viewModelScope.launch(IO) {
            val response = accountRepository.getAccount()

            if (response is ApiResponse.Success) {
                val email = response.value.email
                errorReporter.setScopeTag(ErrorReporter.USERNAME_ERROR_TAG, email)
                prefsRepository.setLastLogin(email)
                viewState = viewState.copy(account = response.value)
            }
        }
    }

    fun getLastLogin() = prefsRepository.getLastLogin()

    fun logout() {
        viewState = viewState.copy(account = null)
        prefsRepository.setApiToken("")
        errorReporter.removeScopeTag(ErrorReporter.USERNAME_ERROR_TAG)
    }

    fun declareLostPassword(email: String) {
        doApiCall(
            call = { accountRepository.recoverPassword(email.trim()) },
            onSuccess = { emitEvent(LoginEvent.UserFeedback(R.string.reset_ok)) }
        )
    }

    fun deleteAccount(password: String) {
        viewState.account?.let { account ->
            doApiCall(
                call = { accountRepository.deleteAccount(account.email, password) },
                onSuccess = {
                    inConfirmationUser = null
                    prefsRepository.setLastLogin("")
                    prefsRepository.setApiToken("")
                    errorReporter.removeScopeTag(ErrorReporter.USERNAME_ERROR_TAG)
                    emitEvent(LoginEvent.Deleted)
                }
            )
        }
    }

    fun updateAccountLastUpdateLocally() {
        val deviceName = Environment.getDeviceName()
        val copy = viewState.account?.copy(
            lastUpdateTime = System.currentTimeMillis(),
            lastUser = deviceName
        )
        copy?.let { viewState = viewState.copy(account = it) }
    }

    fun saveLoginResult(loginSuccessful: Boolean) {
        savedLoginResult = loginSuccessful
    }

    fun loginResultFlow(): Flow<Boolean?> {
        return savedStateHandle.getStateFlow<Boolean?>("login-result", null)
    }

    private fun <T> doApiCall(
        call: suspend () -> ApiResponse<T>,
        onSuccess: (ApiResponse.Success<T>) -> Unit
    ) {
        if (viewState.isLoading) {
            return
        }

        viewState = viewState.copy(isLoading = true)

        viewModelScope.launch(IO) {
            try {
                when (val response = call()) {
                    is ApiResponse.Success -> onSuccess(response)
                    is ApiResponse.Failure -> emitEvent(LoginEvent.UserFeedbackString(response.message))
                    is ApiResponse.UnknownError -> emitEvent(LoginEvent.UserFeedback(R.string.base_error))
                    is ApiResponse.UnregisteredError -> emitEvent(LoginEvent.NavigateToConfirm)
                    is ApiResponse.UnauthorizedError -> Unit
                }
            } finally {
                viewState = viewState.copy(isLoading = false)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                LoginViewModel(app, createSavedStateHandle())
            }
        }
    }
}

