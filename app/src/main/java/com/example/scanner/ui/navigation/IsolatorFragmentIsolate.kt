package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.R
import com.example.scanner.app.templateAttributeDataTextView
import com.example.scanner.app.templateInputTextContainer
import com.example.scanner.app.templateSpinnerSpinner
import com.example.scanner.app.templateSpinnerContainer
import com.example.scanner.app.templateInputTextMyTextInput
import com.example.scanner.app.templateInputTextTextLayout
import com.example.scanner.app.onRightDrawableClicked
import com.example.scanner.app.setAttribute
import com.example.scanner.app.textInvalidValue
import com.example.scanner.app.templateAttributeTitleTextView
import com.example.scanner.databinding.TemplateButton2Binding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.models.IsolatorReasonsResponse
import com.example.scanner.models.IsolatorSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.dialogs.DatePickerFragment
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IsolatorFragmentIsolate : BaseFragment() , TextWatcher {

    companion object{
        const val PARAM="param"
    }

    private lateinit var isolatorSearchResponse: IsolatorReasonsResponse
    private val isolatorIsolateViewModel: IsolatorIsolateViewModel by viewModels{ viewModelFactory }
    lateinit var spinner: Spinner
    lateinit var until: EditText
    lateinit var note: EditText
    lateinit var quantity: EditText
    lateinit var buttonIsolating: AppCompatButton
    lateinit var amount: TextView
    lateinit var number: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply {
                toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
                val itemData:IsolatorSearchResponse.Item=getArgument(PARAM)
                toolbar.title=itemData.name
                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply {
                            //region db
                            arrayOf(
                                Pair(arrayOf("id"),"# компонента "),
                                Pair(arrayOf("nominal"),"Номинал "),
                                Pair(arrayOf("coil"),"Катушка "),
                                Pair(arrayOf("horizontalDivider"),""),
                                Pair(arrayOf("amount"),"На складе "),
                                Pair(arrayOf("isolated"),"В изоляторе "),
                            )
                                .forEach {pair->
                                    containerVertical.addView(
                                        TemplatePresenterBinding.inflate(layoutInflater,containerVertical,false)
                                            .apply {
                                                setAttribute(pair,itemData)
                                                when(pair.first[0].toString()){
                                                    "amount"->amount=templateAttributeDataTextView
                                                    "id"->number=templateAttributeDataTextView
                                                }
                                            }
                                            .root,
                                    )
                                }
                            //endregion
                            //region quantity
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateInputTextContainer.visibility= View.VISIBLE
                                        templateInputTextTextLayout.hint="Количество"
                                        templateInputTextMyTextInput.inputType= InputType.TYPE_CLASS_NUMBER
                                        //quantity=inputText
                                        templateInputTextMyTextInput.addTextChangedListener(object : TextWatcher{
                                            override fun beforeTextChanged(
                                                p0: CharSequence?,
                                                p1: Int,
                                                p2: Int,
                                                p3: Int
                                            ) {

                                            }

                                            override fun onTextChanged(
                                                p0: CharSequence?,
                                                p1: Int,
                                                p2: Int,
                                                p3: Int
                                            ) {

                                            }

                                            override fun afterTextChanged(p0: Editable?) {
                                                isValid()
                                            }
                                        })
                                        quantity=templateInputTextMyTextInput
                                        templateInputTextMyTextInput.onRightDrawableClicked {
                                            templateInputTextMyTextInput.text=null
                                        }

                                    }
                                    .root
                            )
                            //endregion
                            //region spinner
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(layoutInflater,containerVertical,false)
                                    .apply {
                                        templateAttributeTitleTextView.text="Причина "
                                        templateSpinnerContainer.visibility=
                                            View.VISIBLE
                                        spinner=templateSpinnerSpinner
                                    }
                                    .root
                            )
                            //endregion
                            //region until
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(layoutInflater,containerVertical,false)
                                    .apply {
                                        templateInputTextContainer.visibility=
                                            View.VISIBLE
                                        templateInputTextTextLayout.hint=""
                                        templateInputTextMyTextInput.setFocusable(false)
                                        templateInputTextMyTextInput.hint="Дата завершения изоляции "
                                        until=templateInputTextMyTextInput
                                        until.setOnClickListener {
                                            DatePickerFragment(
                                                defaultDate = until.text,
                                                {dateString: String ->
                                                    until.setText(dateString)
                                                    isValid()
                                                }
                                            ).show(childFragmentManager,DatePickerFragment::class.java.name)
                                        }
                                        until.addTextChangedListener(this@IsolatorFragmentIsolate)
                                        templateInputTextMyTextInput.setCompoundDrawables(null,null,null,null,)
                                    }
                                    .root
                            )
                            //endregion
                            //region note
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(layoutInflater,containerVertical,false)
                                    .apply {
                                        templateInputTextContainer.visibility=
                                            View.VISIBLE
                                        this@IsolatorFragmentIsolate.note=this@apply.templateInputTextMyTextInput
                                        templateInputTextMyTextInput.onRightDrawableClicked {
                                            templateInputTextMyTextInput.text=null
                                        }
                                    }
                                    .root
                            )
                            //endregion
                            //region isolate
                            containerHorizon.addView(
                                TemplateButton2Binding.inflate(inflater,containerHorizon,false)
                                    .apply {
                                        containerHorizon.gravity= Gravity.CENTER
                                        buttonScan.text="Изолировать"
                                        buttonIsolating=buttonScan
                                        buttonIsolating.setOnClickListener {
                                            if (isValid()) {
                                                isolatorIsolateViewModel.requestIsolating(
                                                    //component = "",
                                                    component = number.text.toString(),
                                                    note = note.text.toString(),
                                                    quantity = quantity.text.toString(),
                                                    reason = isolatorSearchResponse.found[spinner.selectedItemPosition].id,
                                                    until = until.text.toString()
                                                )
                                            }
                                        }
                                    }
                                    .root
                            )
                            //endregion

                            //region tune card
                            containerHorizon.gravity= Gravity.CENTER
                            root.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                            cardView.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                            containerHorizon.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT

                            (containerHorizon.parent as ViewGroup).layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                            //endregion

                        }
                        .root
                )

            }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isolatorIsolateViewModel.isolatorIsolateFragmentFormState.observe(viewLifecycleOwner){
            when(val state=it){
                is IsolatorIsolateFragmentFormState.Error -> {
                    isolatorIsolateViewModel.mainActivityRouter
                        .navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    state.throwable
                                )
                            })
                }
                is IsolatorIsolateFragmentFormState.SuccessReason ->{
                    isolatorSearchResponse=state.isolatorSearchResponse
                    spinner.adapter=ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        Array(state.isolatorSearchResponse.found.size,{i->state.isolatorSearchResponse.found[i].label})
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }

                }
                is IsolatorIsolateFragmentFormState.SuccessIsolate ->{
                    requireArguments().putSerializable(PARAM,state.any)
                    findNavController().navigateUp()
                }
            }
        }

        isolatorIsolateViewModel.requestIsolatorReason()

        isValid()
    }

    private fun isValid():Boolean {
        var result = true

        quantity.error=null
        amount.error=null
        until.error = null

        //region check Date
        if (until.text.isNullOrEmpty()) {
            until.error = textInvalidValue
            result = false
        }
        else if (try {
                (SimpleDateFormat(
                    getString(R.string.date_format),
                    Locale.getDefault()
                ).parse(until.text.toString())!!
                    .before(
                        Calendar.getInstance().apply {
                            add(Calendar.DATE, -1)
                            set(android.icu.util.Calendar.HOUR_OF_DAY, 23)
                            //set(android.icu.util.Calendar.HOUR,23)
                            set(android.icu.util.Calendar.MINUTE, 59)
                            set(android.icu.util.Calendar.SECOND, 59)
                        }.time
                    ))
            } catch (_: Exception) { true }) {
            until.error = textInvalidValue
            result = false
        }
        //endregion

        //region check quantity, amount
        try {
            if (quantity.text.toString().toInt() <= 0) {
                quantity.error = textInvalidValue
                result = false
            } else if (quantity.text.toString().toInt() > amount.text.toString().toInt()) {
                quantity.error = textInvalidValue
                amount.error=textInvalidValue
                result = false
            }
        }catch (_: Exception){
            quantity.error = textInvalidValue
        }
        //endregion

        buttonIsolating.isEnabled=result
        return result
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(p0: Editable?) {
        isValid()
    }

    sealed class IsolatorIsolateFragmentFormState<out T:Any> {
        data class  Error(val throwable: Throwable) : IsolatorIsolateFragmentFormState<Nothing>()
        data class  SuccessReason(val isolatorSearchResponse: IsolatorReasonsResponse) :
            IsolatorIsolateFragmentFormState<Nothing>()
        data class  SuccessIsolate(val any: IsolatorSearchResponse.Item) : IsolatorIsolateFragmentFormState<Nothing>()
    }

    class IsolatorIsolateViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository):BaseViewModel(){

        companion object {
            fun getInstance(context: Context): IsolatorIsolateViewModel {
                return   IsolatorIsolateViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context)
                )
            }
        }

        val isolatorIsolateFragmentFormState=
            MutableLiveData<IsolatorIsolateFragmentFormState<Any>>()

        fun requestIsolatorReason() {
            ioCoroutineScope.launch {
                isolatorIsolateFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> IsolatorIsolateFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.isolatorReason(
                                token = token,
                            )){
                                is ApiPantes.ApiState.Success->
                                    IsolatorIsolateFragmentFormState.SuccessReason(result.data)
                                is ApiPantes.ApiState.Error->
                                    IsolatorIsolateFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )

            }
        }

        fun requestIsolating(component:String,note:String,quantity:String,reason:String,until:String,) {
            ioCoroutineScope.launch {
                isolatorIsolateFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> IsolatorIsolateFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.isolatorIsolating(
                                component = component,
                                token = token,
                                note = note,
                                quantity = quantity,
                                reason = reason,
                                until = until
                            )){
                                is ApiPantes.ApiState.Success->
                                    IsolatorIsolateFragmentFormState.SuccessIsolate(result.data)
                                is ApiPantes.ApiState.Error->
                                    IsolatorIsolateFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )

            }
        }


    }
}
