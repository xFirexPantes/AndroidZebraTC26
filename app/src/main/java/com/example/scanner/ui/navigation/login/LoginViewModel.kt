package com.example.scanner.ui.navigation.login

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.R
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.ui.navigation_over.ProgressFragment
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class LoginViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    companion object {
        private var _loginViewModel: WeakReference<LoginViewModel>? = null
        fun getInstanceSingleton(context: Context): LoginViewModel {
            return _loginViewModel
                ?.get()
                ?:run {
                    LoginViewModel(
                        LoginRepository.getInstanceSingleton(context))
                        .apply { _loginViewModel =WeakReference(this) }
                }
        }
    }

    val loginFormState=
        MutableLiveData<LoginFormState<Any>>()



    fun login(username: String, password: String) {
        mainActivityRouter.navigate(ProgressFragment::class.java)
        Other.getInstanceSingleton().ioCoroutineScope.launch {
            when(val result=loginRepository.login(username, password)){
                is ApiPantes.ApiState.Success->{
                    loginFormState.postValue(LoginFormState.SuccessLogged)
                }
                is ApiPantes.ApiState.Error->{
                    loginFormState.postValue(LoginFormState.Error(result.exception))
                }
            }
        }

    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            loginFormState.value = LoginFormState.CheckCredential(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            loginFormState.value = LoginFormState.CheckCredential(passwordError = R.string.invalid_password)
        } else {
            loginFormState.value = LoginFormState.CheckCredential(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
//        return if (!username.contains("@")) {
//            Patterns.EMAIL_ADDRESS.matcher(username).matches()
//        } else {
//            username.isNotBlank()
//        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return true
        //return password.length > 5
    }



}