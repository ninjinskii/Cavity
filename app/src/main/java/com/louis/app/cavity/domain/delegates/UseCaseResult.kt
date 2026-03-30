package com.louis.app.cavity.domain.delegates

import androidx.annotation.StringRes
import com.louis.app.cavity.R

sealed interface UseCaseResult {
    data class Success(@param:StringRes val message: Int) : UseCaseResult
    data class Fail(@param:StringRes val message: Int = R.string.base_error) : UseCaseResult
}
