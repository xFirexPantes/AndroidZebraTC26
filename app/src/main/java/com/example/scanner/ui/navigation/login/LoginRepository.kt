package com.example.scanner.ui.navigation.login

import android.content.Context
import com.example.scanner.modules.Pref
import com.example.scanner.modules.ApiPantes.ApiState
import com.example.scanner.models.LoggedInUserResponse
import com.example.scanner.modules.ApiPantes
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class LoginRepository(
    val pref: Pref,
    private val apiPantes: ApiPantes,
) {

    companion object {
        private var _loginRepository: WeakReference<LoginRepository>? = null
        fun getInstanceSingleton(context: Context): LoginRepository {
            return _loginRepository
                ?.get()
                ?:run {
                    LoginRepository(
                        Pref.getInstanceSingleton(context),
                        ApiPantes.getInstanceSingleton()
                    ).apply {
                        _loginRepository = WeakReference(this)
                    }
                }
        }
    }

    var user: LoggedInUserResponse? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = pref.loggedInUserResponse
    }

    fun logout() {
//        val token=user?.token
//        token?.let {
//            Other.getInstanceSingleton().ioCoroutineScope.launch {
//                val result=apiPantes.logout(token)
//                if (result is ApiState.Error) {
//                    Other.getInstanceSingleton().mainCoroutineScope.launch {
//                        MainActivity.MainActivityRouter.getInstanceSingleton().navigate(
//                            ErrorsFragment::class.java,
//                            Bundle().apply { putSerializable(ErrorsFragment.PARAM,result.exception) }
//                        )
//                    }
//                }
//            }
//        }

        user = null
        pref.loggedInUserResponse=null

    }

    suspend fun login(username: String, password: String):ApiState<Any> {
        // handle login

        this.user = null

        return when (val result = apiPantes.login(username, password)) {
            is ApiState.Success -> {
                this.user = result.data
                pref.loggedInUserResponse=result.data
                // If user credentials will be cached in local storage, it is recommended it be encrypted
                // @see https://developer.android.com/training/articles/keystore
                result
            }
            else -> {
                result
            }

        }

    }

    fun isSessionTimeExpired(): Boolean {

        var result=false

        if ((System.currentTimeMillis()-pref.timeLastActive)>=TimeUnit.HOURS.toMillis(1)) {
            logout()
            result=true
        }

        pref.timeLastActive=
            System.currentTimeMillis()

        return result
    }

}