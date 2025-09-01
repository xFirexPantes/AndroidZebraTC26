package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.R
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.models.InvoiceInfoResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class InvoiceFragmentInfo: BaseFragment() {

    companion object{
        const val PARAMS_INVOICE_ID="params"
    }

    private val invoiceInfoViewModel: InvoiceInfoViewModel by viewModels { viewModelFactory  }
    private lateinit var layoutContent:LinearLayoutCompat


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        invoiceInfoViewModel.invoiceInfoFormState.observe(viewLifecycleOwner){
            when(it){
                is InvoiceInfoFormState.Success ->{

                    val invoiceInfoResponse=
                        it.data as InvoiceInfoResponse

                    invoiceInfoViewModel.invoiceFragmentInfoTitle.postValue(
                        getString(
                            R.string.format_title_invoice,
                            invoiceInfoResponse.number
                        )
                    )
                    invoiceInfoViewModel.invoiceFragmentInfoSubtitle.postValue(
                        getString(
                            R.string.format_subtitle_invoice,
                            getArgument(PARAMS_INVOICE_ID)
                        )
                    )

                    layoutContent.removeAllViews()
                    arrayOf(
                        Pair(arrayOf<Any>("name","","", LinearLayout.VERTICAL),"Наименование изделия "),
                        Pair(arrayOf("partial"),"Частичная отгрузка "),
                        Pair(arrayOf("date"),"Дата "),
                        Pair(arrayOf("client"),"Заказчик "),
                        Pair(arrayOf("owner"),"Владелец "),
                        Pair(arrayOf("note"),"Примечание "),
                    ).forEach { pair ->
                        layoutContent.addView(
                            TemplatePresenterBinding.inflate(inflater,layoutContent,false)
                                .apply {
                                    setAttribute(pair,invoiceInfoResponse)
                                }
                                .root
                        )
                    }

                }
                is InvoiceInfoFormState.Error -> {
                    invoiceInfoViewModel.mainActivityRouter.navigate(
                        ErrorsFragment::class.java,
                        Bundle().apply {
                            putSerializable(
                                ErrorsFragment.PARAM,
                                it.exception
                            )
                        })
                }
            }
        }

        invoiceInfoViewModel.requestInfo(getArgument(PARAMS_INVOICE_ID))

        return TemplateFragmentBinding.inflate(inflater, container, false)
            .apply {

                toolbar.apply {
                        setNavigationOnClickListener {
                            findNavController().navigateUp()
                        }
                        invoiceInfoViewModel.invoiceFragmentInfoTitle
                            .observe(viewLifecycleOwner){title=it}
                        invoiceInfoViewModel.invoiceFragmentInfoSubtitle
                            .observe(viewLifecycleOwner){subtitle=it}
                    }

                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply {
                            this@InvoiceFragmentInfo.layoutContent=containerVertical
                        }
                        .root
                )

            }
            .root
    }

    sealed class InvoiceInfoFormState<out T : Any>{
        data class Success<out T : Any>(val data:T): InvoiceInfoFormState<T>()
        data class Error(val exception: Throwable) : InvoiceInfoFormState<Nothing>()

    }

    class InvoiceInfoViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository):
        BaseViewModel() {

        companion object {
            private var _invoiceInfoViewModel: WeakReference<InvoiceInfoViewModel>? = null
            fun getInstanceSingleton(context: Context): InvoiceInfoViewModel {
                return _invoiceInfoViewModel
                    ?.get()
                    ?:run {
                        InvoiceInfoViewModel(
                            ApiPantes.getInstanceSingleton(),
                            LoginRepository.getInstanceSingleton(context)
                        ).apply { _invoiceInfoViewModel = WeakReference(this) }
                    }
            }
        }

        val invoiceFragmentInfoTitle=
            MutableLiveData<String?>()
        val invoiceFragmentInfoSubtitle=
            MutableLiveData<String?>()

        val invoiceInfoFormState=
            MutableLiveData<InvoiceInfoFormState<*>>()

        fun requestInfo(id: String) {
            Other.getInstanceSingleton().ioCoroutineScope.launch {
                invoiceInfoFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InvoiceInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when (val result = apiPantes.invoiceInfo(token, id)) {
                            is ApiPantes.ApiState.Success -> InvoiceInfoFormState.Success(result.data)
                            is ApiPantes.ApiState.Error -> InvoiceInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }

    }
}