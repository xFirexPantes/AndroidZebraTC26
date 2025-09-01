package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.scanner.R
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplateRecyclerBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateResultEmptyBinding
import com.example.scanner.databinding.TemplateScannerReadyBinding
import com.example.scanner.models.InvoiceSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.ApiPantes.ApiState
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch

class InvoiceFragment : BaseFragment() {

    companion object{
        const val PARAM="param"
    }

    private val invoicesViewModel: InvoicesViewModel by viewModels<InvoicesViewModel> {viewModelFactory}
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterInvoices=AdapterInvoices()


    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return TemplateFragmentBinding.inflate(inflater, container, false)
            .apply {

                toolbar.apply {
                    //region backArrow
                    setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }
                    //endregion
                    //region iconManual
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,iconContainer,false)
                            .apply {

                                src=ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)

                                invoicesViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
                                    root.visibility=it
                                }
                                image.setOnClickListener {
                                    scanViewModel.scannerApiEmulator.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )
                    //endregion
                    //region iconScan
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,iconContainer,false)
                            .apply {
                                invoicesViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
                                    src=ResourcesCompat.getDrawable(resources,it,null)
                                }
                                image.setOnClickListener {
                                    scanViewModel.scannerApi.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )
                    //endregion
                    //region title
                    invoicesViewModel.invoicesFragmentTitle.observe(viewLifecycleOwner){
                        title=it
                    }
                    //endregion
                    //region subtitle
                    invoicesViewModel.invoicesFragmentSubTitle.observe(viewLifecycleOwner){
                        subtitle=it
                    }
                    //endregion
                }

                //region invoiceRecycler
                root.addView(
                    TemplateRecyclerBinding.inflate(inflater,root,false)
                        .apply {

                            recycler.layoutManager =
                                object : LinearLayoutManager(requireContext()) {
                                    override fun onScrollStateChanged(state: Int) {
                                        super.onScrollStateChanged(state)

                                        if (findLastVisibleItemPosition()+1 == adapterInvoices.itemCount) {
                                            invoicesViewModel.invoiceSearch(
                                                getArgument(PARAM),
                                                adapterInvoices.last.toString()
                                            )
                                        }
                                    }
                                }

                            recycler.adapter=
                                adapterInvoices

                            //region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .apply {
                                        invoicesViewModel.invoicesFragmentEmpty
                                            .observe(viewLifecycleOwner){
                                                root.visibility=it
                                            }
                                        invoicesViewModel.invoicesFragmentEmpty
                                            .postValue(View.GONE)
                                    }
                                    .root
                            )
                            //endregion

                            //region ready
                            containerContent.addView(
                                TemplateScannerReadyBinding.inflate(inflater,containerContent,false)
                                    .apply {
                                        icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_qr,null)
                                        title="Сканируйте код накладной"
                                        iconLayout.image.setOnClickListener {
                                                scanViewModel.scannerApi.softScan(childFragmentManager,requireContext())
                                            }
                                        invoicesViewModel.invoicesFragmentReady
                                            .observe(viewLifecycleOwner){
                                                root.visibility=it
                                            }
                                    }
                                    .root
                            )
                            //endregion

                        }
                        .root
                )
                //endregion

            }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mutableOnFragmentDetached.observe(viewLifecycleOwner){

            when(val f=it.data){
                is InvoiceFragmentLines->{
                    adapterInvoices.updateItem(
                        f.getArgument<String>(InvoiceFragmentLines.PARAMS_INVOICE_ID),
                        f.getArgument<Boolean>(InvoiceFragmentLines.PARAMS2_COLLECTED),
                    )
                }
                else -> {}
            }
        }

        invoicesViewModel.invoicesFragmentState.observe(viewLifecycleOwner) {
            when(it) {
                is InvoicesFragmentState.Success ->
                    it.data?.let {invoiceSearchResponse->

                        invoiceSearchResponse as InvoiceSearchResponse

                        if(adapterInvoices.isResetContent){
                            invoicesViewModel.invoicesFragmentTitle.postValue(
                                getString(R.string.format_title,"${invoiceSearchResponse.found.size}")
                            )
                            invoicesViewModel.invoicesFragmentSubTitle.postValue(
                                getString(
                                    R.string.format_subtitle,
                                    getArgument(PARAM)
                                )
                            )

                            invoicesViewModel.invoicesFragmentEmpty
                                .postValue(
                                    if (invoiceSearchResponse.found.isNotEmpty()) {
                                        View.GONE
                                    }
                                    else{
                                        View.VISIBLE
                                    }
                                )
                        }

                        adapterInvoices
                            .appendContent(invoiceSearchResponse)

                    }
                is InvoicesFragmentState.Error ->
                    it.exception?.let { exception->
                        findNavController().navigateUp()
                        invoicesViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply { putSerializable(
                                ErrorsFragment.PARAM,exception) })
                    }
            }
        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
            when(val scanState=it){
                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                    scanState.stringScanResult?.let {stringScanResult->
                        arguments=Bundle().apply {
                            putSerializable(PARAM,stringScanResult)
                        }

                        adapterInvoices.setContent(InvoiceSearchResponse())
                        adapterInvoices.resetContent()
                        invoicesViewModel.invoiceSearch(
                            getArgument(PARAM),
                            ""
                        )

                        invoicesViewModel.invoicesFragmentReady
                            .postValue(
                                View.GONE
                            )
                    }
                }
                else->{}
            }
        }

        if (invoicesViewModel.invoicesFragmentState.value==null) {
            if (getArgument<String?>(PARAM).isNullOrEmpty()){
                invoicesViewModel.invoicesFragmentReady
                    .postValue(View.VISIBLE)
            }else{
                invoicesViewModel.invoicesFragmentReady
                    .postValue(View.GONE)

                invoicesViewModel.invoiceSearch(
                    getArgument(PARAM),
                    ""
                )
            }
            invoicesViewModel.invoicesFragmentTitle
                .postValue(
                    getString(R.string.button_invoice)
                )


        }
        else{
            invoicesViewModel.invoicesFragmentReady
                .postValue(View.GONE)
        }


    }

    inner class AdapterInvoices :BaseRecyclerAdapter<InvoiceSearchResponse>(InvoiceSearchResponse()){

        override fun getCallback(dataOld: InvoiceSearchResponse?): DiffUtil.Callback {
            return object :DiffUtil.Callback(){
                override fun getOldListSize(): Int {
                    return dataOld?.found?.size?:0
                }

                override fun getNewListSize(): Int {
                    return data.found.size
                }

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return data.found[newItemPosition].id==
                            dataOld?.found?.get(oldItemPosition)?.id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return data.found[newItemPosition].id==
                            dataOld?.found?.get(oldItemPosition)?.id
                }
            }
        }

        override fun appendData(dataNew: InvoiceSearchResponse) {
            if (dataNew.found.isNotEmpty()){
                data.last=dataNew.last
                data.found.addAll(dataNew.found)
            }
        }

        override fun getLastId(): String {
            return data.last
        }

        override fun cloneData(): InvoiceSearchResponse {
            return data.copy(found = ArrayList<InvoiceSearchResponse.Item>()
                .apply {
                    data.found.forEach {
                        add(it.copy())
                    }
                }
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(TemplateCardBinding.inflate(layoutInflater,parent,false).root){}
        }

        override fun getItemCount(): Int {
            return data.found.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val  itemData=
                data.found[position]

            TemplateCardBinding.bind(holder.itemView)
                .apply {
                    containerVertical.removeAllViews()

                    arrayOf(
                        Pair(arrayOf("name"),"Накладная "),
                        Pair(arrayOf("partial"),"Частичная отгрузка "),
                        Pair(arrayOf<Any>("name","","",LinearLayout.VERTICAL),"Наименование изделия "),
                        //Pair(arrayOf("number"),"Номер "),
                        Pair(arrayOf("collected"),"Собрана "),
                    ).forEach{
                        containerVertical.addView(
                            TemplatePresenterBinding.inflate(layoutInflater,this.containerVertical,false)
                                .apply {
                                    setAttribute(it,itemData)
                                }
                                .root
                        )
                    }

                    containerVertical.setOnClickListener {
                        invoicesViewModel.mainActivityRouter.navigate(
                            InvoiceFragmentLines::class.java,
                            Bundle().apply {
                                putSerializable(InvoiceFragmentLines.PARAMS_INVOICE_ID,itemData.id)
                                putSerializable(InvoiceFragmentLines.PARAMS1_INVOICE_NAME,itemData.number)
                                putSerializable(InvoiceFragmentLines.PARAMS2_COLLECTED,itemData.collected)
                            }
                        )
                    }

                }

        }

        fun updateItem(id: String, collected: Boolean) {
            val dataOld=cloneData()
            data.found.find { it.id==id }?.let {
                it.collected=collected
            }
            DiffUtil.calculateDiff(getCallback(dataOld),false)
                .dispatchUpdatesTo(this)
        }


    }

    sealed class InvoicesFragmentState<out T : Any>{
        data class Success<out T : Any>(private var _data:T?): InvoicesFragmentState<T>(){
            val data:T?
                get() {
                    val temp=_data
                    _data=null
                    return temp
                }
        }
        data class Error(var _exception: Throwable?) : InvoicesFragmentState<Nothing>(){
            val exception: Throwable?
                get() {
                    val temp=_exception
                    _exception=null
                    return temp
                }
        }
    }

    class InvoicesViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref
    ) : BaseViewModel() {

        companion object {
            fun getInstance(context: Context): InvoicesViewModel {
                return InvoicesViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }

        val invoicesFragmentEmpty=
            MutableLiveData<Int>()
        val invoicesFragmentReady=
            MutableLiveData<Int>()

        val invoicesFragmentTitle=
            MutableLiveData<String?>()
        val invoicesFragmentSubTitle=
            MutableLiveData<String?>()
        val invoicesFragmentState=
            MutableLiveData<InvoicesFragmentState<Any>>()

        fun invoiceSearch(parseArguments: String,last:String) {
            ioCoroutineScope.launch {
                invoicesFragmentState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InvoicesFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else ->when (val result = apiPantes.invoiceSearch(
                            token,
                            parseArguments,
                            last
                        )) {
                            is ApiState.Success ->
                                InvoicesFragmentState.Success(result.data)

                            is ApiState.Error ->
                                InvoicesFragmentState.Error(result.exception)
                        }
                    }
                )
            }
        }

        init {
//            ioCoroutineScope.launch {
//                when (val token = loginRepository.user?.token) {
//                    null -> InvoicesFragmentState.Error(ErrorsFragment.nonFatalExceptionToken)
//                    else ->when (val result = apiPantes.log(
//                        token,
//                        "test_log"
//                    )) {
//                        is ApiState.Success ->{
//                            result
//                        }
//
//
//                        is ApiState.Error ->{
//                            result
//                        }
//
//                    }
//                }
//            }
        }

    }

}