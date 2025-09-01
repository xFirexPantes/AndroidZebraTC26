package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.scanner.R
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateRecyclerBinding
import com.example.scanner.databinding.TemplateResultEmptyBinding
import com.example.scanner.databinding.TemplateScannerReadyBinding
import com.example.scanner.models.ComponentsSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch

class ComponentFragment: BaseFragment() {
    companion object{
        const val PARAM="param"
    }


    private val componentsViewModel: ComponentsViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterComponents=
        AdapterComponents()

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
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                src= ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)
                                componentsViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
                                    root.visibility=it
                                }
                                image.setOnClickListener {
                                    scanViewModel.scannerApiEmulator.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )
                    //endregion
                    //region button scan
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                componentsViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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

                componentsViewModel.componentFragmentTitle
                    .observe(viewLifecycleOwner){
                        toolbar.title=it
                    }

                componentsViewModel.componentFragmentSubtitle
                    .observe(viewLifecycleOwner){
                        toolbar.subtitle=it
                    }

                //region recyclerView
                root.addView(
                    TemplateRecyclerBinding.inflate(inflater,root,false)
                        .apply {
                            recycler.adapter=adapterComponents
                            recycler.layoutManager=
                                object : LinearLayoutManager(requireContext()) {
                                    override fun onScrollStateChanged(state: Int) {
                                        super.onScrollStateChanged(state)
                                        if (findLastVisibleItemPosition()+1 == adapterComponents.itemCount) {
                                            componentsViewModel.componentSearch(
                                                getArgument(PARAM),
                                                adapterComponents.last.toString())
                                        }
                                    }
                                }
                            //region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .root
                                    .apply {
                                        componentsViewModel.componentFragmentEmpty
                                            .observe(viewLifecycleOwner){
                                                this.visibility=it
                                            }
                                        componentsViewModel.componentFragmentEmpty
                                            .postValue(
                                                View.GONE
                                            )
                                    }
                            )
                            //endregion

                            //region ready
                            containerContent.addView(
                                TemplateScannerReadyBinding.inflate(inflater,containerContent,false)
                                    .apply {
                                        icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_qr,null)
                                        title="Сканируйте код элемента'"
                                        componentsViewModel.componentsFragmentReady
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

        componentsViewModel.componentsFragmentState.observe(viewLifecycleOwner)
        {
            when(val state=it){
                is ComponentsFragmentState.Error ->{
                    state.exception?.let {exception->
                        findNavController().navigateUp()
                        componentsViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    exception
                                )
                            })
                    }
                }
                is ComponentsFragmentState.Success ->{
                    state.data?.let { componentsSearchResponse->
                        componentsSearchResponse as ComponentsSearchResponse

                        if (adapterComponents.isResetContent){
                            componentsViewModel.componentFragmentTitle
                                .postValue(
                                    getString(R.string.format_title,"${componentsSearchResponse.total}")
                                )
                            componentsViewModel.componentFragmentEmpty
                                .postValue(
                                    if(componentsSearchResponse.found.isEmpty())
                                        View.VISIBLE
                                    else
                                        View.GONE
                                )
                            adapterComponents.setContent(componentsSearchResponse)
                        }
                        else{
                            adapterComponents.appendContent(componentsSearchResponse)
                        }


                    }

                }
                is ComponentsFragmentState.Idle->{
                    componentsViewModel.componentFragmentTitle
                        .postValue(getString(R.string.button_search))

                    componentsViewModel.componentsFragmentReady.postValue(
                        when{
                            getArgument<String?>(PARAM).isNullOrEmpty()-> View.VISIBLE
                            else-> View.GONE
                        }
                    )

                    componentsViewModel.componentFragmentEmpty.postValue(
                        when{
                            !getArgument<String?>(PARAM).isNullOrEmpty()
                                    && adapterComponents.itemCount==0 -> View.VISIBLE
                            else-> View.GONE
                        }
                    )
                }
            }

            if (it!= ComponentsFragmentState.Idle){
                componentsViewModel.componentsFragmentState.postValue(
                    ComponentsFragmentState.Idle
                )
            }
        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner)
        {
            when(val scanState=it){
                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                    scanState.stringScanResult?.let { stringScanResult ->
                        arguments=Bundle().apply {
                            putSerializable(PARAM,stringScanResult)
                        }
                        componentsViewModel.componentFragmentSubtitle
                            .postValue(getString(R.string.format_subtitle,getArgument(PARAM)))
                        adapterComponents.resetContent()
                        componentsViewModel.componentSearch(getArgument(PARAM),"")

                        componentsViewModel.componentsFragmentReady
                            .postValue(
                                View.GONE
                            )

                    }
                }
                else->{}
            }
        }

        if (!getArgument<String?>(PARAM).isNullOrEmpty() && adapterComponents.itemCount==0){
            componentsViewModel.componentSearch(
                getArgument(PARAM),"")
        }

        componentsViewModel.componentsFragmentState.postValue(
            ComponentsFragmentState.Idle
        )

    }

    inner class AdapterComponents(): BaseRecyclerAdapter<ComponentsSearchResponse>(ComponentsSearchResponse()) {
        override fun getCallback(dataOld: ComponentsSearchResponse?): DiffUtil.Callback {
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
                            dataOld?.found[oldItemPosition]?.id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return data.found[newItemPosition].id==
                            dataOld?.found[oldItemPosition]?.id
                }
            }
        }

        override fun appendData(dataNew: ComponentsSearchResponse) {
            if (dataNew.found.isNotEmpty()){
                data.last=dataNew.last
                data.found.addAll(dataNew.found)
            }
        }

        override fun getLastId(): Any {
            return data.last
        }

        override fun cloneData(): ComponentsSearchResponse {
            return data.copy(found = ArrayList(data.found))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object :ViewHolder(TemplateCardBinding.inflate(layoutInflater,parent,false).root){}
//            return object :ViewHolder(FragmentComponentsRecyclerItemBinding
//                .inflate(layoutInflater,parent,false).root){}
        }

        override fun getItemCount(): Int {
            return data.found.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemBinding=TemplateCardBinding.bind(holder.itemView)
            val itemData =
                data.found[position]
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
                //Pair(arrayOf("isolated"),"В изоляторе "),
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

            itemBinding.containerVertical.setOnClickListener {
                    componentsViewModel.mainActivityRouter.navigate(
                        ComponentFragmentInfo::class.java,
                        Bundle().apply {
                            putSerializable(ComponentFragmentInfo.PARAM, itemData.id)
                        }
                    )

            }

//            val bindingItem =
//                FragmentComponentsRecyclerItemBinding.bind(holder.itemView)
//            val itemData =
//                data.found[position]
//            bindingItem.apply {
//                name.text = itemData.name
//                id.text = itemData.id
//                nominal.text = itemData.nominal
//                coil.text = itemData.coil.toRusString()
//                amount.text = itemData.amount.toStringPresent()
//                cases.text = itemData.case
//                bindingItem.position.text = getString(
//                    R.string.position,
//                    itemData.rack.toString(),
//                    itemData.cell
//                )
//                root.setOnClickListener {
//                    componentsViewModel.mainActivityRouter.navigate(
//                        ComponentFragmentInfo::class.java,
//                        Bundle().apply {
//                            putSerializable(ComponentFragmentInfo.PARAM, itemData.id)
//                        }
//                    )
//                }
////                if (BuildConfig.DEBUG) {
////                    text.visibility = View.VISIBLE
////                    text.text = "${data.found[position]}"
////                } else {
//                    text.visibility = View.GONE
////                }
//            }
        }

    }

    sealed class ComponentsFragmentState<out T:Any> {


        data class Error(private var _exception: Throwable?) : ComponentsFragmentState<Nothing>(){
            val exception: Throwable?
                get() {
                    val tmp=_exception
                    _exception=null
                    return tmp
                }
        }
        data class Success<out T : Any>(private var _data: T?) : ComponentsFragmentState<T>(){
            val data:T?
                get() {
                    val tmp=_data
                    _data=null
                    return tmp
                }
        }
        data object Idle:ComponentsFragmentState<Nothing>()
    }

    class ComponentsViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref
    ) : BaseViewModel() {
        fun componentSearch(param: String,last:String) {
            ioCoroutineScope.launch {
                componentsFragmentState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> ComponentsFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else-> when(
                            val result = apiPantes.componentSearch(
                                    token = token,
                                    query = param,
                                    last=last,
                                )
                            ){
                                    is ApiPantes.ApiState.Success->
                                        ComponentsFragmentState.Success(result.data)
                                    is ApiPantes.ApiState.Error->
                                        ComponentsFragmentState.Error(result.exception)
                                }

                    }
                )
            }
        }

        companion object {
            fun getInstance(context: Context): ComponentsViewModel {
                return   ComponentsViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }

        val componentsFragmentReady=
            MutableLiveData<Int>()
        val componentFragmentEmpty=
            MutableLiveData<Int>()
        val componentFragmentTitle=
            MutableLiveData<String>()
        val componentFragmentSubtitle=
            MutableLiveData<String>()

        val componentsFragmentState=
            MutableLiveData<ComponentsFragmentState<*>>()
    }

}