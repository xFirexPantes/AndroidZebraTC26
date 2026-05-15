package com.example.scanner.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragmentDialog
import com.example.scanner.ui.navigation.login.LoginRepository
import androidx.core.graphics.drawable.toDrawable
import com.example.scanner.databinding.TemplateDialogBinding
import com.example.scanner.models.IssuanceIssueResponse
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch

class IssuanceIssueDialog(val confirmEnable: Boolean=true) : BaseFragmentDialog() {

    companion object{
        const val PARAM_LINE_ID="param"
        const val PARAM_INVOICE_ID="param1"
        const val PARAM_INVOICE_NAME="param2"
        const val PARAM_LINE_NAME="param3"
        const val PARAM_COIL="param4"
        const val PARAM_COLLECTED="param5"
        const val PARAM_COMMENT="param55"
        const val PARAM6_RESULT="param6"
        const val PARAM_YARL="params3"
        const val PARAM_CHECKED=""
        const val PARAM_RGM="0"
    }

    private val issuanceIssueDialogViewModel:IssuanceIssueDialogViewModel by viewModels { viewModelFactory  }
    private lateinit var invoiceId:String
    private lateinit var invoiceName:String
    private var lineId:String?=null
    private var lineName:String?=null
    private var collected:Boolean?=null
    private var coil: String?=null
    private var yarl: String?=null
    private var rgm: String?=null
    private var checked:Boolean?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

    return TemplateDialogBinding.inflate(inflater,container,false)
            .apply {

                root.post {

                    dialog?.setCancelable(false)

                    dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

                    dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                }

                lineId=getArgument<Int?>(PARAM_LINE_ID)?.toString()

                invoiceId=getArgument(PARAM_INVOICE_ID)
                invoiceName=getArgument(PARAM_INVOICE_NAME)
                lineName=getArgument(PARAM_LINE_NAME)
                coil=getArgument(PARAM_COIL)
                collected=getArgument(PARAM_COLLECTED)
                checked=getArgument(PARAM_CHECKED)
                yarl=getArgument(PARAM_YARL) ?: "params3"
                rgm=getArgument(PARAM_RGM) ?: "0"
                if (rgm == "0") {
                    toolbar.title = when (collected!!) {
                        false -> "Завершить сборку?"
                        true -> "Отменить сборку?"
                    }
                }
                else{
                    toolbar.title = when (checked!!) {
                        false -> "Завершить проверку?"
                        true -> "Отменить проверку?"
                    }
                }
                toolbar.subtitle=null

                text.text=StringBuilder()
                    .apply {


                        when(lineId){
                            null->{

                            }
                            else -> {
                                append(
                                    when(coil){
                                        null->"Строка $lineName"
                                        else->"Строка $lineName Катушка $coil"
                                    }
                                )
                                append(" ")
                            }
                        }


                        append(
                            StringBuilder()
                                .append("Накл.")
                                .append(invoiceName)
                        )

                    }

                cancel.setOnClickListener { dismiss() }

                ok.setOnClickListener {
                    when(rgm){
                        "0"->{when(collected!!){
                            false->{
                                issuanceIssueDialogViewModel.requestIssuanceIssue(
                                    coil=coil,
                                    comment = getArgument<String?>(PARAM_COMMENT)?:"",
                                    invoice=invoiceId,
                                    line = lineId,
                                    yarl= yarl ?: "params3",
                                    rgm = "0",
                                )
                            }
                            true->{
                                issuanceIssueDialogViewModel.requestIssuanceReturn(
                                    coil=coil,
                                    invoice=invoiceId,
                                    line = lineId,
                                    yarl= yarl ?: "params3",
                                    rgm = "0",
                                )
                            }
                            }
                        }
                        "1"->{when(checked!!){
                            false->{
                                issuanceIssueDialogViewModel.requestIssuanceIssue(
                                    coil=coil,
                                    comment = getArgument<String?>(PARAM_COMMENT)?:"",
                                    invoice=invoiceId,
                                    line = lineId,
                                    yarl= yarl ?: "params3",
                                    rgm = "1",
                                )
                            }
                            true->{
                                issuanceIssueDialogViewModel.requestIssuanceReturn(
                                    coil=coil,
                                    invoice=invoiceId,
                                    line = lineId,
                                    yarl= yarl ?: "params3",
                                    rgm = "1",
                                )
                            }
                        }
                        }
                    }

                }

                issuanceIssueDialogViewModel.issuanceIssueDialogFormState.observe(viewLifecycleOwner){
                    when(val state=it){
                        is IssuanceIssueDialogFormState.SuccessIssuanceIssue->{
                                requireArguments().putSerializable(PARAM6_RESULT,state.issuanceIssueResponseSAD.data)
                                dismiss()
                        }
                        is IssuanceIssueDialogFormState.SuccessIssuanceReturn->{
                            requireArguments().putSerializable(PARAM6_RESULT,state.data)
                                dismiss()
                        }
                        is IssuanceIssueDialogFormState.Error->{

                            issuanceIssueDialogViewModel.mainActivityRouter.navigate(
                                ErrorsFragment::class.java,
                                Bundle().apply { putSerializable(ErrorsFragment.PARAM,state.exceptionSAD.data) }
                            )
                                 dismiss()
                        }
                    }
                }


                if (!confirmEnable){
                    ok.performClick()
                }

        }
            .root.apply {
                visibility=if (confirmEnable) View.VISIBLE else View.GONE
        }
    }

}

class IssuanceIssueDialogViewModel(private val apiPantes: ApiPantes,private val loginRepository: LoginRepository):
    BaseViewModel(){
    fun requestIssuanceIssue(
    coil: String?,
    comment: String = "",
    invoice: String,
    line: String? = null,
    yarl: String,
    rgm: String,
    ) {
        Other.getInstanceSingleton().ioCoroutineScope.launch {
            val token = loginRepository.user?.token
            if (token == null) {
                issuanceIssueDialogFormState.postValue(
                    IssuanceIssueDialogFormState.Error(Other.SAD(Exception("Отсутствует токен")))
                )
                return@launch
            }

            val result = try {
                apiPantes.issuanceIssue(token, coil, comment, invoice, yarl,rgm, line)
            } catch (e: Exception) {
                ApiPantes.ApiState.Error(e)
            }

            when (result) {
                is ApiPantes.ApiState.Success -> {
                    issuanceIssueDialogFormState.postValue(
                        IssuanceIssueDialogFormState.SuccessIssuanceIssue(Other.SAD(result.data))
                    )
                }
                is ApiPantes.ApiState.Error -> {
                    issuanceIssueDialogFormState.postValue(
                        IssuanceIssueDialogFormState.Error(Other.SAD(result.exception))
                    )
                }
            }
        }
    }
    fun requestIssuanceReturn(
        coil: String?,
        invoice:String,
        line: String?,
        yarl: String?,
        rgm: String?
    ) {
        Other.getInstanceSingleton().ioCoroutineScope.launch {
            loginRepository.user?.token?.let { token->
            when(val result=apiPantes.issuanceReturn(
                token = token,
                coil = coil,
                invoice = invoice,
                line = line,
                yarl = yarl,rgm = rgm
            )){
                is ApiPantes.ApiState.Success->{
                    issuanceIssueDialogFormState.postValue(
                        IssuanceIssueDialogFormState.SuccessIssuanceReturn(result.data))
                }
                is ApiPantes.ApiState.Error->{
                    issuanceIssueDialogFormState.postValue(
                        IssuanceIssueDialogFormState.Error(Other.SAD(result.exception)))
                }
            }


            }
        }
    }

    val issuanceIssueDialogFormState=
        MutableLiveData<IssuanceIssueDialogFormState<Any>>()

}

sealed class IssuanceIssueDialogFormState<out T : Any> {
    data class SuccessIssuanceReturn(val data: IssuanceIssueResponse):IssuanceIssueDialogFormState<IssuanceIssueResponse>()
    data class SuccessIssuanceIssue(val issuanceIssueResponseSAD: Other.SAD<IssuanceIssueResponse>):IssuanceIssueDialogFormState<IssuanceIssueResponse>()
    data class Error(val exceptionSAD: Other.SAD<Throwable>):IssuanceIssueDialogFormState<Nothing>()
}