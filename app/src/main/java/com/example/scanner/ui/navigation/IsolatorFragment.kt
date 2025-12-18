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
import com.example.scanner.databinding.TemplateButton3Binding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplateRecyclerBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateResultEmptyBinding
import com.example.scanner.databinding.TemplateScannerReadyBinding
import com.example.scanner.models.IsolatorSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Pref
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class IsolatorFragment : BaseFragment() {

    companion object{
        const val PARAM="param"
    }

    private val isolatorViewModel: IsolatorViewModel by viewModels { viewModelFactory  }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterIsolator=AdapterIsolator()

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
                    setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }
                    //region iconManual
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,iconContainer,false)
                            .apply {
                                src= ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)
                                isolatorViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
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
                                isolatorViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
                                    src=ResourcesCompat.getDrawable(resources,it,null)
                                }

                                image.setOnClickListener {
                                    scanViewModel.scannerApi.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )
                    //endregion
                    //region subtitle
                    isolatorViewModel.isolatorFragmentSubtitle
                        .observe(viewLifecycleOwner){
                            toolbar.subtitle=it
                        }
                    //endregion
                    //region title
                    isolatorViewModel.isolatorFragmentTitle
                        .observe(viewLifecycleOwner){
                            toolbar.title=it
                        }
                    //endregion
                }

                //region recycler
                root.addView(
                    TemplateRecyclerBinding.inflate(inflater,root,false)
                        .apply {
                            recycler.layoutManager =
                                object : LinearLayoutManager(requireContext()) {
                                    override fun onScrollStateChanged(state: Int) {
                                        super.onScrollStateChanged(state)

                                        if (findLastVisibleItemPosition()+1 == adapterIsolator.itemCount) {
                                            isolatorViewModel.isolatorSearch(
                                                getArgument(PARAM),
                                                adapterIsolator.last.toString())
                                        }
                                    }
                                }

                            recycler.adapter=
                                adapterIsolator

                            //region empty recycler
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .root
                                    .apply {
                                        isolatorViewModel.isolatorFragmentEmpty
                                            .observe(viewLifecycleOwner){
                                                this.visibility=it
                                            }
                                        isolatorViewModel.isolatorFragmentEmpty
                                            .postValue(View.GONE)
                                    }
                            )
                            //endregion

                            //region ready
                            containerContent.addView(
                                TemplateScannerReadyBinding.inflate(inflater,containerContent,false)
                                    .apply {
                                        icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_qr,null)
                                        title="Сканируйте код элемента'"
                                        isolatorViewModel.isolatorFragmentReady
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
                is IsolatorFragmentIsolate->{
                    adapterIsolator.updateItem(
                        f.getArgument(IsolatorFragmentIsolate.PARAM)
                    )
                }
                is IsolatorFragmentMinus->{
                    f.getArgument<IsolatorSearchResponse.Item?>(IsolatorFragmentMinus.PARAM_RESULT)
                        ?.let {
                            adapterIsolator.updateItem(it)
                        }
                }
            }
        }

        isolatorViewModel.isolatorFragmentFormState.observe(viewLifecycleOwner){

            when(val state=it){
                is IsolatorFragmentFormState.Error -> {
                    findNavController().navigateUp()

                    isolatorViewModel.mainActivityRouter.navigate(
                        ErrorsFragment::class.java,
                        Bundle()
                            .apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    state.exception
                                )
                            }
                    )
                }
                is IsolatorFragmentFormState.Success ->{

                    val isolatorSearchResponse=state.data as IsolatorSearchResponse

                    if (adapterIsolator.isResetContent){
                        adapterIsolator.setContent(isolatorSearchResponse)
                        isolatorViewModel.isolatorFragmentTitle.postValue(
                            getString(R.string.format_title,"${isolatorSearchResponse.total}"))

                        isolatorViewModel.isolatorFragmentEmpty.postValue(
                            if (isolatorSearchResponse.found.isEmpty())
                                View.VISIBLE
                            else
                                View.GONE
                        )

                    }
                    else{
                        adapterIsolator.appendContent(isolatorSearchResponse)
                    }

                }
                is IsolatorFragmentFormState.Idle->{}
            }

            if (isolatorViewModel.isolatorFragmentFormState.value!=IsolatorFragmentFormState.Idle) {
                isolatorViewModel.isolatorFragmentFormState.postValue(
                    IsolatorFragmentFormState.Idle
                )
            }

        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
            when(val scanState=it){
                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                    scanState.stringScanResult?.let {stringScanResult->

                        requireArguments().putSerializable(
                            PARAM,
                            stringScanResult
                        )
                        adapterIsolator
                            .resetContent()
                        isolatorViewModel
                            .isolatorSearch(stringScanResult,"")
                        isolatorViewModel.isolatorFragmentReady
                            .postValue(View.GONE)
                        isolatorViewModel.isolatorFragmentSubtitle
                            .postValue(getString(R.string.format_subtitle,stringScanResult))

                    }
                }
                else->{}
            }
        }

        if (isolatorViewModel.isolatorFragmentFormState.value==null){
            isolatorViewModel.isolatorFragmentTitle
                .postValue(getString(R.string.button_isolator))
            if (getArgument<String?>(PARAM).isNullOrEmpty()){
                isolatorViewModel.isolatorFragmentReady
                    .postValue(View.VISIBLE)
            }else{
                isolatorViewModel.isolatorFragmentReady
                    .postValue(View.GONE)
                isolatorViewModel.isolatorSearch(getArgument(PARAM),"")

            }
        }
        else{
            isolatorViewModel.isolatorFragmentReady
                .postValue(View.GONE)
        }

    }

    inner class AdapterIsolator(): BaseRecyclerAdapter<IsolatorSearchResponse>(IsolatorSearchResponse()){

        override fun getCallback(dataOld: IsolatorSearchResponse?): DiffUtil.Callback {
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
                            &&
                            data.found[newItemPosition].amount==
                            dataOld.found[oldItemPosition].amount
                            &&
                            data.found[newItemPosition].isolated==
                            dataOld.found[oldItemPosition].isolated
                            &&
                            data.found[newItemPosition].rack==
                            dataOld.found[oldItemPosition].rack
                            &&
                            data.found[newItemPosition].name==
                            dataOld.found[oldItemPosition].name
                            &&
                            data.found[newItemPosition].nominal==
                            dataOld.found[oldItemPosition].nominal
                            &&
                            data.found[newItemPosition].coil==
                            dataOld.found[oldItemPosition].coil
                }
            }
        }

        override fun appendData(dataNew: IsolatorSearchResponse) {
            if (dataNew.found.isNotEmpty()){
                data.last=dataNew.last
                data.found.addAll(dataNew.found)
            }
        }

        override fun getLastId(): Any {
            return data.last
        }

        override fun cloneData(): IsolatorSearchResponse {
            return data.copy(
                found = ArrayList<IsolatorSearchResponse.Item>().apply{
                    data.found.forEach {
                        add(it.copy())
                    }
                }
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object :ViewHolder(TemplateCardBinding.inflate(layoutInflater,parent,false).root){}
        }

        override fun getItemCount(): Int {
            return data.found.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemBinding=TemplateCardBinding.bind(holder.itemView)
            val itemData=data.found[position]
            itemBinding.containerVertical.removeAllViews()
            itemBinding.containerHorizon.removeAllViews()

            //region content
            arrayOf(
                Pair(arrayOf("name"),""),
                Pair(arrayOf("id"),"# компонента "),
                Pair(arrayOf("nominal"),"Номинал "),
                Pair(arrayOf("coil"),"Катушка "),
                Pair(arrayOf("horizontalDivider"),""),
                Pair(arrayOf("amount"),"На складе "),
                Pair(arrayOf("isolated"),"В изоляторе "),
            )
                .forEach {pair->
                    itemBinding.containerVertical.addView(
                        TemplatePresenterBinding.inflate(layoutInflater,itemBinding.containerVertical,false)
                            .apply {
                                setAttribute(pair,itemData)
                            }
                            .root
                    )
                }
            //endregion

            //region buttons
            itemBinding.containerHorizon
                .apply {
                    addView(
                        TemplateButton3Binding.inflate(layoutInflater,itemBinding.containerHorizon,false)
                            .apply {
                                (layoutParams as LinearLayout.LayoutParams).weight=1f
                                title="ИЗОЛИРОВАТЬ"
                                    root.setOnClickListener {
                                        isolatorViewModel.mainActivityRouter
                                            .navigate(
                                                IsolatorFragmentIsolate::class.java,
                                                Bundle().apply {
                                                    putSerializable(IsolatorFragmentIsolate.PARAM,itemData)
                                                }
                                            )
                                    }
                            }
                            .root
                    )
                    addView(
                        TemplateButton3Binding.inflate(layoutInflater,itemBinding.containerHorizon,false)
                            .apply {
                                (layoutParams as LinearLayout.LayoutParams).weight=1f
                                title="ОТМИНУСОВАТЬ"
                                root.setOnClickListener {
                                    isolatorViewModel.mainActivityRouter
                                        .navigate(
                                            IsolatorFragmentMinus::class.java,
                                            Bundle().apply { putSerializable(IsolatorFragmentMinus.PARAM, itemData) }
                                        )
                                }
                            }
                            .root
                    )

                        }
            //endregion

        }

        fun updateItem(value: IsolatorSearchResponse.Item) {

            val dataOld = cloneData()

            data.found.find { it.id == value.id }?.apply {
                amount = value.amount
                cell = value.cell
                coil = value.coil
                isolated = value.isolated
                name = value.name
                nominal = value.nominal
                rack = value.rack
            }

            DiffUtil.calculateDiff(getCallback(dataOld), false).dispatchUpdatesTo(this)
        }


    }

    sealed class IsolatorFragmentFormState<out T:Any> {
        data class Error(var exception: Throwable): IsolatorFragmentFormState<Nothing>()
        data class Success<out T : Any>(val data:T): IsolatorFragmentFormState<T>()
        data object Idle: IsolatorFragmentFormState<Nothing>()
    }

    class IsolatorViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref
    ) : BaseViewModel() {

        companion object {
            private var _isolatorViewModel: WeakReference<IsolatorViewModel>? = null
            fun getInstance(context: Context): IsolatorViewModel {
            return _isolatorViewModel
                ?.get()
                ?:run {
                    IsolatorViewModel(
                        ApiPantes.getInstanceSingleton(),
                        LoginRepository.getInstanceSingleton(context),
                        Pref.getInstanceSingleton(context)
                    ).apply {
                        //_isolatorViewModel = WeakReference(this)
                    }
                }
            }
        }

        val isolatorFragmentTitle=
            MutableLiveData<String?>()
        val isolatorFragmentSubtitle=
            MutableLiveData<String?>()
        val isolatorFragmentReady=
            MutableLiveData<Int>()
        val isolatorFragmentEmpty=
            MutableLiveData<Int>()

        val isolatorFragmentFormState=
            MutableLiveData<IsolatorFragmentFormState<*>>()

        fun isolatorSearch(parseArguments: String,last:String) {
            ioCoroutineScope.launch {
                isolatorFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> IsolatorFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.isolatorSearch(
                                token = token,
                                component = "",
                                last=last,
                                query = parseArguments,
                            )){
                                is ApiPantes.ApiState.Success->
                                    IsolatorFragmentFormState.Success(result.data)
                                is ApiPantes.ApiState.Error->
                                    IsolatorFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )
            }
        }

    }
}