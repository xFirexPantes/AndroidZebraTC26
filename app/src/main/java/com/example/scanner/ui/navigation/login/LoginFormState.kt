package com.example.scanner.ui.navigation.login

/**
 * Data validation state of the login form.
 */
sealed class LoginFormState<out T : Any>{
    data class CheckCredential(
        val usernameError: Int?=null,
        val passwordError: Int?=null,
        val isDataValid: Boolean = false
    ):LoginFormState<Nothing>()

    data object SuccessLogged:LoginFormState<Nothing>()
    data class Error(val exception: Throwable?) : LoginFormState<Nothing>()
    data object ClearCredential : LoginFormState<Nothing>()
    data object SetView : LoginFormState<Nothing>()

}