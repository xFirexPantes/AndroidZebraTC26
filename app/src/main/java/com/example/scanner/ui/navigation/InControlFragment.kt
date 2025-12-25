package com.example.scanner.ui.navigation

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
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
import com.example.scanner.models.InControlSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InControlFragment: BaseFragment() {
    companion object{
        const val PARAM="param"
    }

    private var paramValue: String? = null

    private val incontrolViewModel: InControlViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterincontrol=
        Adapterincontrol()
    private lateinit var infoTextView : TextView
    private var msg: String = ""
    private var IDAll: String = ""
    private var action15: Boolean = false
    private var action23: Boolean = false
    //private lateinit var urgentSearchBut : Button
    //private var isUrgent : Boolean = false
   // private var isUrgentCompare : Boolean = false

    sealed class Back2SkladState<out T : Any> {
        data class Success(val message: String) : Back2SkladState<String>()
        data class CheckST(val IDAll: String,val isOk: String,val action15: Boolean,val action23: Boolean) : Back2SkladState<String>()
        data class Error(val exception: Throwable) : Back2SkladState<Nothing>()
        object Idle : Back2SkladState<Nothing>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)
        paramValue = arguments?.getString(PARAM)
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
                                incontrolViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
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
//                    iconContainer.addView(
//                        TemplateCardBinding.inflate(inflater, root, false)
//                            .apply {
                                // Создаём TextView и добавляем в containerVertical
//                                urgentSearchBut = Button(requireContext()).apply {
//                                    id = View.generateViewId()  // генерируем ID
//                                    visibility = View.VISIBLE  // изначально скрыт
//                                    setPadding(8, 8, 8, 8)
//                                    layoutParams = ViewGroup.LayoutParams(
//                                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                                        ViewGroup.LayoutParams.WRAP_CONTENT
//                                    )
//                                }
//                                urgentSearchBut.setText("срочно")
//                                containerVertical.addView(urgentSearchBut , 0)  // добавляем в начало
//
//                                // Сохраняем ссылку (если нужно управлять позже)
//                                // Например, через tag или поле во фрагменте
//                                containerVertical.tag = urgentSearchBut  // или сохраните в поле фрагмента

                                // ... остальная логика (наблюдатели и т.д.)
//                                urgentSearchBut.setOnClickListener(){
//                                    adapterincontrol.resetContent()
//                                    isUrgent = !isUrgent
//                                    if (isUrgent) {
//                                        urgentSearchBut.setBackgroundColor(Color.argb(255,0,0,200))
//                                        urgentSearchBut.setTextColor(Color.argb(255,255,255,255))
//
//                                        incontrolViewModel.incontrolUrgentSearch(
//                                            getArgument(PARAM),"")
//
//
//                                    }
//                                    else{
//                                        isUrgentCompare = false
//                                        urgentSearchBut.setBackgroundColor(Color.argb(100,100,100,100))
//                                        urgentSearchBut.setTextColor(Color.argb(255,0,0,0))
//
//                                    }
//
//
//                                }
//                            }
//                            .root
//                    )
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                incontrolViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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

                incontrolViewModel.incontrolFragmentTitle
                    .observe(viewLifecycleOwner){
                        toolbar.title=it
                    }

                incontrolViewModel.incontrolFragmentSubtitle
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
                            recycler.adapter=adapterincontrol
                            recycler.layoutManager=
                                object : LinearLayoutManager(requireContext()) {
//                                    override fun onScrollStateChanged(state: Int) {
//                                        super.onScrollStateChanged(state)
//                                        if (findLastVisibleItemPosition()+1 == adapterincontrol.itemCount) {
//                                            incontrolViewModel.incontrolSearch(
//                                                "default",
//                                                adapterincontrol.last.toString())
//                                        }
//                                    }
                                }
                            //region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .root
                                    .apply {
                                        incontrolViewModel.incontrolFragmentEmpty
                                            .observe(viewLifecycleOwner){
                                                this.visibility=it
                                            }
                                        incontrolViewModel.incontrolFragmentEmpty
                                            .postValue(
                                                View.GONE
                                            )
                                    }
                            )
                            //endregion

                            //region ready
//                            containerContent.addView(
//                                TemplateScannerReadyBinding.inflate(inflater,containerContent,false)
//                                    .apply {
//                                        icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_qr,null)
//                                        title="Сканируйте код элемента'"
//                                        incontrolViewModel.incontrolFragmentReady
//                                            .observe(viewLifecycleOwner){
//                                                root.visibility=it
//                                            }
//                                    }
//                                    .root
//                            )
                            //endregion
                        }
                        .root
                )
                //endregion

            }.root
    }


    private fun showResponse(response: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Комментарий")
            .setMessage(response)
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showError(exception: Throwable) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ошибка")
            .setMessage(exception.message ?: "Произошла неизвестная ошибка")
            .setPositiveButton("ОК", null)
            .show()
    }


    private fun handleBack2SkladState(state: Back2SkladState<String>) {
        when (state) {
            is Back2SkladState.Success -> {
                showResponse(state.message)
            }
            is Back2SkladState.Error -> {
                showError(state.exception)
            }
            Back2SkladState.Idle -> {}
            is Back2SkladState.CheckST -> {
                IDAll = state.IDAll
                msg = state.isOk
                action15 = state.action15
                action23 = state.action23
            }

        }
    }
    override fun onPause() {
        super.onPause()
        // Очищаем состояние, если нужно
        incontrolViewModel.back2SkladState.postValue(Back2SkladState.Idle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incontrolViewModel.back2SkladState.observe(viewLifecycleOwner) { state ->
            handleBack2SkladState(state)
        }
        incontrolViewModel.refreshListEvent.observe(viewLifecycleOwner) {
            // Перезагружаем данные списка
            adapterincontrol.resetContent()
            incontrolViewModel.incontrolSearch(paramValue!!, "")
        }
        incontrolViewModel.incontrolFragmentState.observe(viewLifecycleOwner)
        {
            when(val state=it){
                is InControlFragmentState.Error ->{
                    state.exception?.let {exception->
                        findNavController().navigateUp()
                        incontrolViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    exception
                                )
                            })
                    }
                }
                is InControlFragmentState.Success ->{
                    state.data?.let { incontrolSearchResponse->
                        incontrolSearchResponse as InControlSearchResponse
//                        if (      isUrgentCompare){
//                            val apiId = incontrolSearchResponse.found.firstOrNull()?.id
//                            val currentId = if (adapterincontrol.itemCount > 0) {
//                                adapterincontrol.data.found.firstOrNull()?.id
//                            } else {
//                                null
//                            }
//                            if (apiId != null && currentId != null) {
//                                if (apiId == currentId) {
//                                    infoTextView.visibility = View.VISIBLE
//                                    infoTextView.setBackgroundColor(Color.argb(255,0,255,0))
//                                } else {
//                                    infoTextView.visibility = View.VISIBLE
//                                    infoTextView.setBackgroundColor(Color.argb(255,255,0,0))
//                                }
//                            } else {
//
//                            }
//                        }
//                        else {
                        if (adapterincontrol.isResetContent) {
                            infoTextView.visibility = View.GONE
                            incontrolViewModel.incontrolFragmentTitle
                                .postValue(
                                    getString(
                                        R.string.format_title,
                                        "${incontrolSearchResponse.total}"
                                    )
                                )
                            incontrolViewModel.incontrolFragmentEmpty
                                .postValue(
                                    if (incontrolSearchResponse.found.isEmpty())
                                        View.VISIBLE
                                    else
                                        View.GONE
                                )
                            adapterincontrol.setContent(incontrolSearchResponse)
                        } else {
                            adapterincontrol.appendContent(incontrolSearchResponse)
                        }
//                            if (isUrgent) {
//                                isUrgentCompare = true
//                            }
//                    }

                    }

                }
                is InControlFragmentState.Idle->{
                    incontrolViewModel.incontrolFragmentTitle
                        .postValue(getString(R.string.vk_button))

                    incontrolViewModel.incontrolFragmentReady.postValue(
                        when{
                            getArgument<String?>(PARAM).isNullOrEmpty()-> View.VISIBLE
                            else-> View.GONE
                        }
                    )

                    incontrolViewModel.incontrolFragmentEmpty.postValue(
                        when{
                            !getArgument<String?>(PARAM).isNullOrEmpty()
                                    && adapterincontrol.itemCount==0 -> View.VISIBLE
                            else-> View.GONE
                        }
                    )
                }
            }

            if (it!= InControlFragmentState.Idle){
                incontrolViewModel.incontrolFragmentState.postValue(
                    InControlFragmentState.Idle
                )
            }


        }

        fun showPrimInputDialog( onConfirm: (String) -> Unit) {
            val builder = AlertDialog.Builder(requireContext())
            val input = EditText(requireContext())
                    input.inputType = InputType.TYPE_CLASS_TEXT  // или нужный тип ввода
            val dtValue = adapterincontrol.findDtByIdAll(IDAll)
            var actions = ""
            if (action15 == true) {
                actions += "\nТест на паяемость и теплостойкость при пайке"
            }
            if (action23 == true) {
                actions += "\nБаланс смачиваемости"
            }
            builder.setTitle("Укажите место хранения на ВК")
            builder.setMessage("Проверить к: ${dtValue ?: "Не указано"}" + actions)
            builder.setView(input)

            builder.setPositiveButton("ОК") { dialog, _ ->
                val text = input.text.toString().trim()
                onConfirm(text)
                dialog.dismiss()
            }

            builder.setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }

            // Показываем диалог
            builder.show()

            // 1. Устанавливаем фокус на поле ввода
            input.post {
                // 1. Устанавливаем фокус на поле ввода
                input.requestFocus()

                // 2. Показываем клавиатуру
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner)
        {
            when(val scanState=it){
                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                    scanState.stringScanResult?.let { stringScanResult ->
                        if (stringScanResult[0] == '3') {
                            val parts = stringScanResult.split('$')
                            if (parts.size > 1) {
                                val num = parts[1]

                                // ЗАПУСКАЕМ КОРУТИНУ ДЛЯ АСИНХРОННОГО ВЫЗОВА
                                lifecycleScope.launch {
                                    try {
                                        // 1. Вызываем checkst() и ждём результата
                                        val result = incontrolViewModel.checkst(num)

                                        when (result) {
                                            is Result.Success -> {
                                                msg = result.data.isOk
                                                IDAll = result.data.IDAll
                                                action15 = result.data.action15
                                                action23 = result.data.action23
                                                // 2. Получаем DT по IDAll


                                                if (msg.isEmpty()) {
                                                    // 3. Показываем диалог с подставленным DT
                                                    showPrimInputDialog { prim ->
                                                        incontrolViewModel.back2Sklad(num, prim)
                                                    }
                                                } else {
                                                    showResponse(msg)
                                                }
                                            }
                                            is Result.Failure -> {
                                                showError(result.exception)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        showError(e)
                                    }
                                }
                            }
                        }
                        if (stringScanResult.startsWith('C')) {
                            // Отбрасываем первый символ 'C'
                            val content = stringScanResult.substring(1)

                        }


                    }
                }
                else->{}
            }
        }

        if (!getArgument<String?>(PARAM).isNullOrEmpty() && adapterincontrol.itemCount==0){
            incontrolViewModel.incontrolSearch(
                getArgument(PARAM),"")
        }

        incontrolViewModel.incontrolFragmentState.postValue(
            InControlFragmentState.Idle
        )
       // incontrolViewModel.incontrolSearch(paramValue!!,"")
    }

    inner class Adapterincontrol(): BaseRecyclerAdapter<InControlSearchResponse>(InControlSearchResponse()) {
        override fun getCallback(dataOld: InControlSearchResponse?): DiffUtil.Callback {
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


        fun findDtByIdAll(idAll: String): String? {
            return data.found
                .firstOrNull { it.IDAll == idAll.toInt() }  // ищем первый элемент с совпадающим id
                ?.DT                                   // предполагаем, что у InControlSearchResponse.found.item есть поле dt
        }
        override fun appendData(dataNew: InControlSearchResponse) {
            if (dataNew.found.isNotEmpty()) {
                // Проверяем, нет ли уже таких элементов
                val newItems = dataNew.found.filter { item ->
                    !data.found.any { existing -> existing.id == item.id }
                }
                data.last = dataNew.last
                data.found.addAll(newItems)
                notifyDataSetChanged()
            }
        }

        override fun getLastId(): Any {
            return if (data.found.isNotEmpty()) {
                data.last
            } else {
                "" // или другое значение-заполнитель
            }
        }

        override fun cloneData(): InControlSearchResponse {
            return data.copy(found = ArrayList(data.found))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object :ViewHolder(TemplateCardBinding.inflate(layoutInflater,parent,false).root){}
//            return object :ViewHolder(FragmentincontrolRecyclerItemBinding
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
                Pair(arrayOf("horizontalDivider"),""),
                Pair(arrayOf("amount"),"На складе "),
                    //Pair(arrayOf("isolated"),"В изоляторе "),
            )
                .filter { pair ->
                    !pair.first.contains("DT") // Дополнительная фильтрация
                }
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
                incontrolViewModel.mainActivityRouter.navigate(
                    ComponentFragmentInfo::class.java,
                        Bundle().apply {
                            putSerializable(ComponentFragmentInfo.PARAM, itemData.id)
                        }
                    )

            }

//            val bindingItem =
//                Fragmentincontrol//            }
        }

    }

    sealed class InControlFragmentState<out T:Any> {


        data class Error(private var _exception: Throwable?) : InControlFragmentState<Nothing>(){
            val exception: Throwable?
                get() {
                    val tmp=_exception
                    _exception=null
                    return tmp
                }
        }
        data class Success<out T : Any>(private var _data: T?) : InControlFragmentState<T>(){
            val data:T?
                get() {
                    val tmp=_data
                    _data=null
                    return tmp
                }
        }
        data object Idle:InControlFragmentState<Nothing>()
    }

    class InControlViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref

    ) : BaseViewModel() {

        fun incontrolSearch(param: String,last:String) {
            ioCoroutineScope.launch {
                incontrolFragmentState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> InControlFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else-> when(
                            val result = apiPantes.incontrolSearch(
                                    token = token,
                                    query = param,
                                    last=last,
                                )
                            ){
                                    is ApiPantes.ApiState.Success->
                                        InControlFragmentState.Success(result.data)
                                    is ApiPantes.ApiState.Error->
                                        InControlFragmentState.Error(result.exception)
                                }

                    }
                )
            }
        }


        fun back2Sklad(num: String,prim: String) {

            ioCoroutineScope.launch {

                when(val token=loginRepository.user?.token){
                    null-> InControlFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                    else->{
                        when(val result = apiPantes.incontrolBack2sklad(
                            token = token,
                            num = num,
                            prim = prim,
                            )) {
                            is ApiPantes.ApiState.Success -> {
                                if (result.data.toString().length > 0) {
                                    back2SkladState.postValue(Back2SkladState.Success(result.data))
                                }
                                refreshListEvent.postValue(Unit)
                                                      }
                            is ApiPantes.ApiState.Error -> {
                                back2SkladState.postValue(Back2SkladState.Error(result.exception))
                            }
                        }
                    }
                }

            }
        }
        suspend fun checkst(num: String): Result<Back2SkladState.CheckST> = withContext(Dispatchers.IO) {
            val token = loginRepository.user?.token
                ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

            when (val result = apiPantes.incontrolCheckst(token, num)) {
                is ApiPantes.ApiState.Success -> {
                    if (result.data.toString().isNotEmpty()) {

                        val DT = result.data.isOk!!
                        val IDAll = result.data.IDAll!!
                        val action15 = result.data.action15!!
                        val action23  = result.data.action23!!
                        DT.let {
                            Back2SkladState.CheckST(
                                IDAll ,
                                it,action15,action23
                            )
                        }.let {
                            Result.Success(
                                it
                            )
                        }
                    } else {
                        Result.Failure(Exception("Empty response"))
                    }
                }

                is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
            } as Result<Back2SkladState.CheckST>
        }

        companion object {
            fun getInstance(context: Context): InControlViewModel {
                return   InControlViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }

        val incontrolFragmentReady=
            MutableLiveData<Int>()
        val incontrolFragmentEmpty=
            MutableLiveData<Int>()
        val incontrolFragmentTitle=
            MutableLiveData<String>()
        val incontrolFragmentSubtitle=
            MutableLiveData<String>()

        val incontrolFragmentState=
            MutableLiveData<InControlFragmentState<*>>()
        val back2SkladState = MutableLiveData<Back2SkladState<String>>()
        val refreshListEvent = MutableLiveData<Unit>()
    }
    sealed class Result<out T : Any> {
        data class Success<out T : Any>(val data: T) : Result<T>()
        data class Failure(val exception: Throwable) : Result<Nothing>()
    }

    // Для удобства: расширения
    val Result<*>.isSuccess: Boolean get() = this is Result.Success
    val Result<*>.isFailure: Boolean get() = this is Result.Failure

    inline fun <R : Any> Result<R>.onSuccess(action: (R) -> Unit): Result<R> {
        if (this is Result.Success) action(data)
        return this
    }

    inline fun Result<*>.onFailure(action: (Exception) -> Unit): Result<*> {
        if (this is Result.Failure) action(exception)
        return this
    }

    fun action(exception: Throwable) {
        TODO("Not yet implemented")
    }
}