package com.example.scanner.ui.navigation

import android.content.Context
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.scanner.R
import com.example.scanner.app.templateAttributeDataTextView
import com.example.scanner.app.templateCheckBoxCheckBox
import com.example.scanner.app.templateInputTextContainer
import com.example.scanner.app.templateInputTextMyTextInput
import com.example.scanner.app.templateInputTextTextLayout
import com.example.scanner.app.onRightDrawableClicked
import com.example.scanner.app.setAttribute
import com.example.scanner.app.templateAttributeTitleTextView
import com.example.scanner.app.templateCheckBoxCheckBoxPr
import com.example.scanner.databinding.TemplateButton2Binding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplateProgressBinding
import com.example.scanner.databinding.TemplateTabLayoutBinding
import com.example.scanner.models.IssuanceIssueResponse
import com.example.scanner.models.LinesInfoResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseUiMutableLiveData
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.dialogs.IssuanceIssueDialog
import com.example.scanner.ui.dialogs.IssuanceIssueDialog.Companion.PARAM6_RESULT
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject

class InvoiceFragmentInfoLinePr:BaseFragment(){

    companion object{
        const val PARAM_LINE_ID="param"
        const val PARAM_LINE_COLLECTED="param4"
        const val PARAM_INVOICE_ID="param1"
        const val PARAM_INVOICE_NAME="param2"
        const val PARAM_YARL="params3"
        const val PARAM3_RESULT="param3"
        const val PARAM_LINE_CHECKED="param4"
    }

    private val invoiceLineInfoViewModel: InvoiceLineInfoViewModel by viewModels { viewModelFactory  }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private lateinit var linesInfoResponse:LinesInfoResponse

    private lateinit var lineId:String
    private lateinit var invoiceId:String
    private lateinit var yarl:String
    private lateinit var invoiceNumber:String
    private val tabName0="Склад"
    private val tabName1="Характеристики"

    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

         return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply {
                val progressView=TemplateProgressBinding.inflate(inflater,root,false)
                    .apply {
                        progressBar.visibility= View.VISIBLE
                    }
                    .root

                toolbar.apply {
                    setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }

                    //region iconManual
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                invoiceLineInfoViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
                                    root.visibility=it
                                }
                                src= ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)
                                image.setOnClickListener {
                                    scanViewModel.scannerApiEmulator.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )
                    //endregion
                    //region iconScan
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                invoiceLineInfoViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
                                    src=ResourcesCompat.getDrawable(resources,it,null)
                                }
                                image.setOnClickListener {
                                    scanViewModel.scannerApi.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )
                    //endregion
                }
                val card= TemplateCardBinding.inflate(inflater,root,false)

                val tabs=TemplateTabLayoutBinding.inflate(inflater,root,false).root
                val onTabSelectedListener=object :OnTabSelectedListener{
                    override fun onTabSelected(tab: TabLayout.Tab?) {

                        card.containerVertical.removeAllViews()
                        when(tab?.text){
                            tabName0->{
                                    val arrayListViews= ArrayList<View>()
                                    arrayOf(
                                        Pair(arrayOf("number"),"# компонента "),
                                        Pair(arrayOf("quantity"),"Количество "),
                                        Pair(arrayOf("amount"),"На складе "),
                                        Pair(arrayOf("separate"),"Отдельно "),
                                        Pair(arrayOf("horizontalDivider"),""),
                                    )
                                        .forEach {pair->
                                            arrayListViews.add(
                                                TemplatePresenterBinding.inflate(layoutInflater,card.containerVertical,false)
                                                    .apply {
                                                        setAttribute(pair,linesInfoResponse)
                                                    }
                                                    .root
                                            )
                                        }

                                    //region катушек собрано
                                    arrayListViews.add(
                                        TemplatePresenterBinding.inflate(layoutInflater,card.containerVertical,false)
                                            .apply {
                                                templateAttributeDataTextView.text=
                                                    StringBuilder()
                                                        .append("Катушки (Cобрано ")
                                                        .append(linesInfoResponse.coils.filter { it.collected }.size.toString())
                                                        .append(" из ")
                                                        .append(linesInfoResponse.coils.size)
                                                        .append(")")
                                            }
                                            .root
                                    )
                                    //endregion
                                    //region список катушек
                                val states = arrayOf(
                                    intArrayOf(android.R.attr.state_checked), // Состояние "включён"
                                    intArrayOf(-android.R.attr.state_checked) // Состояние "выключен"
                                )

                                val colors = intArrayOf(
                                    ContextCompat.getColor(view!!.context, R.color.checkbox_checked),   // Цвет для включённого состояния
                                    ContextCompat.getColor(view!!.context, R.color.checkbox_unchecked) // Цвет для выключенного
                                )
                                val colorsPr = intArrayOf(
                                    ContextCompat.getColor(view!!.context, R.color.checkbox_checkedPr),   // Цвет для включённого состояния
                                    ContextCompat.getColor(view!!.context, R.color.checkbox_unchecked) // Цвет для выключенного
                                )

                                val colorStateList = ColorStateList(states, colors)

                                val colorStateListPr = ColorStateList(states, colorsPr)
                                    linesInfoResponse.coils.forEach { coilsItem ->
                                        arrayListViews.add(
                                            TemplatePresenterBinding.inflate(layoutInflater,card.containerVertical,false)
                                                .apply {
                                                    templateAttributeDataTextView.text=StringBuilder(coilsItem.number.toString())
                                                        .append(" ")
                                                        .append(coilsItem.amount)
                                                        .append("/")
                                                        .append(coilsItem.quantity)
                                                    templateCheckBoxCheckBox.apply {
                                                        setOnClickListener {
                                                            buttonTintList = colorStateList
                                                            templateCheckBoxCheckBox.isChecked=!templateCheckBoxCheckBox.isChecked
//                                                            IssuanceIssueDialog()
//                                                                .apply {
//                                                                    arguments=
//                                                                        Bundle().apply {
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_LINE_ID,lineId.toInt())
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_INVOICE_ID,invoiceId)
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_INVOICE_NAME,invoiceNumber)
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_LINE_NAME,linesInfoResponse.name)
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_COIL,coilsItem.number.toString())
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_COLLECTED,coilsItem.collected)
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_COMMENT,StringBuilder(linesInfoResponse.comment).toString())
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_YARL,yarl)
//                                                                            putSerializable(IssuanceIssueDialog.PARAM_CHECKED,coilsItem.checkedpr)
//                                                                        }
//                                                                }
//                                                                .show(
//                                                                    childFragmentManager,
//                                                                    IssuanceIssueDialog::class.java.name)

                                                        }
                                                        isChecked=coilsItem.collected
                                                        (parent as ViewGroup).visibility=View.VISIBLE
                                                        //isEnabled=!getArgument<Boolean>(PARAM_LINE_COLLECTED)
                                                    }
                                                    templateCheckBoxCheckBoxPr.apply {
                                                        buttonTintList = colorStateListPr
                                                        setOnClickListener {
                                                            templateCheckBoxCheckBoxPr.isChecked=!templateCheckBoxCheckBoxPr.isChecked
                                                            IssuanceIssueDialog()
                                                                .apply {
                                                                    arguments=
                                                                        Bundle().apply {
                                                                            putSerializable(IssuanceIssueDialog.PARAM_LINE_ID,lineId.toInt())
                                                                            putSerializable(IssuanceIssueDialog.PARAM_INVOICE_ID,invoiceId)
                                                                            putSerializable(IssuanceIssueDialog.PARAM_INVOICE_NAME,invoiceNumber)
                                                                            putSerializable(IssuanceIssueDialog.PARAM_LINE_NAME,linesInfoResponse.name)
                                                                            putSerializable(IssuanceIssueDialog.PARAM_COIL,coilsItem.number.toString())
                                                                            putSerializable(IssuanceIssueDialog.PARAM_COLLECTED,coilsItem.collected)
                                                                            putSerializable(IssuanceIssueDialog.PARAM_COMMENT,StringBuilder(linesInfoResponse.comment).toString())
                                                                            putSerializable(IssuanceIssueDialog.PARAM_YARL,yarl)
                                                                            putSerializable(IssuanceIssueDialog.PARAM_CHECKED,coilsItem.checkedpr)
                                                                            putSerializable(IssuanceIssueDialog.PARAM_RGM,"1")
                                                                        }
                                                                }
                                                                .show(
                                                                    childFragmentManager,
                                                                    IssuanceIssueDialog::class.java.name)

                                                        }
                                                        isChecked=coilsItem.checkedpr
                                                        visibility=View.VISIBLE
                                                        //isEnabled=!getArgument<Boolean>(PARAM_LINE_COLLECTED)
                                                    }
                                                    if (coilsItem.isused) {
                                                        templateAttributeDataTextView.setBackgroundColor(
                                                            ContextCompat.getColor(
                                                                layoutInflater.context,
                                                                R.color.yellow
                                                            )
                                                        )
                                                    } else {
                                                        templateAttributeDataTextView.setBackgroundColor(
                                                            ContextCompat.getColor(
                                                                layoutInflater.context,
                                                                R.color.default_background
                                                            )
                                                        )
                                                    }

                                                }
                                                .root
                                        )

                                    }
                                    //endregion
                                    //region примечание
                                    arrayListViews.add(
                                        TemplatePresenterBinding.inflate(layoutInflater,card.containerVertical,false)
                                            .apply {
                                                templateInputTextContainer.visibility= View.VISIBLE
                                                templateInputTextTextLayout.hint="Примечание"
                                                templateInputTextMyTextInput.onRightDrawableClicked {templateInputTextMyTextInput.text=null}
                                                templateInputTextMyTextInput.setText(StringBuilder(linesInfoResponse.comment))
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
                                                        p0?.let {
                                                            linesInfoResponse.comment=it.toString()
                                                        }
                                                    }
                                                })

                                            }
                                            .root
                                    )
                                    //endregion
                                    arrayListViews.add(
                                        TemplateButton2Binding.inflate(layoutInflater,card.containerVertical,false)
                                            .apply {
                                                buttonScan.text="Повернуть лифт"
                                                        buttonScan.isEnabled=linesInfoResponse.elevator
                                                        buttonScan.setOnClickListener {
                                                            invoiceLineInfoViewModel.issuanceElevator(lineId,invoiceId)
                                                        }
                                            }
                                            .root
                                    )

                                    arrayListViews.forEach {
                                        card.containerVertical.addView(it)
                                    }
                                root.removeView(progressView)
                            }
                            tabName1->{
                                linesInfoResponse.attributes.forEach { att->
                                    card.containerVertical.addView(
                                        TemplatePresenterBinding.inflate(layoutInflater,card.containerVertical,false)
                                            .apply {
                                                templateAttributeTitleTextView.text=StringBuilder(att.name).append(" ")
                                                templateAttributeDataTextView.text=att.value
                                            }
                                            .root
                                    )
                                }
                            }
                        }

                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {

                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {

                    }

                }

                root.addView(tabs)
                root.addView(card.root)

                invoiceLineInfoViewModel.invoiceLineInfoFragmentFormState.value=
                    InvoiceLineInfoFragmentFormState.SetupView

                mutableOnFragmentDetached.observe(viewLifecycleOwner){
                    when(val f=it.data){
                        is IssuanceIssueDialog->{
                            f.getArgument<IssuanceIssueResponse?>(PARAM6_RESULT)
                                ?.let { issuanceIssueResponse->
                                    requireArguments().putSerializable(PARAM_LINE_ID,issuanceIssueResponse.line!!.id.toInt())
                                    invoiceLineInfoViewModel.invoiceLineInfoFragmentFormState.postValue(
                                        InvoiceLineInfoFragmentFormState.SetupView
                                    )

                                    //requireArguments().putSerializable(PARAM3_RESULT,issuanceIssueResponse)

                                    //findNavController().navigateUp()

                                    //invoiceLineInfoViewModel.requestLineInfo(lineId,invoiceId)

                            }
                        }
                    }
                }

                invoiceLineInfoViewModel.invoiceLineInfoFragmentFormState.observe(viewLifecycleOwner){

                    when(val state=it){
                        is InvoiceLineInfoFragmentFormState.SetupView ->{
                            tabs.removeAllTabs()
                            tabs.addTab(tabs.newTab().apply { text=tabName0 })
                            tabs.addTab(tabs.newTab().apply { text=tabName1 })

                            lineId=getArgument<Int>(PARAM_LINE_ID).toString()
                            invoiceId=getArgument(PARAM_INVOICE_ID)
                            invoiceNumber=getArgument(PARAM_INVOICE_NAME)
                            yarl=getArgument(PARAM_YARL) ?: "params3"
                            invoiceLineInfoViewModel.requestLineInfo(lineId,invoiceId,yarl)
                        }
                        is InvoiceLineInfoFragmentFormState.Error ->{
                            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
                            val json = state.exception.message?.let { it1 -> JSONObject(it1) }
                            if (json != null) {
                                Toast.makeText(context, json.optString("message", "Ошибка сервера"),
                                    Toast.LENGTH_LONG ).show()
                            }

                        }
                        is InvoiceLineInfoFragmentFormState.SuccessLineInfo ->{
                            linesInfoResponse=state.linesInfoResponse
                            toolbar.title=linesInfoResponse.name
                            tabs.removeOnTabSelectedListener(onTabSelectedListener)
                            tabs.addOnTabSelectedListener(onTabSelectedListener)
                            root.post {
                                root.addView(progressView)
                                toolbar.post {
                                    onTabSelectedListener.onTabSelected(tabs.getTabAt(0))
                                }
                            }
                        }
                        is InvoiceLineInfoFragmentFormState.SuccessElevation->{
                            findNavController().navigateUp()
                        }
                        is InvoiceLineInfoFragmentFormState.SuccessComment -> {}
                    }

                }

                scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
                    when(val state=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            state.stringScanResult?.let {stringScanResult->
                                    IssuanceIssueDialog(false)
                                        .apply {
                                            arguments=Bundle()
                                                .apply {
                                                    putSerializable(IssuanceIssueDialog.PARAM_LINE_ID,null)
                                                    //putSerializable(IssuanceIssueDialog.PARAM_LINE_ID,lineId.toInt())
                                                    putSerializable(IssuanceIssueDialog.PARAM_INVOICE_ID,invoiceId)
                                                    putSerializable(IssuanceIssueDialog.PARAM_INVOICE_NAME,invoiceNumber)
                                                    putSerializable(IssuanceIssueDialog.PARAM_LINE_NAME,linesInfoResponse.name)
                                                    putSerializable(IssuanceIssueDialog.PARAM_COIL,stringScanResult)
                                                    putSerializable(IssuanceIssueDialog.PARAM_COLLECTED,false)
                                                    putSerializable(IssuanceIssueDialog.PARAM_CHECKED,false)
                                                    putSerializable(IssuanceIssueDialog.PARAM_YARL,yarl)
                                                    putSerializable(IssuanceIssueDialog.PARAM_RGM,"1")
                                                }
                                        }
                                        .show(childFragmentManager,IssuanceIssueDialog::class.java.name)

                            }
                        }
                        else->{}
                    }
                }

            }
            .root
    }

    override fun onPause() {
        if (this@InvoiceFragmentInfoLinePr::linesInfoResponse.isInitialized){
            getArgument<String?>(PARAM_INVOICE_ID)?.let {invoiceId->
                getArgument<Int?>(PARAM_LINE_ID)?.let { lineId ->
                    invoiceLineInfoViewModel.requestIssuanceComment(
                        invoiceId,
                        lineId.toString(),
                        linesInfoResponse.comment,
                        yarl
                    )
                }
            }
        }
        super.onPause()
    }

    sealed class InvoiceLineInfoFragmentFormState<out T : Any>{
        data object SetupView : InvoiceLineInfoFragmentFormState<Nothing>()
        data class Error(val exception: Throwable) : InvoiceLineInfoFragmentFormState<Nothing>()
        data class SuccessLineInfo(val linesInfoResponse: LinesInfoResponse):
            InvoiceLineInfoFragmentFormState<Nothing>()
        data class SuccessElevation(val r: ResponseBody):
            InvoiceLineInfoFragmentFormState<Nothing>()
        data class SuccessComment(val r: ResponseBody):
            InvoiceLineInfoFragmentFormState<Nothing>()

    }

    class InvoiceLineInfoViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref,
    ):BaseViewModel(){
        fun requestIssuanceComment(invoiceId: String, lineId: String, comment: String,yarl: String) {
            Other.getInstanceSingleton().ioCoroutineScope.launch {
                invoiceLineInfoFragmentFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InvoiceLineInfoFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> {
                            when (val result = apiPantes.issuanceComment(
                                token = token,
                                invoice = invoiceId,
                                yarl = yarl,
                                line = lineId,
                                comment=comment
                            )) {
                                is ApiPantes.ApiState.Success ->
                                    InvoiceLineInfoFragmentFormState.SuccessComment(result.data)

                                is ApiPantes.ApiState.Error ->
                                    InvoiceLineInfoFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )
            }
        }
        fun requestLineInfo(lineId: String, invoiceId: String,yarl:String) {
            Other.getInstanceSingleton().ioCoroutineScope.launch {
                invoiceLineInfoFragmentFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InvoiceLineInfoFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> {
                            when (val result = apiPantes.lineInfo(
                                token = token,
                                line = lineId,
                                invoice = invoiceId,
                                yarl = yarl
                            )) {
                                is ApiPantes.ApiState.Success ->
                                    InvoiceLineInfoFragmentFormState.SuccessLineInfo(result.data)

                                is ApiPantes.ApiState.Error ->
                                    InvoiceLineInfoFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )
            }
        }
        fun issuanceElevator(lineId: String, invoiceId: String) {
            Other.getInstanceSingleton().ioCoroutineScope.launch {
                invoiceLineInfoFragmentFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InvoiceLineInfoFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> {
                            when (val result = apiPantes.issuanceElevator(
                                token = token,
                                line = lineId,
                                invoice = invoiceId
                            )) {
                                is ApiPantes.ApiState.Success ->
                                    InvoiceLineInfoFragmentFormState.SuccessElevation(result.data)

                                is ApiPantes.ApiState.Error ->
                                    InvoiceLineInfoFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )
            }
        }

        companion object{
            fun getInstance(context: Context): InvoiceLineInfoViewModel {
                return InvoiceLineInfoViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }
        val invoiceLineInfoFragmentFormState=
            BaseUiMutableLiveData<InvoiceLineInfoFragmentFormState<Any>>()
    }

}


