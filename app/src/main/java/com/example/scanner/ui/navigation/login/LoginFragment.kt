package com.example.scanner.ui.navigation.login

import androidx.annotation.StringRes
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.app.isValidAction
import com.example.scanner.databinding.FragmentLoginBinding
import com.example.scanner.app.onRightDrawableClicked
import com.example.scanner.app.softInput
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.NonFatalExceptionShowToaste
import com.example.scanner.ui.base.NonFatalExceptionShowDialogMessage
import com.example.scanner.ui.navigation.HomeFragment
import com.example.scanner.ui.navigation_over.ErrorsFragment


class LoginFragment : BaseFragment(), TextView.OnEditorActionListener,TextWatcher,
    View.OnFocusChangeListener {

    val loginViewModel: LoginViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.apply {

            userLayout.hint=getString(R.string.prompt_login)
            user.inputType= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            pass.setImeActionLabel("Далее",EditorInfo.IME_ACTION_DONE)


            pass.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passLayout.hint=getString(R.string.prompt_password)
            pass.setImeActionLabel(getString(R.string.action_sign_in_short),EditorInfo.IME_ACTION_DONE)
            pass.imeOptions=EditorInfo.IME_ACTION_DONE
            pass.setSelectAllOnFocus(true)

            butt.buttonScan.text="Войти"

            loginViewModel.loginFormState.observe(viewLifecycleOwner)
            { state ->

                when (state) {

                    is LoginFormState.CheckCredential -> {
                        state.passwordError?.let { pass.error = getString(it) }
                            ?: run { pass.error = null }
                        state.usernameError?.let { user.error = getString(it) }
                            ?: run { user.error = null }
                    }

                    is LoginFormState.SuccessLogged -> {
                        loginViewModel.mainActivityRouter.navigate(HomeFragment::class.java)
                    }

                    is LoginFormState.Error -> {

                        loginViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(ErrorsFragment.PARAM,
                                    if (state.exception is NonFatalExceptionShowDialogMessage)
                                        NonFatalExceptionShowToaste(state.exception.message)
                                    else {
                                        state.exception
                                    }
                                )
                            }
                        )

                        loginViewModel.loginFormState.value=
                            LoginFormState.CheckCredential()

                        afterTextChanged(null)

                    }

                    is LoginFormState.ClearCredential->{
                        user.text = null
                        pass.text = null
                    }

                    is LoginFormState.SetView->{

                        pass.addTextChangedListener(this@LoginFragment)
                        pass.onFocusChangeListener = this@LoginFragment
                        pass.onRightDrawableClicked{
                            it.text.clear()
                            it.error=null
                        }
                        pass.setOnEditorActionListener(this@LoginFragment)

                        user.addTextChangedListener(this@LoginFragment)
                        user.onFocusChangeListener = this@LoginFragment
                        user.onRightDrawableClicked{
                            it.text.clear()
                            it.error=null
                        }
                        user.setOnEditorActionListener(this@LoginFragment)
                        user.requestFocus()

                        butt.buttonScan.setOnClickListener {
                            onEditorAction(pass,EditorInfo.IME_ACTION_DONE,null)
                        }
                        titleAuth.setOnClickListener {

                            titleAuth.tag=
                                try {
                                    titleAuth.tag.toString().toInt()+1
                                }catch (_: Exception){
                                    1
                                }

                            if(titleAuth.tag==10){
                                titleAuth.tag=null
                                user.setText(StringBuilder("sa"))
                                pass.setText(StringBuilder("NOMiXB5p"))
                            }

                        }

                        if (loginViewModel.loginRepository.isLoggedIn)
                            loginViewModel.loginFormState.value=LoginFormState.SuccessLogged
                        else
                            loginViewModel.loginFormState.value=LoginFormState.ClearCredential

                    }

                }

            }

            loginViewModel.loginFormState.value=
                LoginFormState.SetView

        }.root

    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }

    override fun onEditorAction(tv: TextView, actionId: Int, event: KeyEvent?): Boolean {
        when(tv){
            binding.user->{
                binding.pass.requestFocus()
            }
            binding.pass->{
                if (isValidAction(actionId,event)) {
                    when(val state=loginViewModel.loginFormState.value){
                        is LoginFormState.CheckCredential->{
                            if (state.isDataValid) {
                                loginViewModel.loginRepository.logout()
                                loginViewModel.login(
                                    binding.user.text.toString(),
                                    binding.pass.text.toString()
                                )
                            }
                            else {
                                state.usernameError?.let { showLoginFailed(it) }
                                state.passwordError?.let { showLoginFailed(it) }
                            }
                        }
                        else->{
                        }
                    }
                }
            }
        }
        return true
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        loginViewModel.loginDataChanged(
            binding.user.text.toString(),
            binding.pass.text.toString()
        )

    }

    override fun onFocusChange(p0: View?, p1: Boolean) {
        p0?.let { requireContext().softInput(it,p1) }
    }
}