package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.media.AudioManager

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.scanner.R
import com.example.scanner.app.SessionViewModel
import com.example.scanner.app.SoundHelper
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateRecyclerBinding
import com.example.scanner.databinding.TemplateResultEmptyBinding
import com.example.scanner.models.IsolatorListSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.ReceiveFragment.Companion.PARAM_STEP_1_VALUE
import com.example.scanner.ui.navigation.ReceiveFragment.ReceiveFragmentFormState
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

class IsolatorListFragment: BaseFragment() {
    companion object{
        const val PARAM="param"
    }
    private var needscroll : Boolean = true
    private var paramValue: String = ""
    private lateinit var toolbarlnk: androidx.appcompat.widget.Toolbar
    private lateinit var recyclerView: RecyclerView
    private val isolatorListViewModel: IsolatorListViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterisolatorlist=
        AdapterisolatorList()

    private lateinit var step1: EditText
    private val sViewModel: SessionViewModel by viewModels { viewModelFactory }
    private lateinit var infoTextView : TextView
    private var IDAll: String = ""
    private var curNum : String? = ""
    var oldSize = 0
    private var Nkat: String = ""
    private var isBottle: Boolean = false
    private lateinit var soundHelper: SoundHelper
    private var lastQR: String = ""
    // Инициализация (один раз)




    private val itemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0, 0) {
            // Отключаем автоматическое удаление
            override fun getSwipeThreshold(viewHolder: ViewHolder): Float = 1f
            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder
            ): Boolean = false

            // Визуально выделяем элемент при свайпе
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                // Затемняем элемент при свайпе
                val alpha = 1 - abs(dX) / viewHolder.itemView.width.toFloat()
                viewHolder.itemView.alpha = alpha
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)
        paramValue = arguments?.getString(PARAM).toString()
        curNum = ""

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
                    toolbarlnk = toolbar
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                src= ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)
                                isolatorListViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
                                    root.visibility=it
                                }
                                image.setOnClickListener {
                                    scanViewModel.scannerApiEmulator.softScan(childFragmentManager,requireContext())
                                }
                            }
                            .root
                    )

                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                isolatorListViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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
                when (paramValue) {
                    "iniso" -> toolbar.title = "В изоляторе"
                    "inisosklad" -> toolbar.title = "Выданы образцы"
                    "towh" -> toolbar.title = "На склад"
                    else -> toolbar.title = "Неизвестный режим"
                }


                isolatorListViewModel.isolatorListFragmentSubtitle
                    .observe(viewLifecycleOwner){
                        toolbar.subtitle=it
                    }
                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply
                        {
                            if (paramValue=="iniso") {
                                val filterButton = Button(requireContext()).apply {
                                    text = "Фильтры"
                                    setOnClickListener {
                                        showFilterDialog()
                                    }
                                }
                                containerVertical.addView(filterButton, 0)
                            }
                        }
                        .root
                )
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
                    TemplateRecyclerBinding.inflate(inflater, root, false)
                        .apply {
                            // Сохраняем ссылку на RecyclerView из текущего binding
                            recyclerView = recycler
                            recyclerView.isSaveEnabled = false
                            // Настраиваем RecyclerView
                            recycler.adapter = adapterisolatorlist
                            recycler.layoutManager = LinearLayoutManager(requireContext())
                            recycler.setHasFixedSize(true) // опционально
                            // Подключаем ItemTouchHelper
                            itemTouchHelper.attachToRecyclerView(recycler)
                            // region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater, containerContent, false)
                                    .root
                                    .apply {
                                        isolatorListViewModel.isolatorListFragmentEmpty
                                            .observe(viewLifecycleOwner) { this.visibility = it }
                                        isolatorListViewModel.isolatorListFragmentEmpty.postValue(View.GONE)
                                    }
                            )
                            // endregion

                            // region ready (опционально)
                            // containerContent.addView(...)
                            // endregion
                        }
                        .root
                )


                // Сохраняем ссылку на recyclerView


                // Настраиваем RecyclerView
                //endregion

            }.root

    }

    private fun showFilterDialog() {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Выберите фильтр")
                .setAdapter(

                    ArrayAdapter(
                        requireContext(),
                        R.layout.dialog_list_item,
                        arrayOf("Все", "По коду", "По причине")
                    )

                ) { _, which ->
                    adapterisolatorlist.resetContent()
                    isolatorListViewModel.isolatorListFragmentState.postValue(IsolatorListFragmentState.Idle)

                    when (which) {
                        0 -> {
                            isolatorListViewModel.isolatorListSearch(0, paramValue,"")
                        }
                        1 -> showOvenNumberDialog()
                        2 -> showStateDialog()
                    }
                }

                .show()

    }
    private fun showOvenNumberDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_input_number, null)
        val inputEditText = dialogView.findViewById<EditText>(R.id.editTextOvenNumber)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Введите номер элемента")
            .setView(dialogView)
            .setPositiveButton("ОК") { _, _ ->
                val inputText = inputEditText.text.toString()

                // Проверка на пустой ввод
                if (inputText.isBlank()) {
                    Toast.makeText(requireContext(), "Введите номер элемента", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                val skladid = inputText.toIntOrNull()
                if (skladid  == null) {
                    Toast.makeText(requireContext(), "Пожалуйста, введите корректное число", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                adapterisolatorlist.resetContent()
                toolbarlnk.title = "Поиск $skladid "
                isolatorListViewModel.isolatorListSearch(skladid , paramValue,"")
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun showStateDialog() {
        // Создаём Map с ID и соответствующими состояниями
        val stateMap = mapOf(
            -1 to "Все" ,
            0 to "Ведется исследование",
            1 to "Замена элементов",
            2 to "Тест паяемости",
            4 to "Перенос в ручной элемент",
            5 to "Перенос в автоматический элемент",
            6 to "Возврат денежных средств поставщиком",
            7 to "Утилизация элемента",
            8 to "Функциональный контроль",
            9 to "Излишки",
            10 to "Замена брака из излишков"
        )

        // Получаем список состояний для отображения в диалоге
        val states = stateMap.values.toList()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите состояние")
            .setAdapter(
                ArrayAdapter(requireContext(), R.layout.dialog_list_item, states.toTypedArray())
            ) { _, which ->
                // Получаем ID выбранного состояния по индексу
                val selectedId = stateMap.keys.toList()[which]
                val state = states[which]

                adapterisolatorlist.resetContent()
                toolbarlnk.title = state
                isolatorListViewModel.isolatorListSearch(0, paramValue,selectedId.toString())
            }
            .show()
    }



    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        soundHelper = SoundHelper(requireContext())
        isolatorListViewModel.refreshListEvent.observe(viewLifecycleOwner) {
            // Перезагружаем данные списка
            adapterisolatorlist.resetContent()
            isolatorListViewModel.isolatorListSearch(0, paramValue,"")

        }
        sViewModel.scannedItems.observe(viewLifecycleOwner) { scanned ->
            adapterisolatorlist.updateItems(scanned) // Перекрашиваем все элементы
        }
        isolatorListViewModel.searchCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted && curNum!!.isNotEmpty()) {

                lifecycleScope.launch {
                    try {
                        if (needscroll) {
                            when (val result = isolatorListViewModel.getAllID(curNum!!)) {
                                is Result.Success -> {

                                    IDAll = result.data.toString()
                                    val position = adapterisolatorlist.findPosition(IDAll)
                                    if (position != null && position != -1) {
                                        adapterisolatorlist.scrollToPosition(position, recyclerView)
                                    } else {
                                        // showResponse("Элемент с IDAll=$IDAll не найден в списке")
                                    }

                                }

                                is Result.Failure -> showError(result.exception)
                            }
                        }
                    } catch (e: Exception) {
                        showError(e)
                    }

                    // Сброс флага для будущих вызовов
                   // isolatorListViewModel.resetSearchCompleted()
                }
            }
        }
        isolatorListViewModel.isolatorFragmentSearchResponse.observe(viewLifecycleOwner) { response ->
            // Обновляем адаптер при получении новых данных
            response?.let {
                adapterisolatorlist.setContent(it)
            }
        }
        isolatorListViewModel.isolatorListFragmentState.observe(viewLifecycleOwner)
        {
            when(val state=it){
                is IsolatorListFragmentState.Error ->{
                    state.exception?.let {exception->
                        findNavController().navigateUp()
                        isolatorListViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    exception
                                )
                            })
                    }
                }
                is IsolatorListFragmentState.Success ->{
                    state.data?.let { incontrolSearchResponse->
                        incontrolSearchResponse as IsolatorListSearchResponse

                        if (adapterisolatorlist.isResetContent) {
                            infoTextView.visibility = View.GONE
                            isolatorListViewModel.isolatorListFragmentTitle
                                .postValue(
                                    getString(
                                        R.string.format_title,
                                        "${incontrolSearchResponse.total}"
                                    )
                                )
                            isolatorListViewModel.isolatorListFragmentEmpty
                                .postValue(
                                    if (incontrolSearchResponse.found.isEmpty())
                                        View.VISIBLE
                                    else
                                        View.GONE
                                )
                            adapterisolatorlist.setContent(incontrolSearchResponse)
                        } else {
                            adapterisolatorlist.appendContent(incontrolSearchResponse)
                        }
//                            if (isUrgent) {
//                                isUrgentCompare = true
//                            }
//                    }

                    }

                }
                is IsolatorListFragmentState.Idle->{
                    isolatorListViewModel.isolatorListFragmentTitle
                        .postValue(getString(R.string.vk_button))

                    isolatorListViewModel.isolatorListFragmentReady.postValue(
                        when{
                            getArgument<String?>(PARAM).isNullOrEmpty()-> View.VISIBLE
                            else-> View.GONE
                        }
                    )

                    isolatorListViewModel.isolatorListFragmentEmpty.postValue(
                        when{
                            !getArgument<String?>(PARAM).isNullOrEmpty()
                                    && adapterisolatorlist.itemCount==0 -> View.VISIBLE
                            else-> View.GONE
                        }
                    )
                }
            }

            if (it!= IsolatorListFragmentState.Idle){
                isolatorListViewModel.isolatorListFragmentState.postValue(
                    IsolatorListFragmentState.Idle
                )
            }


        }


        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
            when(paramValue){
                "iniso" -> {

                }
                "towh" -> {
                    when(val stateScan=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan(stringScanResult)
                                    stringScanResult.startsWith('C') -> handleCScan(stringScanResult)
                                    (stringScanResult.split('$')).size == 5 -> handleCScanBottle(stringScanResult)
                                    else -> showErrorMessage("Неподдерживаемый формат QR-кода")
                                }
                            }
                        }
                        else->{}
                    }
                }
                "inisosklad" -> {
                    when(val stateScan=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan(stringScanResult)
                                    stringScanResult.startsWith('C') -> handleCScanSklad(stringScanResult)
                                    (stringScanResult.split('$')).size == 5 -> handleCScanBottle(stringScanResult)
                                    else -> showErrorMessage("Неподдерживаемый формат QR-кода")
                                }
                            }
                        }
                        else->{}
                    }

                }
                else->{}
            }

            isolatorListViewModel.isolatorListFragmentState.postValue(
                IsolatorListFragmentState.Idle
            )

        }
        isolatorListViewModel.isolatorListSearch(0,paramValue,"")
    }
    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun handle3N0Scan(stringScanResult: String) {
        isBottle = false

        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            val num = parts[1]
            curNum = num
            lifecycleScope.launch {
                when (val result = isolatorListViewModel.getAllID(num)) {
                    is Result.Success -> {
                        val IDAllList = result.data
                        // Теперь можно работать с полученным списком
                        handleIDAllList(IDAllList)
                        isolatorListViewModel.refreshListEvent.postValue(Unit)
                    }

                    is Result.Failure -> {
                        showError(result.exception)
                    }
                }

            }

        }
    }

    private fun handleIDAllList(idAllList: ArrayList<Int>) {
        val lastStel = isolatorListViewModel.lastStoredStel
        val lastCell = isolatorListViewModel.lastStoredCell
        val firstIdAll = idAllList.firstOrNull()
        IDAll = firstIdAll.toString()
        val currentItem = adapterisolatorlist.findByIdAll(IDAll)
        if (currentItem == null) {
            showResponse("Элемент не найден")
            return
        }

        val stel = currentItem.stel
        val cell = currentItem.cell
        if (firstIdAll != null) {
            val position = adapterisolatorlist.findPosition(firstIdAll.toString())
            if (position != null && position != -1) {
                adapterisolatorlist.scrollToPosition(position,recyclerView)
            } else {
                showResponse("Элемент с IDAll=$firstIdAll не найден в списке")
            }
        } else {
            showResponse("Получен пустой список IDAll")
        }
        if (lastStel.isNotEmpty() && lastCell.isNotEmpty()) {
            if(lastStel == stel.toString() && (lastCell == cell)) {
                infoTextView.visibility = View.VISIBLE
                infoTextView.setBackgroundColor(Color.argb(255, 0, 255, 0))
                needscroll = false
                lifecycleScope.launch {
                    val putResult: Result<Unit> = if (isBottle){
                        isolatorListViewModel.put2WH("bottle"+curNum!!)
                    } else {
                        isolatorListViewModel.put2WH(curNum!!)

                    }


                    when (putResult) {
                        is Result.Success<Unit> -> {
                            // Успех: запрашиваем обновление списка
                            isolatorListViewModel.refreshListEvent.postValue(Unit)
                        }

                        is Result.Failure -> {
                            // Ошибка: показываем сообщение
                            showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                        }
                    }

                }
            }
            else {
                infoTextView.visibility = View.VISIBLE
                infoTextView.setBackgroundColor(Color.argb(255,255,0,0))
                val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
                isolatorListViewModel.clearStelAndCell()
            }

        }


    }
    private fun handleCScan(stringScanResult: String) {
        if (curNum == "") {
            Toast.makeText(requireContext(), "Сначала отсканируйте компонент", Toast.LENGTH_SHORT).show()
        }
        else{
            val content = stringScanResult.substring(1)

            // Проверяем, что осталось ровно 12 символов
            if (content.length == 12) {
                // Разбиваем на 3 части по 4 символа
                val shelfPart = content.substring(0, 4)   // стеллаж
                val levelPart = content.substring(4, 8)  // полка
                val cellPart  = content.substring(8, 12) // ячейка

                // Удаляем ведущие нули в каждой части
                val stel = shelfPart.toIntOrNull()?.toString() ?: ""
                val level = levelPart.toIntOrNull()?.toString() ?: "0"
                val cell  = cellPart.toIntOrNull()?.toString() ?: "0"

                // Формируем yach = Полка + "." + Ячейка
                val yach = if (level.isNotEmpty() && cell.isNotEmpty()) {
                    "${level}.${cell}"
                } else {
                    ""
                }

                // Получаем текущие значения stel и cell из отображаемых данных
                val currentItem =  adapterisolatorlist.getItemByIdAll(IDAll.toInt())
                if (currentItem != null) {
                    val currentStel = currentItem.stel.toString()
                    val currentCell = currentItem.cell

                    // Сравниваем

                    if (stel == currentStel && yach == currentCell) {
                        // Совпадение → зелёный фон
                        infoTextView.visibility = View.VISIBLE
                        infoTextView.setBackgroundColor(Color.argb(255,0,255,0))
//                                                adapterincontrol.resetContent()
//                                                incontrolViewModel.put2WH(curNum!!,paramValue,box)
//                                                incontrolViewModel.refreshListEvent.postValue(Unit)
                        lifecycleScope.launch {
                            val putResult : Result<Unit> = if (isBottle) {
                                isolatorListViewModel.put2WH("bottle"+curNum!!)
                            } else {
                                isolatorListViewModel.put2WH(curNum!!)
                            }


                            when (putResult) {
                                is Result.Success<Unit> -> {
                                    // Успех: запрашиваем обновление списка
                                    isolatorListViewModel.refreshListEvent.postValue(Unit)
                                }
                                is Result.Failure -> {
                                    // Ошибка: показываем сообщение
                                    showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                                }
                            }

                        }
                        curNum = ""
                        isolatorListViewModel.saveStelAndCell(stel,yach)
                    } else {
                        // Несовпадение → красный фон
                        infoTextView.visibility = View.VISIBLE
                        infoTextView.setBackgroundColor(Color.argb(255,255,0,0))
                        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
                        isolatorListViewModel.clearStelAndCell()
                    }
                } else {
                    showResponse("Нет данных для сравнения (receiveFragmentAcceptSearchResponse пуст)")
                }
            } else {
                showResponse("QR-код после 'C' должен содержать 12 цифр, получено: ${content.length}")
            }
        }
    }

    private fun handleCScanSklad(stringScanResult: String) {
        if (curNum == "") {
            Toast.makeText(requireContext(), "Сначала отсканируйте компонент", Toast.LENGTH_SHORT).show()
        }
        else{
            val content = stringScanResult.substring(1)

            // Проверяем, что осталось ровно 12 символов
            if (content.length == 12) {
                // Разбиваем на 3 части по 4 символа
                val shelfPart = content.substring(0, 4)   // стеллаж
                val levelPart = content.substring(4, 8)  // полка
                val cellPart  = content.substring(8, 12) // ячейка

                // Удаляем ведущие нули в каждой части
                val stel = shelfPart.toIntOrNull()?.toString() ?: ""
                val level = levelPart.toIntOrNull()?.toString() ?: "0"
                val cell  = cellPart.toIntOrNull()?.toString() ?: "0"

                // Формируем yach = Полка + "." + Ячейка
                val yach = if (level.isNotEmpty() && cell.isNotEmpty()) {
                    "${level}.${cell}"
                } else {
                    ""
                }

                // Получаем текущие значения stel и cell из отображаемых данных
                val currentItem =  adapterisolatorlist.getItemByIdAll(IDAll.toInt())
                if (currentItem != null) {
                    val currentStel = currentItem.stel.toString()
                    val currentCell = currentItem.cell

                    // Сравниваем

                    if (stel == currentStel && yach == currentCell) {
                        // Совпадение → зелёный фон
                        infoTextView.visibility = View.VISIBLE
                        infoTextView.setBackgroundColor(Color.argb(255,0,255,0))
//                                                adapterincontrol.resetContent()
//                                                incontrolViewModel.put2WH(curNum!!,paramValue,box)
//                                                incontrolViewModel.refreshListEvent.postValue(Unit)
                        lifecycleScope.launch {
                            val putResult : Result<Unit> = if (isBottle) {
                                isolatorListViewModel.put2WHisoSklad("bottle"+curNum!!)
                            } else {
                                isolatorListViewModel.put2WHisoSklad(curNum!!)
                            }


                            when (putResult) {
                                is Result.Success<Unit> -> {
                                    // Успех: запрашиваем обновление списка
                                    isolatorListViewModel.refreshListEvent.postValue(Unit)
                                }
                                is Result.Failure -> {
                                    // Ошибка: показываем сообщение
                                    showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                                }
                            }

                        }
                        curNum = ""
                        isolatorListViewModel.saveStelAndCell(stel,yach)
                    } else {
                        // Несовпадение → красный фон
                        infoTextView.visibility = View.VISIBLE
                        infoTextView.setBackgroundColor(Color.argb(255,255,0,0))
                        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
                        isolatorListViewModel.clearStelAndCell()
                    }
                } else {
                    showResponse("Нет данных для сравнения (receiveFragmentAcceptSearchResponse пуст)")
                }
            } else {
                showResponse("QR-код после 'C' должен содержать 12 цифр, получено: ${content.length}")
            }
        }
    }

    private fun handleCScanBottle(stringScanResult: String) {
        try {
            val parts = stringScanResult.split('$')

            // Проверяем, что частей ровно 5
            if (parts.size != 5) {
                showErrorMessage("Неверный формат QR-кода бутылки (ожидается 5 частей через $)")
                return
            }

            // Номер катушки на 1‑й позиции (индекс 1)
            Nkat = parts[2]
            isBottle = true
            requireArguments().putSerializable(PARAM_STEP_1_VALUE, stringScanResult)
            step1.setText(stringScanResult)
            lastQR = stringScanResult

            // Всегда просто запускаем поиск

            isolatorListViewModel.isolatorListSearch(0,paramValue,"")

            Timber.tag("ReceiveFragment").d("Обработан QR бутылки: Nkat=$Nkat")

        } catch (e: Exception) {
            showErrorMessage("Ошибка обработки QR-кода бутылки: ${e.message}")
            Timber.tag("ReceiveFragment").e(e, "Bottle scan error")
        }
    }

    private fun showError(exception: Throwable) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ошибка")
            .setMessage(exception.message ?: "Произошла неизвестная ошибка")
            .setPositiveButton("ОК", null)
            .show()
    }


    private fun showResponse(response: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Комментарий")
            .setMessage(response)
            .setNegativeButton("Закрыть", null)
            .show()
    }


    inner class AdapterisolatorList: BaseRecyclerAdapter<IsolatorListSearchResponse>(IsolatorListSearchResponse()) {
        private var hasItemCountDecreased = false
        private var selectedPosition: Int = -1 // -1 = ничего не выделено



        override fun setContent(dataNew: IsolatorListSearchResponse) {
            val newSize = getNewSize(dataNew)  // Получаем размер нового списка
            val scanned = sViewModel.scannedItems.value ?: emptySet()
            hasItemCountDecreased = newSize < oldSize
            oldSize = newSize // Устанавливаем флаг
            if (hasItemCountDecreased) {
                isolatorListViewModel.lastStoredCell = ""
                isolatorListViewModel.lastStoredStel = ""
            }


            dataNew.found.forEach { item ->
                item.coils.forEach { coil ->
                    coil.isScanned = scanned.contains(coil.num)
                }
                item.isScanned = item.coils.all { it.isScanned }
            }

            super.setContent(dataNew) // Передаем данные базовому адаптеру
        }

        private fun getNewSize(newData: IsolatorListSearchResponse): Int {
            return newData.found.size
        }
        override fun getCallback(dataOld: IsolatorListSearchResponse?): DiffUtil.Callback {
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
                    val oldItem = dataOld?.found?.get(oldItemPosition)
                    val newItem = data.found[newItemPosition]

                    // Если старый элемент не существует, считаем, что содержимое отличается
                    if (oldItem == null) return false

                    return oldItem.id == newItem.id &&
                            oldItem.kolpacks == newItem.kolpacks
                }
            }
        }
        @SuppressLint("NotifyDataSetChanged")
        fun setSelectedPosition(position: Int) {
            selectedPosition = position
            notifyDataSetChanged() // перерисовываем все элементы
        }
//        fun findPosition(idAll: String): Int? {
//            val targetId = idAll.toInt()
//            val index = data.found.indexOfFirst { it.IDAll == targetId }
//            return if (index != -1) index else null
//        }
//
//        fun findDtByIdAll(idAll: String): String? {
//            return data.found
//                .firstOrNull { it.IDAll == idAll.toInt() }  // ищем первый элемент с совпадающим id
//                ?.DT                                   // предполагаем, что у InControlSearchResponse.found.item есть поле dt
//        }

        override fun appendData(dataNew: IsolatorListSearchResponse) {
            if (dataNew.found.isNotEmpty()) {

                // Проверяем, нет ли уже таких элементов
                val newItems = dataNew.found.filter { item ->
                    !data.found.any { existing -> existing.id == item.id }
                }
                data.last = dataNew.last
                data.found.addAll(newItems)
                //notifyDataSetChanged()
            }
        }

        fun findPosition(idAll: String): Int? {
            val targetId = idAll.toInt()
            val index = data.found.indexOfFirst { it.IDAll == targetId }
            return if (index != -1) index else null
        }
        fun getItemByIdAll(idAll: Int): IsolatorListSearchResponse.Item? {
            return data.found.firstOrNull { it.IDAll == idAll }
        }
        fun findByIdAll(idAll: String): IsolatorListSearchResponse.Item? {
            return data.found
                .firstOrNull { it.IDAll == idAll.toInt() }  // ищем первый элемент с совпадающим id
            // предполагаем, что у InControlSearchResponse.found.item есть поле dt
        }

        fun scrollToPosition(position: Int,recyclerView: RecyclerView) {
            if (position in 0 until itemCount) {
                // Устанавливаем выделенную позицию
                setSelectedPosition(position)

                // Прокручиваем после отрисовки
                recyclerView.post {
                    recyclerView.scrollToPosition(position)
                }
            } else {
                Timber.tag("AdapterInControl")
                    .w("Cannot scroll to position $position. Valid range: 0–${itemCount - 1}")
            }
        }
        override fun getLastId(): Any {
            return if (data.found.isNotEmpty()) {
                data.last
            } else {
                "" // или другое значение-заполнитель
            }
        }

        override fun cloneData(): IsolatorListSearchResponse {
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

        @SuppressLint("NotifyDataSetChanged")
        fun updateItems(scanned: Set<Int>) {
            data.found.forEach { item ->
                item.coils.forEach { coil ->
                    coil.isScanned = scanned.contains(coil.num)
                }
                item.isScanned = item.coils.isNotEmpty() && item.coils.all { it.isScanned }
            }
            notifyDataSetChanged()
        }
        @SuppressLint("SuspiciousIndentation", "SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemBinding = TemplateCardBinding.bind(holder.itemView)
            val itemData = data.found[position]

            // Очищаем контейнеры
            itemBinding.containerVertical.removeAllViews()
            itemBinding.containerHorizon.removeAllViews()

            // region Заполняем основные поля (через TemplatePresenterBinding)
            arrayOf(
                Pair(arrayOf("SkladID"), "# компонента "),
                Pair(arrayOf("Naim"), "Наименование "),
                Pair(arrayOf("Nom"), "Номинал "),
                Pair(arrayOf("Krp"), "Корпус "),
                Pair(arrayOf("kol"), "Кол-во "),
                Pair(arrayOf("Reason"), "Причина "),
                Pair(arrayOf("kolpacks"), "Упаковок "),
                Pair(arrayOf("stel"), "Стеллаж "),
                Pair(arrayOf("cell"), "Ячейка ")
            ).forEach { pair ->
                val presenterBinding = TemplatePresenterBinding.inflate(
                    layoutInflater,
                    itemBinding.containerVertical,
                    false
                )
                presenterBinding.setAttribute(pair, itemData)
                itemBinding.containerVertical.addView(presenterBinding.root)
            }
            // endregion

            // region Отображение катушек (coils)
            if (itemData.coils.isNotEmpty()) {
                // Создаём HorizontalScrollView
                val horizontalScrollView = HorizontalScrollView(holder.itemView.context)
                horizontalScrollView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                horizontalScrollView.setPadding(0, 16, 0, 0)

                // Контейнер для катушек
                val coilsContainer = LinearLayout(holder.itemView.context)
                coilsContainer.orientation = LinearLayout.HORIZONTAL
                coilsContainer.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Для каждой катушки создаём View
                itemData.coils.forEach { coil ->
                    val coilView = LinearLayout(holder.itemView.context)
                    coilView.orientation = LinearLayout.VERTICAL
                    coilView.setPadding(8, 4, 8, 4)

                    // TextView для type
                    val tvType = TextView(holder.itemView.context).apply {
                        text = coil.type
                        setTextColor(Color.BLACK)
                        textSize = 14f
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    // TextView для num
                    val tvNum = TextView(holder.itemView.context).apply {
                        text = "№${coil.num}"
                        setTextColor(Color.GRAY)
                        textSize = 12f
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    // region Логика подсветки катушки
                    if (coil.isScanned) {
                        coilView.setBackgroundColor(
                            ContextCompat.getColor(coilView.context, R.color.yellow_highlight)
                        )
                        // tvNum.setTextColor(ContextCompat.getColor(tvNum.context, R.color.red_text)) // доп. акцент
                    } else {
                        coilView.background = null
                        tvNum.setTextColor(Color.GRAY)
                    }
                    // endregion

                    coilView.addView(tvType)
                    coilView.addView(tvNum)
                    coilsContainer.addView(coilView)
                }

                horizontalScrollView.addView(coilsContainer)
                itemBinding.containerVertical.addView(horizontalScrollView)
            }
            // endregion
            if (paramValue == "towh" || paramValue == "inisosklad") {
                // region Логика подсветки всего элемента
                if (itemData.isScanned) {
                    itemBinding.containerVertical.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.yellow_highlight)
                    )
                } else {
                    itemBinding.containerVertical.background = null
                }
                // endregion

                // region Выделение текущего элемента (зелёная рамка)
                if (position == selectedPosition) {
                    itemBinding.root.setBackgroundResource(R.drawable.bg_green_border)
                } else {
                    itemBinding.root.background = null
                }
                // endregion
            }
            else {
                itemBinding.root.background = null
                itemBinding.containerVertical.background = null
            }
//            itemBinding.containerVertical.setOnClickListener {
//                isolatorListViewModel.mainActivityRouter.navigate(
//                    DryFragmentInfo::class.java,
//                    Bundle().apply {
//                        putSerializable(IsolatorListFragmentInfo.PARAM, itemData.id)
//                    }
//                )
//            }
        }

    }

    sealed class IsolatorListFragmentState<out T:Any> {


        data class Error(private var _exception: Throwable?) : IsolatorListFragmentState<Nothing>(){
            val exception: Throwable?
                get() {
                    val tmp=_exception
                    _exception=null
                    return tmp
                }
        }
        data class Success<out T : Any>(private var _data: T?) : IsolatorListFragmentState<T>(){
            val data:T?
                get() {
                    val tmp=_data
                    _data=null
                    return tmp
                }
        }
        data object Idle:IsolatorListFragmentState<Nothing>()
    }

    class IsolatorListViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref

    ) : BaseViewModel() {

        var skipNextPut = false
            private set
        var lastStoredStel: String = ""
        var lastStoredCell: String = ""
        fun isolatorListSearch(SkladID: Int, rgm: String,Reason: String) {
            ioCoroutineScope.launch {
                // Очищаем старое состояние перед запросом
                isolatorListFragmentState.postValue(IsolatorListFragmentState.Idle)

                when (val token = loginRepository.user?.token) {
                    null -> isolatorListFragmentState.postValue(IsolatorListFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken))
                    else -> {
                        val result = apiPantes.isolatorListSearch(token,  SkladID, Reason, rgm)
                        when (result) {
                            is ApiPantes.ApiState.Success -> {
                                // Отправляем новый результат
                                isolatorListFragmentState.postValue(IsolatorListFragmentState.Success(result.data))
                            }
                            is ApiPantes.ApiState.Error -> {
                                isolatorListFragmentState.postValue(IsolatorListFragmentState.Success(null))
                            }
                        }
                    }
                }
               // _searchCompleted.postValue(true)

            }
        }

        suspend fun put2WH(num: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolPut2WH( num,token)) {
                    is ApiPantes.ApiState.Success -> Result.Success(Unit) // Возвращаем Unit
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }


        suspend fun put2WHisoSklad(num: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolPut2WHisoSklad( num,token)) {
                    is ApiPantes.ApiState.Success -> Result.Success(Unit) // Возвращаем Unit
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }

        suspend fun getAllID(num: String): Result<ArrayList<Int>> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolGetIDAll(token, num)) {
                    is ApiPantes.ApiState.Success -> {
                        if (result.data.isNotEmpty()) {
                            Result.Success(result.data)
                        } else {
                            Result.Failure(Exception("Empty response"))
                        }
                    }

                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }



        fun saveStelAndCell(stel: String, cell: String) {
            lastStoredStel = stel
            lastStoredCell = cell
        }

        fun clearStelAndCell() {
            lastStoredStel = ""
            lastStoredCell = ""
        }

        companion object {
            fun getInstance(context: Context): IsolatorListViewModel {
                return   IsolatorListViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }

        val isolatorListFragmentReady=
            MutableLiveData<Int>()
        val isolatorListFragmentEmpty=
            MutableLiveData<Int>()
        val isolatorListFragmentTitle=
            MutableLiveData<String>()
        val isolatorListFragmentSubtitle=
            MutableLiveData<String>()
        private val _searchCompleted = MutableLiveData<Boolean>()
        val searchCompleted: LiveData<Boolean> = _searchCompleted
        val isolatorListFragmentState=

            MutableLiveData<IsolatorListFragmentState<*>>()
        val refreshListEvent = MutableLiveData<Unit>()
        val isolatorFragmentSearchResponse=
            MutableLiveData<IsolatorListSearchResponse>()

    }
    sealed class Result<out T : Any> {
        data class Success<out T : Any>(val data: T) : Result<T>()
        data class Failure(val exception: Throwable) : Result<Nothing>()
    }

    sealed class IsolatorListFragmentFormState<out T:Any>
    {

        data class Error(var exception: Throwable): IsolatorListFragmentFormState<Nothing>()
    }

}