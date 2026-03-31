package com.example.scanner.ui.navigation

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
import com.example.scanner.models.TrueSignSearchResponse
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

class TrueSignFragment: BaseFragment() {
    companion object{
        const val PARAM="param"
    }


    private val truesignViewModel: TrueSignViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterTrueSign =
        AdapterTrueSign()
    private lateinit var infoTextView : TextView

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
                                truesignViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
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
                        TemplateCardBinding.inflate(inflater, root, false)
                            .apply {
                                // Создаём TextView и добавляем в containerVertical


                                 }
                            .root
                    )
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                truesignViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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

                truesignViewModel.truesignFragmentTitle
                    .observe(viewLifecycleOwner){
                        toolbar.title=it
                    }

                truesignViewModel.truesignFragmentSubtitle
                    .observe(viewLifecycleOwner){
                        toolbar.subtitle=it
                    }
                root.addView(
                    TemplateCardBinding.inflate(inflater, root, false)
                        .apply {
                            // Создаём TextView и добавляем в containerVertical
                            infoTextView = TextView(requireContext()).apply {
                                id = View.generateViewId()  // генерируем ID
                                visibility = View.GONE  // изначально скрыт
                                setTextColor(Color.RED)  // например, красный текст
                                textSize = 14f
                                setPadding(8, 8, 8, 8)
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }
                            containerVertical.addView(infoTextView, 0)  // добавляем в начало

                            // Сохраняем ссылку (если нужно управлять позже)
                            // Например, через tag или поле во фрагменте
                            containerVertical.tag = infoTextView  // или сохраните в поле фрагмента

                            // ... остальная логика (наблюдатели и т.д.)
                        }
                        .root
                )
                //region recyclerView
                root.addView(
                    TemplateRecyclerBinding.inflate(inflater,root,false)
                        .apply {
                            recycler.adapter=adapterTrueSign
                            recycler.layoutManager=
                                object : LinearLayoutManager(requireContext()) {
                                    override fun onScrollStateChanged(state: Int) {
                                        super.onScrollStateChanged(state)
                                        if (findLastVisibleItemPosition()+1 == adapterTrueSign.itemCount) {
                                            truesignViewModel.truesignSearch(
                                                getArgument(PARAM),
                                                adapterTrueSign.last.toString())
                                        }
                                    }
                                }
                            //region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .root
                                    .apply {
                                        truesignViewModel.truesignFragmentEmpty
                                            .observe(viewLifecycleOwner){
                                                this.visibility=it
                                            }
                                        truesignViewModel.truesignFragmentEmpty
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
                                        truesignViewModel.truesignFragmentReady
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

        truesignViewModel.truesignFragmentState.observe(viewLifecycleOwner)
        {
            when(val state=it){
                is TrueSignFragmentState.Error ->{
                    state.exception?.let {exception->
                        findNavController().navigateUp()
                        truesignViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    exception
                                )
                            })
                    }
                }
                is TrueSignFragmentState.Success ->{
                    state.data?.let { truesignSearchResponse->
                        truesignSearchResponse as TrueSignSearchResponse

                        if (adapterTrueSign.isResetContent) {
                            infoTextView.visibility = View.GONE
                            truesignViewModel.truesignFragmentTitle
                                .postValue(
                                    getString(
                                        R.string.format_title,
                                        "${truesignSearchResponse.total}"
                                    )
                                )
                            truesignViewModel.truesignFragmentEmpty
                                .postValue(
                                    if (truesignSearchResponse.found.isEmpty())
                                        View.VISIBLE
                                    else
                                        View.GONE
                                )
                            adapterTrueSign.setContent(truesignSearchResponse)
                        } else {
                            adapterTrueSign.appendContent(truesignSearchResponse)
                        }



                    }

                }
                is TrueSignFragmentState.Idle->{
                    truesignViewModel.truesignFragmentTitle
                        .postValue(getString(R.string.button_search))

                    truesignViewModel.truesignFragmentReady.postValue(
                        when{
                            getArgument<String?>(PARAM).isNullOrEmpty()-> View.VISIBLE
                            else-> View.GONE
                        }
                    )

                    truesignViewModel.truesignFragmentEmpty.postValue(
                        when{
                            !getArgument<String?>(PARAM).isNullOrEmpty()
                                    && adapterTrueSign.itemCount==0 -> View.VISIBLE
                            else-> View.GONE
                        }
                    )
                }
            }

            if (it!= TrueSignFragmentState.Idle){
                truesignViewModel.truesignFragmentState.postValue(
                    TrueSignFragmentState.Idle
                )
            }
        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner)
        {
            when(val scanState=it){


                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult-> {
                    scanState.stringScanResult?.let { stringScanResult ->

                        arguments = Bundle().apply {
                            putSerializable(PARAM, stringScanResult)
                        }
                        infoTextView.visibility = View.VISIBLE
                        infoTextView.text = stringScanResult
                        truesignViewModel.truesignFragmentSubtitle
                            .postValue(getString(R.string.format_subtitle, getArgument(PARAM)))
                        adapterTrueSign.resetContent()
//                        truesignViewModel.truesignSearch(getArgument(PARAM),"")

                        truesignViewModel.truesignFragmentReady
                            .postValue(
                                View.GONE
                            )


                    }
                }
                else->{}
            }
        }

        if (!getArgument<String?>(PARAM).isNullOrEmpty() && adapterTrueSign.itemCount==0){
            truesignViewModel.truesignSearch(
                getArgument(PARAM),"")
        }

        truesignViewModel.truesignFragmentState.postValue(
            TrueSignFragmentState.Idle
        )

    }

    inner class AdapterTrueSign: BaseRecyclerAdapter<TrueSignSearchResponse>(TrueSignSearchResponse()) {
        override fun getCallback(dataOld: TrueSignSearchResponse?): DiffUtil.Callback {
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

        override fun appendData(dataNew: TrueSignSearchResponse) {
            if (dataNew.found.isNotEmpty()){
                data.last=dataNew.last
                data.found.addAll(dataNew.found)
            }
        }

        override fun getLastId(): Any {
            return data.last
        }

        override fun cloneData(): TrueSignSearchResponse {
            return data.copy(found = ArrayList(data.found))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object :ViewHolder(TemplateCardBinding.inflate(layoutInflater,parent,false).root){}

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
                Pair(arrayOf("isokol"),"В изоляторе "),
                Pair(arrayOf("drykol"),"На сушке "),
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

//            itemBinding.containerVertical.setOnClickListener {
//                truesignViewModel.mainActivityRouter.navigate(
//                        TrueSignFragmentInfo::class.java,
//                        Bundle().apply {
//                            putSerializable(TrueSignFragmentInfo.PARAM, itemData.id)
//                        }
//                    )
//
//            }


        }

    }

    sealed class TrueSignFragmentState<out T:Any> {


        data class Error(private var _exception: Throwable?) : TrueSignFragmentState<Nothing>(){
            val exception: Throwable?
                get() {
                    val tmp=_exception
                    _exception=null
                    return tmp
                }
        }
        data class Success<out T : Any>(private var _data: T?) : TrueSignFragmentState<T>(){
            val data:T?
                get() {
                    val tmp=_data
                    _data=null
                    return tmp
                }
        }
        data object Idle:TrueSignFragmentState<Nothing>()
    }

    class TrueSignViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref
    ) : BaseViewModel() {
        fun truesignSearch(param: String,last:String) {
            ioCoroutineScope.launch {
                truesignFragmentState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> TrueSignFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else-> when(
                            val result = apiPantes.componentSearch(
                                    token = token,
                                    query = param,
                                    last=last,
                                )
                            ){
                                    is ApiPantes.ApiState.Success->
                                        TrueSignFragmentState.Success(result.data)
                                    is ApiPantes.ApiState.Error->
                                        TrueSignFragmentState.Error(result.exception)
                                }

                    }
                )
            }
        }

        companion object {
            fun getInstance(context: Context): TrueSignViewModel {
                return   TrueSignViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }

        val truesignFragmentReady=
            MutableLiveData<Int>()
        val truesignFragmentEmpty=
            MutableLiveData<Int>()
        val truesignFragmentTitle=
            MutableLiveData<String>()
        val truesignFragmentSubtitle=
            MutableLiveData<String>()

        val truesignFragmentState=
            MutableLiveData<TrueSignFragmentState<*>>()
    }

}