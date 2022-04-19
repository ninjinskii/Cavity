package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.PrefsRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AccountViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val prefsRepository = PrefsRepository.getInstance(app)
    private val accountRepository = AccountRepository.getInstance(app)

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _userFeedbackString = MutableLiveData<Event<String>>()
    val userFeedbackString: LiveData<Event<String>>
        get() = _userFeedbackString

    private val _user = MutableLiveData<String?>(null)
    val user: LiveData<String?>
        get() = _user

    private val _navigateToConfirm = MutableLiveData<Event<Unit>>()
    val navigateToConfirm: LiveData<Event<Unit>>
        get() = _navigateToConfirm

    private val _confirmedEvent = MutableLiveData<Event<Unit>>()
    val confirmedEvent: LiveData<Event<Unit>>
        get() = _confirmedEvent

    fun submitIp(ip: String) {
        val token = prefsRepository.getApiToken()
        accountRepository.submitIpAndRetrieveToken(ip, token)
    }

    fun login(email: String, password: String) {
        doApiCall(
            call = { accountRepository.login(email, password) },
            onSuccess = {
                prefsRepository.setApiToken(it.value.token)
                _user.postValue(it.value.email)
            }
        )
    }

    fun register(email: String, password: String) {
        doApiCall(
            call = { accountRepository.register(email, password) },
            onSuccess = {
                _userFeedback.postOnce(R.string.confirm_mail_sent)
                _navigateToConfirm.postOnce(Unit)
            }
        )
    }

    fun confirmAccount(email: String, registrationCode: String) {
        doApiCall(
            call = { accountRepository.confirmAccount(email, registrationCode) },
            onSuccess = { _confirmedEvent.postOnce(Unit) }
        )
    }

    private fun <T> doApiCall(
        call: suspend () -> ApiResponse<T>,
        onSuccess: (ApiResponse.Success<T>) -> Unit
    ) {
        if (_isLoading.value == true) {
            return
        }

        _isLoading.postValue(true)

        viewModelScope.launch(IO) {
            when (val response = call()) {
                is ApiResponse.Success -> onSuccess(response)
                is ApiResponse.Failure -> _userFeedbackString.postOnce(response.message)
                is ApiResponse.UnknownError -> _userFeedback.postOnce(R.string.base_error)
                is ApiResponse.UnregisteredError -> _navigateToConfirm.postOnce(Unit)
            }

            _isLoading.postValue(false)
        }
    }
}