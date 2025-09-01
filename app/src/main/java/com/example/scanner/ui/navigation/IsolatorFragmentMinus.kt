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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.app.templateAttributeDataTextView
import com.example.scanner.app.templateCheckBoxRoot
import com.example.scanner.app.floatDisable
import com.example.scanner.app.floatEnable
import com.example.scanner.app.templateInputTextMyTextInput
import com.example.scanner.app.templateInputTextTextLayout
import com.example.scanner.app.onRightDrawableClicked
import com.example.scanner.app.setAttribute
import com.example.scanner.app.textInvalidValue
import com.example.scanner.app.templateAttributeTitleTextView
import com.example.scanner.app.toRusString
import com.example.scanner.databinding.TemplateButton2Binding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.models.IsolatorSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class IsolatorFragmentMinus : BaseFragment() {

    companion object{
        const val PARAM="param"
        const val PARAM_RESULT="param1"
    }

    private val isolatorMinusViewModel: IsolatorMinusViewModel by viewModels{ viewModelFactory }

    private val isValidTextWatcher=object : TextWatcher{
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
    }

    private lateinit var quantity: EditText
    private lateinit var numberCoil: EditText
    private lateinit var numberCoilContainer: TextInputLayout
    private lateinit var buttonMinus: AppCompatButton
    private lateinit var noteNote: EditText
    private lateinit var amount: TextView
    private lateinit var coil: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply { toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
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
                                                    "coil"->coil=templateAttributeDataTextView
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
                                        templateInputTextTextLayout.hint="Количество"
                                        templateInputTextMyTextInput.inputType= InputType.TYPE_CLASS_NUMBER
                                        quantity=templateInputTextMyTextInput
                                        templateInputTextMyTextInput.addTextChangedListener(isValidTextWatcher)
                                        templateInputTextMyTextInput.onRightDrawableClicked {templateInputTextMyTextInput.text=null}

                                    }
                                    .root
                            )
                            //endregion

                            //region note
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateInputTextTextLayout.hint="Примечание"
                                        noteNote=templateInputTextMyTextInput
                                        templateInputTextMyTextInput.onRightDrawableClicked {
                                            templateInputTextMyTextInput.text=null
                                        }
                                    }
                                    .root
                            )
                            //endregion

                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateAttributeTitleTextView.text="Элемент на катушке / в упаковке"
                                        templateCheckBoxRoot.visibility= View.VISIBLE

                                    }
                                    .root
                            )
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {

                                        templateInputTextTextLayout.hint="Номер катушки / упаковки"
                                        templateInputTextMyTextInput.inputType= InputType.TYPE_CLASS_NUMBER
                                        numberCoil=templateInputTextMyTextInput
                                        templateInputTextMyTextInput.addTextChangedListener(isValidTextWatcher)
                                        numberCoilContainer=templateInputTextTextLayout
                                        templateInputTextMyTextInput.onRightDrawableClicked {
                                            templateInputTextMyTextInput.text=null
                                        }

                                    }
                                    .root
                            )
                            containerHorizon.addView(
                                TemplateButton2Binding.inflate(inflater,containerHorizon,false)
                                    .apply {
                                        containerHorizon.gravity= Gravity.CENTER
                                        buttonScan.text="Отминусовать"
                                        buttonMinus=buttonScan
                                        buttonScan.setOnClickListener {
                                            if (isValid()) {
                                                isolatorMinusViewModel.requestMinus(
                                                    numberCoil = if (numberCoil.text.isNullOrEmpty()) 0 else numberCoil.text?.toString()?.toInt(),
                                                    component = itemData.id.toString(),
                                                    note = noteNote.text?.toString(),
                                                    quantity = quantity.text?.toString(),
                                                )
                                            }                            }
                                    }
                                    .root
                            )


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
        isolatorMinusViewModel.isolatorMinusFragmentFormState
            .postValue(IsolatorMinusFragmentFormState.SetupView)

        isolatorMinusViewModel.isolatorMinusFragmentFormState.observe(viewLifecycleOwner){
            when(val state=it){
                is IsolatorMinusFragmentFormState.SetupView->{
                    isValid()
                }
                is IsolatorMinusFragmentFormState.Error -> {
                    isolatorMinusViewModel.mainActivityRouter
                        .navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    state.throwable
                                )
                            })
                }
                is IsolatorMinusFragmentFormState.SuccessMinus ->{
                    requireArguments().putSerializable(PARAM_RESULT,state.any)
                    findNavController().navigateUp()
                }
            }
        }

    }

    private fun isValid():Boolean{
        var result=true

        quantity.error=null
        numberCoil.error=null
        amount.error=null

        //region check coil
        try {
            if (coil.text.toString()==true.toRusString()) {
                numberCoil.isEnabled = true
                if (numberCoil.text?.isNotBlank()==true) {
                    numberCoil.text?.toString()?.toInt()
                }
                numberCoil.alpha= floatEnable
                numberCoilContainer.alpha= floatEnable
            }
            else{
                numberCoil.isEnabled=false
                numberCoil.alpha= floatDisable
                numberCoilContainer.alpha= floatDisable
            }
        }catch (_: Exception){
            result=false
            numberCoil.error=textInvalidValue
        }
        //endregion

        //region check quantity,amount
        try {

            if (quantity.text.toString().toDouble()<=0){
                quantity.error=textInvalidValue
                result=false
            }

            if(quantity.text.toString().toDouble() > amount.text.toString().toDouble()){
                quantity.error=textInvalidValue
                amount.error=textInvalidValue
                result=false
            }
        }catch (_: Exception){
            quantity.error=textInvalidValue
            result=false
        }
        //endregion

        buttonMinus.isEnabled=
            result

        return result
    }

    sealed class IsolatorMinusFragmentFormState<out T:Any> {
        data class  Error(val throwable: Throwable) : IsolatorMinusFragmentFormState<Nothing>()
        data class  SuccessMinus(val any: IsolatorSearchResponse.Item) : IsolatorMinusFragmentFormState<Nothing>()
        data object SetupView: IsolatorMinusFragmentFormState<Nothing>()
    }

    class IsolatorMinusViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository):BaseViewModel(){

        companion object {
            fun getInstance(context: Context): IsolatorMinusViewModel {
                return   IsolatorMinusViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context)
                )
            }
        }

        val isolatorMinusFragmentFormState=
            MutableLiveData<IsolatorMinusFragmentFormState<Any>>()

        fun requestMinus(numberCoil:Int?, component:String?, note:String?, quantity:String?,) {
            ioCoroutineScope.launch {
                isolatorMinusFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> IsolatorMinusFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.isolatorMinus(
                                token = token,
                                numberCoil=numberCoil,
                                component = component,
                                note = note,
                                quantity = quantity,
                            )){
                                is ApiPantes.ApiState.Success->
                                    IsolatorMinusFragmentFormState.SuccessMinus(result.data)
                                is ApiPantes.ApiState.Error->
                                    IsolatorMinusFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )

            }
        }


    }
}
