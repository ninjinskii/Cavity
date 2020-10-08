package com.louis.app.cavity.ui

interface InputValidation {
    fun setValidationRule(rule: (String) -> Boolean)
    fun validate()
    fun clearErrors()
}
