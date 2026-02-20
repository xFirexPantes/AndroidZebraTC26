package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.scanner.R
import com.example.scanner.app.templateAttributeDataTextView
import com.example.scanner.app.templateCheckBoxCheckBox
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplateRecyclerBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateResultEmptyBinding
import com.example.scanner.models.IssuanceIssueResponse
import com.example.scanner.models.LinesSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.base.NonFatalExceptionShowDialogMessage
import com.example.scanner.ui.base.NonFatalExceptionShowToaste
import com.example.scanner.ui.dialogs.IssuanceIssueDialog
import com.example.scanner.ui.dialogs.IssuanceIssueDialogFormState
import com.example.scanner.ui.dialogs.IssuanceIssueDialogViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch

class InvoiceFragmentLines : BaseFragment(),SearchView.OnQueryTextListener {

    companion object{
        const val PARAMS_INVOICE_ID="params"
        const val PARAMS1_INVOICE_NAME="params1"
        const val PARAMS2_COLLECTED="params2"
        const val SAVE_SEARCH="search"
    }

    private val invoiceLinesViewModel: InvoiceLinesViewModel by viewModels { viewModelFactory  }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val issuanceIssueDialogViewModel: IssuanceIssueDialogViewModel by viewModels{ viewModelFactory  }
    private val adapterLines=AdapterLines()
    private var lastStringScanResult=Other.SAD<String>(null)
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        scanViewModelReference=scanViewModel

        invoiceLinesViewModel.pref.orderLines=
            getString(R.string.order_by_not_collected)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        return TemplateFragmentBinding.inflate(inflater)
            .apply {

                toolbar.apply {

                    setNavigationOnClickListener {
                        invoiceLinesViewModel.filterSearchView.value
                            ?.let {
                                invoiceLinesViewModel.filterSearchView.postValue(null)
                            }
                            ?:run {
                                findNavController().navigateUp()
                            }
                    }

                    //region title
                    invoiceLinesViewModel.invoiceFragmentLinesTitle
                        .observe(viewLifecycleOwner){title=it}
                    invoiceLinesViewModel.invoiceFragmentLinesTitle
                        .postValue(
                            getString(
                                R.string.format_title_invoice,
                                getArgument(PARAMS1_INVOICE_NAME)
                            )
                        )
                    //endregion
                    //region subtitle
                    invoiceLinesViewModel.invoiceFragmentLinesSubTitle
                        .observe(viewLifecycleOwner){subtitle=it}
                    //endregion
                    //region invoiceInfo
                    addView(
                        TemplateIconBinding.inflate(inflater)
                            .apply {
                                src=ResourcesCompat.getDrawable(resources,R.drawable.ic_info,null)
                                image.setImageResource(R.drawable.ic_question)
                                setOnClickListener {
                                    invoiceLinesViewModel.mainActivityRouter
                                        .navigate(
                                            InvoiceFragmentInfo::class.java,
                                            Bundle().apply {
                                                putSerializable(
                                                    InvoiceFragmentInfo.PARAMS_INVOICE_ID,
                                                    getArgument(PARAMS_INVOICE_ID)
                                                )
                                            })
                                }
                            }
                            .root
                    )
                    //endregion
                    //region SearchView
                    addView(
                        SearchView(requireContext())
                            .apply {
                                invoiceLinesViewModel.visibleSearchView.observe(viewLifecycleOwner){
                                    visibility= it
                                }
                                invoiceLinesViewModel.visibleSearchView.value= View.GONE
                                val searchView=this
                                invoiceLinesViewModel.filterSearchView
                                    .observe(viewLifecycleOwner){queryStr->
                                        queryStr
                                            ?.let {
                                                searchView.onActionViewExpanded()
                                                if (queryStr.toString()!=searchView.query?.toString()) {
                                                    searchView.setQuery(queryStr, true)
                                                    invoiceLinesViewModel.invoiceLinesFormState
                                                        .postValue(InvoiceLinesFormState.RequestLines)
                                                }
                                            }
                                            ?:run {
                                                searchView.onActionViewCollapsed()
                                                invoiceLinesViewModel.invoiceLinesFormState
                                                    .postValue(InvoiceLinesFormState.RequestLines)
                                            }
                                    }
                                setOnQueryTextListener(this@InvoiceFragmentLines)
                                findViewById<View>(androidx.appcompat.R.id.search_close_btn)
                                    .setOnClickListener {
                                        invoiceLinesViewModel.filterSearchView
                                            .postValue(null)

                                    }
                            }
                    )
                    //endregion
                    //region iconManual
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,iconContainer,false)
                            .apply {
                                src=ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)

                                invoiceLinesViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
                                    root.visibility=it
                                }
                                image.setImageResource(R.drawable.ic_search)
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
                                invoiceLinesViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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

                //region Sort and Total
                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply {
                            root.setPadding(0,0,0,0)

                            containerHorizon.gravity=Gravity.CENTER_VERTICAL

                            //region sort
                            containerHorizon.addView(
                                TemplatePresenterBinding.inflate(inflater)
                                    .apply {
                                        templateAttributeDataTextView.apply {
                                            fun setTextSort(){
                                                text=getString(
                                                    when(invoiceLinesViewModel.pref.orderLines){
                                                        getString(R.string.order_by_place)->R.string.order_by_place
                                                        getString(R.string.order_by_name)->R.string.order_by_name
                                                        getString(R.string.order_by_name_desc)->R.string.order_by_name_desc
                                                        getString(R.string.order_by_collected)->R.string.order_by_collected
                                                        getString(R.string.order_by_not_collected)->R.string.order_by_not_collected
                                                        else->R.string.order_by_name
                                                    }
                                                )
                                            }
                                            setTextSort()
                                            setOnClickListener {
                                                PopupMenu(requireContext(), templateAttributeDataTextView)
                                                    .apply {
                                                        setForceShowIcon(true)
                                                        setOnMenuItemClickListener { item->
                                                            when(item.itemId){
                                                                R.id.order_by_place->{
                                                                    invoiceLinesViewModel.pref.orderLines=
                                                                        getString(R.string.order_by_place)
                                                                    setTextSort()
                                                                    adapterLines.setContent(
                                                                        LinesSearchResponse())
                                                                    invoiceLinesViewModel.invoiceLinesFormState.value=
                                                                        InvoiceLinesFormState.RequestLines
                                                                }
                                                                R.id.order_by_name->{
                                                                    invoiceLinesViewModel.pref.orderLines=
                                                                        getString(R.string.order_by_name)
                                                                    setTextSort()
                                                                    adapterLines.setContent(
                                                                        LinesSearchResponse())
                                                                    invoiceLinesViewModel.invoiceLinesFormState.value=
                                                                        InvoiceLinesFormState.RequestLines

                                                                }
                                                                R.id.order_by_name_desc->{
                                                                    invoiceLinesViewModel.pref.orderLines=
                                                                        getString(R.string.order_by_name_desc)
                                                                    setTextSort()
                                                                    adapterLines.setContent(
                                                                        LinesSearchResponse())
                                                                    invoiceLinesViewModel.invoiceLinesFormState.value=
                                                                        InvoiceLinesFormState.RequestLines

                                                                }
                                                                R.id.order_by_collected->{
                                                                    invoiceLinesViewModel.pref.orderLines=
                                                                        getString(R.string.order_by_collected)
                                                                    setTextSort()
                                                                    adapterLines.setContent(
                                                                        LinesSearchResponse())
                                                                    invoiceLinesViewModel.invoiceLinesFormState.value=
                                                                        InvoiceLinesFormState.RequestLines
                                                                }
                                                                R.id.order_by_not_collected->{
                                                                    invoiceLinesViewModel.pref.orderLines=
                                                                        getString(R.string.order_by_not_collected)
                                                                    setTextSort()
                                                                    adapterLines.setContent(
                                                                        LinesSearchResponse())
                                                                    invoiceLinesViewModel.invoiceLinesFormState.value=
                                                                        InvoiceLinesFormState.RequestLines

                                                                }
                                                            }

                                                            true }
                                                        inflate(R.menu.actions_order)
                                                        show()
                                                    }
                                            }
                                        }
                                    }
                                    .root
                            )
                            //endregion

                            //region total
                            containerHorizon.addView(
                                TextView(requireContext())
                                    .apply {
                                        layoutParams=
                                            LinearLayoutCompat.LayoutParams(containerHorizon.layoutParams)
                                            .apply {
                                                weight=1f
                                                width=LinearLayout.LayoutParams.MATCH_PARENT
                                                height=LinearLayout.LayoutParams.WRAP_CONTENT
                                            }

                                        gravity=
                                            Gravity.END

                                        invoiceLinesViewModel.invoiceFragmentLinesTotal
                                            .observe(viewLifecycleOwner)
                                            {
                                                this.text=it
                                            }

                                    }
                            )
                            //endregion
                            divider.visibility= View.VISIBLE
                        }
                        .root
                )
                //endregion

                //region recyclerLines
                root.addView(
                    TemplateRecyclerBinding.inflate(inflater,root,false)
                        .apply {
                            recycler.adapter=adapterLines
                            recycler.layoutManager =
                                object : LinearLayoutManager(requireContext())
                                {
//                                    override fun onScrollStateChanged(state: Int) {
//                                        super.onScrollStateChanged(state)
//                                        if (findLastVisibleItemPosition()+1 == adapterLines.itemCount) {
//                                            invoiceLinesViewModel.invoiceLinesFormState.value=
//                                                InvoiceLinesFormState.RequestLines
//                                        }
//                                    }

                                }

                            //region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .apply {
                                        invoiceLinesViewModel.invoiceFragmentLinesEmpty
                                            .observe(viewLifecycleOwner){
                                                root.visibility=it
                                            }
                                        invoiceLinesViewModel.invoiceFragmentLinesEmpty
                                            .postValue(View.GONE)
                                    }
                                    .root
                            )
                            //endregion

                            recyclerView=recycler

                        }
                        .root
                )
                //endregion

            }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invoiceLinesViewModel.invoiceLinesFormState.observe(viewLifecycleOwner){

            when(val state=it){
                is InvoiceLinesFormState.SetupView->{
                    if (adapterLines.isResetContent){
                        invoiceLinesViewModel.invoiceLinesFormState
                            .postValue(
                                InvoiceLinesFormState.RequestLines
                            )
                    }
                }
                is InvoiceLinesFormState.Success -> {
                    val data = (state.data as LinesSearchResponse)
                    adapterLines.setContent(data)
                }
                is InvoiceLinesFormState.Error -> {
                    invoiceLinesViewModel.mainActivityRouter.navigate(
                        ErrorsFragment::class.java,
                        Bundle().apply {
                            putSerializable(
                                ErrorsFragment.PARAM,
                                state.exception
                            )
                        })
                }
                is InvoiceLinesFormState.RequestLines -> {
                    adapterLines.resetContent()
                    invoiceLinesViewModel.requestLines(
                        invoice =getArgument(PARAMS_INVOICE_ID),
                        order = when (invoiceLinesViewModel.pref.orderLines) {
                            getString(R.string.order_by_name) -> "name"
                            getString(R.string.order_by_name_desc) -> "-name"
                            getString(R.string.order_by_not_collected) -> "collected"
                            getString(R.string.order_by_collected) -> "-collected"
                            else -> "place"
                        },
                        last = adapterLines.last.toString(),
                        query = invoiceLinesViewModel.filterSearchView.value?:""

                    )
                }
                is InvoiceLinesFormState.Stub->{}
            }

            when{
                adapterLines.isResetContent->{
                    invoiceLinesViewModel.invoiceFragmentLinesEmpty
                        .postValue(View.GONE)
                    invoiceLinesViewModel.visibleSearchView
                        .postValue(View.GONE)
                }
                adapterLines.itemCount==0 ->{

                    invoiceLinesViewModel.invoiceFragmentLinesEmpty
                        .postValue(View.VISIBLE)

                    invoiceLinesViewModel.visibleSearchView
                        .postValue(
                            if (invoiceLinesViewModel.filterSearchView.value==null)
                                View.GONE
                            else
                                View.VISIBLE
                        )
                }
                else->{
                    invoiceLinesViewModel.invoiceFragmentLinesTotal
                        .postValue(
                            StringBuilder()
                                .append("Собрано строк: ")
                                .append(adapterLines.data.collected)
                                .append("/")
                                .append(adapterLines.data.total)
                                .toString()
                        )

                    invoiceLinesViewModel.invoiceFragmentLinesEmpty
                        .postValue(View.GONE)

                    invoiceLinesViewModel.visibleSearchView
                        .postValue(
                            if (invoiceLinesViewModel.filterSearchView.value==null)
                                View.GONE
                            else
                                View.VISIBLE
                        )

                }
            }

        }

        issuanceIssueDialogViewModel.issuanceIssueDialogFormState.observe(viewLifecycleOwner){
            when(val state=it){
                is IssuanceIssueDialogFormState.SuccessIssuanceIssue->{
                    state.issuanceIssueResponseSAD.data?.let {issuanceIssueResponse->
                        issuanceIssueResponse.line?.number?.let {number->
                            //ставим фильтр по строке
                            invoiceLinesViewModel.filterSearchView.postValue(number)
                            //если в строке есть не собранные катушки - открыть строку
                            if(issuanceIssueResponse.line.coils.find { coil -> !coil.collected }!=null){
                                invoiceLinesViewModel.mainActivityRouter.navigate(
                                    InvoiceFragmentInfoLine::class.java,
                                    Bundle().apply {
                                        putSerializable(
                                            InvoiceFragmentInfoLine.PARAM_INVOICE_ID,
                                            getArgument(PARAMS_INVOICE_ID)
                                        )
                                        putSerializable(
                                            InvoiceFragmentInfoLine.PARAM_INVOICE_NAME,
                                            getArgument(PARAMS1_INVOICE_NAME)
                                        )
                                        putSerializable(
                                            InvoiceFragmentInfoLine.PARAM_LINE_ID,
                                            issuanceIssueResponse.line.id.toInt()
                                        )
                                        putSerializable(
                                            InvoiceFragmentInfoLine.PARAM_LINE_COLLECTED,
                                            false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                is IssuanceIssueDialogFormState.Error->{
                    when(val exception=state.exceptionSAD.data){
                        is NonFatalExceptionShowDialogMessage->{
                            lastStringScanResult.data
                                ?.let { stringScanResult->

                                    invoiceLinesViewModel.filterSearchView
                                        .postValue(stringScanResult)

                                    issuanceIssueDialogViewModel.mainActivityRouter.navigate(
                                        ErrorsFragment::class.java,
                                        Bundle().apply {
                                            putSerializable(
                                                ErrorsFragment.PARAM,
                                                NonFatalExceptionShowToaste(exception.message))
                                        }
                                    )

                                }
                        }
                        null->{

                        }
                        else -> {
                            issuanceIssueDialogViewModel.mainActivityRouter.navigate(
                                ErrorsFragment::class.java,
                                Bundle().apply {
                                    putSerializable(ErrorsFragment.PARAM,exception) }
                            )
                        }

                    }

                }
                else -> {}
            }
        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
            when(val state=it){
                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                    state.stringScanResult?.let {stringScanResult->
                        lastStringScanResult.data=stringScanResult
                        issuanceIssueDialogViewModel.requestIssuanceIssue(
                            coil = stringScanResult,
                            invoice = getArgument(PARAMS_INVOICE_ID)
                        )
                    }
                }
                else->{}
            }
        }

        mutableOnFragmentDetached.observe(viewLifecycleOwner){
                when (val f = it.data) {
                    is IssuanceIssueDialog -> {

                        invoiceLinesViewModel.invoiceLinesFormState
                            .postValue(
                                InvoiceLinesFormState.RequestLines
                            )

                    }

                    is InvoiceFragmentInfoLine -> {
                        f.getArgument<IssuanceIssueResponse?>(InvoiceFragmentInfoLine.PARAM3_RESULT)?.let {
                            invoiceLinesViewModel.invoiceLinesFormState
                                .postValue(
                                    InvoiceLinesFormState.RequestLines
                                )
                        }
                    }
                }
        }

        invoiceLinesViewModel.invoiceLinesFormState
            .postValue(InvoiceLinesFormState.SetupView)


    }

    override fun onResume() {
        super.onResume()
        adapterLines.onResume()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        invoiceLinesViewModel.filterSearchView
            .postValue(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply { putString(SAVE_SEARCH,invoiceLinesViewModel.filterSearchView.value) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getString(SAVE_SEARCH)?.let {
            invoiceLinesViewModel.filterSearchView
                .postValue(it)
        }
    }

    inner class AdapterLines : BaseRecyclerAdapter<LinesSearchResponse>(LinesSearchResponse()) {

        private val contentItems=arrayOf<Pair<Array<Any>, String>>(
            Pair(arrayOf("name", "", "", "", "", true), ""),
            Pair(arrayOf("number"), "#компонента "),
            Pair(arrayOf("nominal"), "Номинал "),
            Pair(arrayOf("coil"), "Катушка "),
            Pair(arrayOf("horizontalDivider"), ""),
            Pair(arrayOf("quantity"), "Количество "),
            Pair(arrayOf("amount"), "На складе "),
            Pair(arrayOf("separate"), "Отдельно "),)

        override fun getCallback(dataOld: LinesSearchResponse?): DiffUtil.Callback {
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
                    return (data.found[newItemPosition].id==
                            dataOld?.found?.get(oldItemPosition)?.id)
                            &&
                            (data.found[newItemPosition].collected==
                                    dataOld.found[oldItemPosition].collected)
                            &&
                            (data.found[newItemPosition].isDirty==
                                    dataOld.found[oldItemPosition].isDirty)

                }
            }
        }

        override fun appendData(dataNew: LinesSearchResponse) {
            if (dataNew.total>0) {
                data.last = dataNew.last
                data.found.addAll(dataNew.found)
            }

        }

        override fun getLastId(): Any {

            return if(isResetContent) "" else data.last
        }

        override fun cloneData(): LinesSearchResponse {
            return data.copy(
                found = ArrayList<LinesSearchResponse.Item>().apply {
                    data.found.forEach { add(it.copy()) }
                },
                last=data.last,
                total = data.total,
                collected = data.collected
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object :ViewHolder(TemplateCardBinding.inflate(layoutInflater,parent,false)
                .root.apply {
                    //addView(TemplateProgressTextBinding.inflate(layoutInflater,this,false).root)
                }

            ){

            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            fun postSetAttribute(
                pair: Pair<Array<Any>, String>,
                templatePresenterBinding: TemplatePresenterBinding,
                itemData: LinesSearchResponse.Item
            ){
                when (pair.first[0]) {
                    "name" -> {
                        templatePresenterBinding.templateCheckBoxCheckBox
                            .apply {
                                isChecked = itemData.collected
                                setOnClickListener {
                                    isChecked =
                                        itemData.collected

                                    IssuanceIssueDialog()
                                        .apply {
                                            arguments =
                                                Bundle().apply {
                                                    putSerializable(
                                                        IssuanceIssueDialog.PARAM_INVOICE_ID,
                                                        this@InvoiceFragmentLines.getArgument(
                                                            PARAMS_INVOICE_ID
                                                        )
                                                    )
                                                    putSerializable(
                                                        IssuanceIssueDialog.PARAM_INVOICE_NAME,
                                                        this@InvoiceFragmentLines.getArgument(
                                                            PARAMS1_INVOICE_NAME
                                                        )
                                                    )
                                                    putSerializable(
                                                        IssuanceIssueDialog.PARAM_LINE_ID,
                                                        itemData.id
                                                    )
                                                    putSerializable(
                                                        IssuanceIssueDialog.PARAM_LINE_NAME,
                                                        itemData.name
                                                    )
                                                    putSerializable(
                                                        IssuanceIssueDialog.PARAM_COLLECTED,
                                                        itemData.collected
                                                    )
                                                }
                                        }
                                        .show(
                                            childFragmentManager,
                                            IssuanceIssueDialog::class.java.name
                                        )

                                }

                            }
                    }
                }
            }

            val itemData: LinesSearchResponse.Item =
                data.found[position]




            val itemBinding =
                TemplateCardBinding.bind(holder.itemView)
                    .apply {
                        root.layoutParams.height= FrameLayout.LayoutParams.WRAP_CONTENT
                        root.requestLayout()
                        if (holder.itemView.tag==null) {
                            containerVertical.removeAllViews()
                        }
                    }


            @Suppress("UNCHECKED_CAST")
            val existingAttribute=
                when(holder.itemView.tag){
                    null->HashMap()
                    else -> holder.itemView.tag as HashMap<Pair<Array<Any>, String>, TemplatePresenterBinding>
                }
            contentItems.forEachIndexed { index, it ->
                existingAttribute[it]
                    ?.apply {
                        setAttribute(it, itemData)
                        postSetAttribute(it, this, itemData)

                        // Окрашиваем только первое поле (индекс 0)
                        if (index == 0 && itemData.isused) {
                            this.root.setBackgroundColor(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.yellow
                                )
                            )
                        } else {
                            this.root.setBackgroundColor(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.default_background
                                )
                            )
                        }
                    }
                    ?: run {
                        itemBinding.containerVertical.addView(
                            TemplatePresenterBinding.inflate(
                                layoutInflater,
                                itemBinding.containerVertical,
                                false
                            ).apply {
                                existingAttribute[it] = this
                                setAttribute(it, itemData)
                                postSetAttribute(it, this, itemData)

                                // Окрашиваем только первое поле (индекс 0)
                                if (index == 0 && itemData.isused) {
                                    this.root.setBackgroundColor(
                                        ContextCompat.getColor(
                                            holder.itemView.context,
                                            R.color.yellow
                                        )
                                    )
                                } else {
                                    this.root.setBackgroundColor(
                                        ContextCompat.getColor(
                                            holder.itemView.context,
                                            R.color.default_background
                                        )
                                    )
                                }
                            }
                                .root
                        )
                    }
            }

            holder.itemView.tag=existingAttribute
            itemBinding.containerVertical.setOnClickListener {
                invoiceLinesViewModel.mainActivityRouter.navigate(
                    InvoiceFragmentInfoLine::class.java,
                    Bundle().apply {
                        putSerializable(
                            InvoiceFragmentInfoLine.PARAM_INVOICE_ID,
                            getArgument(PARAMS_INVOICE_ID)
                        )
                        putSerializable(
                            InvoiceFragmentInfoLine.PARAM_INVOICE_NAME,
                            getArgument(PARAMS1_INVOICE_NAME)
                        )
                        putSerializable(
                            InvoiceFragmentInfoLine.PARAM_LINE_ID,
                            itemData.id
                        )
                        putSerializable(
                            InvoiceFragmentInfoLine.PARAM_LINE_COLLECTED,
                            itemData.collected
                        )
                    }
                )
            }

        }

        override fun getItemCount(): Int {
            return data.found.size
        }

        fun onResume() {
            if (!isResetContent){
                val temp=cloneData()
                adapterLines.setContent(temp)
            }

        }

//        override fun setContent(dataNew: LinesSearchResponse) {
//            //region set dirty
//            val rnd=Math.random()
//            data.found.forEach { it.isDirty=rnd }
//            //endregion
//            super.setContent(dataNew)
//        }

    }

    sealed class InvoiceLinesFormState<out T : Any> {
        data class Success<out T : Any>(val data:T?): InvoiceLinesFormState<T>()
        data class Error(val exception: Throwable) : InvoiceLinesFormState<Nothing>()
        data object RequestLines: InvoiceLinesFormState<Nothing>()
        data object SetupView: InvoiceLinesFormState<Nothing>()
        data object Stub: InvoiceLinesFormState<Nothing>()

    }

    class InvoiceLinesViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref
    ): BaseViewModel()  {

        val invoiceFragmentLinesTitle=
            MutableLiveData<String?>()
        val invoiceFragmentLinesSubTitle=
            MutableLiveData<String?>()
        val filterSearchView=
            MutableLiveData<String?>()
        val visibleSearchView=
            MutableLiveData<Int>()
        val invoiceFragmentLinesTotal=
            MutableLiveData<String?>()
        val invoiceFragmentLinesEmpty=
            MutableLiveData<Int>()
//        val iconResetVisibleView=
//            MutableLiveData<Int>()


        val invoiceLinesFormState=
            MutableLiveData<InvoiceLinesFormState<*>>()

        fun requestLines(invoice: String, order:String, last:String,query:String) {
            Other.getInstanceSingleton().ioCoroutineScope.launch {
                invoiceLinesFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InvoiceLinesFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> {
                            when (val result = apiPantes.linesSearch(
                                token = token,
                                invoice = invoice,
                                order = order,
                                last = last,
                                query=query
                            )) {
                                is ApiPantes.ApiState.Success ->
                                    InvoiceLinesFormState.Success(result.data)

                                is ApiPantes.ApiState.Error ->
                                    InvoiceLinesFormState.Error(result.exception)

                            }
                        }
                    }
                )
            }
        }

        companion object {

            fun getInstance(context: Context): InvoiceLinesViewModel {
                return InvoiceLinesViewModel(
                            ApiPantes.getInstanceSingleton(),
                            LoginRepository.getInstanceSingleton(context),
                            Pref.getInstanceSingleton(context)
                        )
            }
        }

    }

}