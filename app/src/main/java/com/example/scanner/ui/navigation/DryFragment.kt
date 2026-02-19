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
import com.example.scanner.models.DrySearchResponse
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
import timber.log.Timber
import kotlin.math.abs

class DryFragment: BaseFragment() {
    companion object{
        const val PARAM="param"
    }
    private var paramValue: String = ""
    private var needscroll: Boolean = true
    private lateinit var toolbarlnk: androidx.appcompat.widget.Toolbar
    private lateinit var recyclerView: RecyclerView
    private val dryViewModel: DryViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterdry=
        Adapterdry()


    private val sViewModel: SessionViewModel by viewModels { viewModelFactory }
    private lateinit var infoTextView : TextView
    private var box: Int = 0
    private var IDAll: String = ""
    private var curNum : String? = ""
    var oldSize = 0

    private lateinit var soundHelper: SoundHelper

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
    private fun handleScan(id: Int) {
        sViewModel.addItem(id)
    }
    //    private fun showRemoveFromBoxDialog(item: InControlSearchResponse.Item, position: Int) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Убрать из коробки?")
//            .setMessage("Вы уверены, что хотите убрать элемент из коробки?")
//            .setPositiveButton("Да") { _, _ ->
//                lifecycleScope.launch {
//                    try {
//                        val result = incontrolViewModel.incontrolRemoveFromBox(item.IDAll, box)
//                        when (result) {
//                            is Result.Success -> {
//                                adapterincontrol.removeItem(position)
//                                showResponse("Элемент убран из коробки")
//                            }
//                            is Result.Failure -> showError(result.exception)
//                        }
//                    } catch (e: Exception) {
//                        showError(e)
//                    }
//                }
//            }
//            .setNegativeButton("Нет") { dialog, _ ->
//                // Возвращаем элемент на место (перерисовываем)
//                adapterincontrol.notifyItemChanged(position)
//                dialog.dismiss()
//            }
//            .show()
//    }
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
                                dryViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
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
                                dryViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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
                    "toDry" -> toolbar.title = "На сушку"
                    "fromDry" -> toolbar.title = "Забрать с сушки"
                    "listDry" -> toolbar.title = "Что на сушке"  // или "В коробку 0", если нужно
                    else -> toolbar.title = "Неизвестный режим"
                }


                dryViewModel.dryFragmentSubtitle
                    .observe(viewLifecycleOwner){
                        toolbar.subtitle=it
                    }
                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply
                        {
                            if (paramValue=="listDry" || paramValue=="fromDry") {
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

                            // Настраиваем RecyclerView
                            recycler.adapter = adapterdry
                            recycler.layoutManager = LinearLayoutManager(requireContext())
                            recycler.setHasFixedSize(true) // опционально
                            // Подключаем ItemTouchHelper
                            itemTouchHelper.attachToRecyclerView(recycler)
                            // region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater, containerContent, false)
                                    .root
                                    .apply {
                                        dryViewModel.dryFragmentEmpty
                                            .observe(viewLifecycleOwner) { this.visibility = it }
                                        dryViewModel.dryFragmentEmpty.postValue(View.GONE)
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
        if (paramValue=="listDry") {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Выберите фильтр")
                .setAdapter(

                    ArrayAdapter(
                        requireContext(),
                        R.layout.dialog_list_item,
                        arrayOf("Все", "По номеру печи", "По состоянию", "По действию")
                    )

                ) { _, which ->
                    adapterdry.resetContent()
                    dryViewModel.dryFragmentState.postValue(DryFragmentState.Idle)

                    when (which) {
                        0 -> {
                            dryViewModel.drySearch(0, "", 0, "", paramValue)
                            toolbarlnk.title= "Забрать из печи"
                        }
                        1 -> showOvenNumberDialog()
                        2 -> showStateDialog()
                        3 -> showActionDialog()
                    }
                }

                .show()
        }
        if (paramValue=="fromDry") {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Выберите фильтр")
                .setAdapter(

                    ArrayAdapter(
                        requireContext(),
                        R.layout.dialog_list_item,
                        arrayOf("Все", "По номеру печи")
                    )

                ) { _, which ->
                    adapterdry.resetContent()
                    dryViewModel.dryFragmentState.postValue(DryFragmentState.Idle)

                    when (which) {
                        0 -> {
                            dryViewModel.drySearch(0, "", 0, "", paramValue)
                            toolbarlnk.title= "Забрать из печи"
                        }
                        1 -> showOvenNumberDialog()
                    }
                }

                .show()
        }
    }

    private fun showOvenNumberDialog() {
        val ovenNumbers = (1..5).map { it.toString() }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите номер печи")
            .setAdapter(
                ArrayAdapter(requireContext(), R.layout.dialog_list_item, ovenNumbers.toTypedArray())
            ) { _, which ->
                adapterdry.resetContent()
                val oven = ovenNumbers[which].toInt()
                toolbarlnk.title= "Печь $oven"
                dryViewModel.drySearch(0, "", oven , "",paramValue)
            }
            .show()
    }

    private fun showStateDialog() {
        val states = listOf("Отмена сушки", "Просушено", "Идёт сушка", "Сушка закончена")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите состояние")
            .setAdapter(
                ArrayAdapter(requireContext(), R.layout.dialog_list_item, states.toTypedArray())
            ) { _, which ->
                val state = states[which]
                adapterdry.resetContent()
                toolbarlnk.title= state
                dryViewModel.drySearch(0, state, 0, "",paramValue)
            }
            .show()
    }

    private fun showActionDialog() {
        val actions = listOf("Переместить по месту хранения", "Не требуется")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите действие")
            .setAdapter(
                ArrayAdapter(requireContext(), R.layout.dialog_list_item, actions.toTypedArray())
            ) { _, which ->
                val action = actions[which]
                adapterdry.resetContent()
                toolbarlnk.title= action
                dryViewModel.drySearch(0, "", 0, action,paramValue)
            }
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        soundHelper = SoundHelper(requireContext())
        dryViewModel.refreshListEvent.observe(viewLifecycleOwner) {
            // Перезагружаем данные списка
            adapterdry.resetContent()
            dryViewModel.drySearch(0, "", box, "",paramValue)

        }
        sViewModel.scannedItems.observe(viewLifecycleOwner) { scanned ->
            adapterdry.updateItems(scanned) // Перекрашиваем все элементы
        }
        dryViewModel.searchCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if  (needscroll) {
                if (isCompleted && curNum!!.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            when (val result = dryViewModel.getAllID(curNum!!)) {
                                is Result.Success -> {
                                    IDAll = result.data.toString()
                                    val position = adapterdry.findPosition(IDAll)
                                    if (position != null && position != -1) {

                                        adapterdry.scrollToPosition(position, recyclerView)

                                    } else {
                                        // showResponse("Элемент с IDAll=$IDAll не найден в списке")
                                    }
                                }

                                is Result.Failure -> showError(result.exception)
                            }
                        } catch (e: Exception) {
                            showError(e)
                        }
                        // Сброс флага для будущих вызовов
                        dryViewModel.resetSearchCompleted()
                    }
                }

            }
        }
        dryViewModel.dryFragmentState.observe(viewLifecycleOwner)
        {
            when(val state=it){
                is DryFragmentState.Error ->{
                    state.exception?.let {exception->
                        findNavController().navigateUp()
                        dryViewModel.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    exception
                                )
                            })
                    }
                }
                is DryFragmentState.Success ->{
                    state.data?.let { drySearchResponse->
                        drySearchResponse as DrySearchResponse

                        if (adapterdry.isResetContent) {
                            infoTextView.visibility = View.GONE
                            dryViewModel.dryFragmentTitle.postValue(
                                getString(R.string.format_title, "${drySearchResponse.total}")
                            )
                            dryViewModel.dryFragmentEmpty.postValue(
                                if (drySearchResponse.found.isEmpty()) View.VISIBLE else View.GONE
                            )
                            adapterdry.setContent(drySearchResponse)
                            // ОБЯЗАТЕЛЬНО: уведомить адаптер об обновлении
                            //adapterdry.notifyDataSetChanged()
                        } else {
                            adapterdry.appendContent(drySearchResponse)
                            //adapterdry.notifyDataSetChanged()
                        }

                    }

                }
                is DryFragmentState.Idle->{
                    dryViewModel.dryFragmentTitle
                        .postValue(getString(R.string.vk_button))

                    dryViewModel.dryFragmentReady.postValue(
                        when{
                            getArgument<String?>(PARAM).isNullOrEmpty()-> View.VISIBLE
                            else-> View.GONE
                        }
                    )

                    dryViewModel.dryFragmentEmpty.postValue(
                        when{
                            !getArgument<String?>(PARAM).isNullOrEmpty()
                                    && adapterdry.itemCount==0 -> View.VISIBLE
                            else-> View.GONE
                        }
                    )
                }
            }

            if (it!= DryFragmentState.Idle){
                dryViewModel.dryFragmentState.postValue(
                    DryFragmentState.Idle
                )
            }


        }


        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
            when(paramValue){
                "toDry" -> {
                    when(val stateScan=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0ScanTo(stringScanResult)
                                    stringScanResult.startsWith('d') -> handleDScanTo(stringScanResult)
                                    //  (stringScanResult.split('$')).size == 5 -> handleCScanBottle(stringScanResult)
                                    else -> showErrorMessageQR()
                                }
                            }
                        }
                        else->{}
                    }
                }
                "fromDry" -> {
                    when(val stateScan=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0ScanFrom(stringScanResult)
                                    stringScanResult.startsWith('C') -> handleCScanFrom(stringScanResult)
                                    stringScanResult.startsWith('d') -> handleDScanFrom(stringScanResult)
                                    //  (stringScanResult.split('$')).size == 5 -> handleCScanBottleFrom(stringScanResult)
                                    else -> showErrorMessageQR()
                                }
                            }
                        }
                        else->{}
                    }
                }
                "listDry" -> {
                    when(val stateScan=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0ScanTo(stringScanResult)
                                    //      stringScanResult.startsWith('C') -> handleCScanFrom(stringScanResult)
                                    //      stringScanResult.startsWith('d') -> handleDScanFrom(stringScanResult)
                                    //  (stringScanResult.split('$')).size == 5 -> handleCScanBottleFrom(stringScanResult)
                                    else -> showErrorMessageQR()
                                }
                            }



                        }
                        else->{}
                    }
                }
                else->{}
            }

            dryViewModel.dryFragmentState.postValue(
                DryFragmentState.Idle
            )

        }
        dryViewModel.drySearch(0,"",box,"",paramValue)
    }
    private fun handle3N0ScanTo(stringScanResult: String) {


        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            val num = parts[1]
            curNum = num
            // ЗАПУСКАЕМ КОРУТИНУ ДЛЯ АСИНХРОННОГО ВЫЗОВА
            lifecycleScope.launch {
                when (val result = dryViewModel.getAllID(num)) {
                    is Result.Success -> {
                        val IDAll = result.data
                        // Теперь можно работать с полученным списком

                        handleIDAllList(IDAll,0)
                        dryViewModel.refreshListEvent.postValue(Unit)
                    }

                    is Result.Failure -> {
                        showError(result.exception)
                    }
                }

            }
        }
    }


    private fun handleDScanTo(stringScanResult: String) {
        val parts = stringScanResult.split('$')
        if (IDAll == "") {
            showResponse("Сначала отсканируйте упаковку")
        } else{
            if (parts.size > 1) {
                box = parts[1].toInt()
                val currentItem = adapterdry.getItemByID(IDAll.toInt())
                if (currentItem!!.Cab != box) {
                    showResponse("Выбрана не верная печь")
                } else {
                    adapterdry.resetContent()
                    dryViewModel.drySearch(currentItem.IDAll, "", 0, "", paramValue)
                }
            }
        }
    }

    private fun handle3N0ScanFrom(stringScanResult: String) {


        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            val num = parts[1]
            curNum = num
            needscroll = true
            // ЗАПУСКАЕМ КОРУТИНУ ДЛЯ АСИНХРОННОГО ВЫЗОВА
            lifecycleScope.launch {
                when (val result = dryViewModel.getAllID(num)) {
                    is Result.Success -> {
                        IDAll = result.data.toString()
                        // Теперь можно работать с полученным списком

                        handleIDAllList(IDAll.toInt() , curNum!!.toInt())
                        // dryViewModel.refreshListEvent.postValue(Unit)
                    }

                    is Result.Failure -> {
                        showError(result.exception)
                    }
                }

            }
        }
    }

    private fun handleCScanFrom(stringScanResult: String) {
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
                val currentItem =  adapterdry.getItemByID(IDAll.toInt())

                val isMatch = (currentItem?.Stel.toString() == stel) && (currentItem?.Yach  == yach)

                updateInfoTextView(isMatch, false)

                if (isMatch) {
                    needscroll = false
                    lifecycleScope.launch {
                        when (val result = curNum?.let { dryViewModel.getAllID(it) }) {
                            is Result.Success<*> -> {
                                oldSize = adapterdry.itemCount
                                if (currentItem != null) {
                                    dryViewModel.putFromDry2WH(currentItem.IDResSub,  currentItem.id,curNum!!.toInt())
                                    soundHelper.playSuccessSound()
                                }
                                dryViewModel.refreshListEvent.postValue(Unit)
                                resetScanState()
                            }

                            is Result.Failure -> {
                                showError(result.exception)
                            }

                            null -> TODO()
                        }
                        dryViewModel.saveStelAndCell(stel,yach)
                    }
                } else {
                    dryViewModel.clearStelAndCell()
                }

            } else {
                showResponse("QR-код после 'C' должен содержать 12 цифр, получено: ${content.length}")
            }
        }
    }

    private fun handleDScanFrom(stringScanResult: String) {
        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            box = parts[1].toInt()
            toolbarlnk.title= "Забрать с сушки"
            adapterdry.resetContent()
            dryViewModel.drySearch(0, "", box, "",paramValue)
        }
    }
    private fun resetScanState() {
        IDAll = ""
        curNum = ""
        needscroll = true
    }
    private fun updateInfoTextView(isMatch: Boolean,ftime: Boolean) {
        if (!ftime) {
            infoTextView.visibility = View.VISIBLE
            if (isMatch) {
                infoTextView.setBackgroundColor(Color.argb(255, 0, 255, 0)) // Зелёный

            } else {
                infoTextView.setBackgroundColor(Color.argb(255, 255, 0, 0)) // Красный
                val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
            }
        }
        else{
            infoTextView.visibility = View.GONE
        }
    }
    private fun showError(exception: Throwable) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ошибка")
            .setMessage(exception.message ?: "Произошла неизвестная ошибка")
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun showErrorMessageQR() {
        Toast.makeText(requireContext(), "Неподдерживаемый формат QR-кода", Toast.LENGTH_LONG).show()
    }
    private fun showResponse(response: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Комментарий")
            .setMessage(response)
            .setNegativeButton("Закрыть", null)
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleIDAllList(IDAll: Int,num: Int) {

        val lastStel = dryViewModel.lastStoredStel
        val lastCell = dryViewModel.lastStoredCell
        var ftime = false
        val currentItem = adapterdry.getItemByID(IDAll)
        if (currentItem == null) {
            showResponse("Элемент не найден")
            return
        }
        val stel = currentItem.Stel.toString()
        val cell = currentItem.Yach
        if (lastStel == "") ftime = true

        val position = adapterdry.findPosition(IDAll.toString())
        if (position != null && position != -1) {
            adapterdry.scrollToPosition(position, recyclerView)
            handleScan(num)
        }

        if (lastStel.isNotEmpty() && lastCell.isNotEmpty()) {
            val isMatch = (lastStel == stel) && (lastCell == cell)
            updateInfoTextView(isMatch, ftime)

            if (isMatch) {
                dryViewModel.putFromDry2WH(currentItem.IDResSub, currentItem.id, curNum!!.toInt())
                soundHelper.playSuccessSound()
                dryViewModel.refreshListEvent.postValue(Unit)
                resetScanState()
                return  // <-- выходим, так как список скоро обновится
            } else {
                dryViewModel.clearStelAndCell()
            }
        }
    }
    inner class Adapterdry: BaseRecyclerAdapter<DrySearchResponse>(DrySearchResponse()) {
        private var hasItemCountDecreased = false
        private var selectedPosition: Int = -1 // -1 = ничего не выделено



        override fun setContent(dataNew: DrySearchResponse) {
            selectedPosition = -1
            val newSize = getNewSize(dataNew)  // Получаем размер нового списка
            val scanned = sViewModel.scannedItems.value ?: emptySet()

            hasItemCountDecreased = newSize < oldSize
            oldSize = newSize // Устанавливаем флаг
            if (hasItemCountDecreased) {
                dryViewModel.lastStoredCell = ""
                dryViewModel.lastStoredStel = ""
            }


            dataNew.found.forEach { item ->
                item.coils.forEach { coil ->
                    coil.isScanned = scanned.contains(coil.num)
                }
                item.isScanned = item.coils.all { it.isScanned }
            }

            super.setContent(dataNew) // Передаем данные базовому адаптеру
        }

        private fun getNewSize(newData: DrySearchResponse): Int {
            return newData.found.size  // Аналогично, зависит от структуры DrySearchResponse
        }
        override fun getCallback(dataOld: DrySearchResponse?): DiffUtil.Callback {
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

                    return oldItem.id == newItem.id
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
        @SuppressLint("NotifyDataSetChanged")
        override fun appendData(dataNew: DrySearchResponse) {
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

        fun findPosition(idAll: String): Int? {
            val targetId = idAll.toInt()
            val index = data.found.indexOfFirst { it.id == targetId }
            return if (index != -1) index else null
        }


        fun getItemByID(id: Int): DrySearchResponse.Item? {
            return data.found.firstOrNull { it.id == id }
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

        override fun cloneData(): DrySearchResponse {
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
            if (position >= data.found.size) {
                Timber.e("Invalid position $position, size=${data.found.size}")
                return
            }
            // Очищаем контейнеры
            itemBinding.containerVertical.removeAllViews()
            itemBinding.containerHorizon.removeAllViews()

            // region Заполняем основные поля (через TemplatePresenterBinding)
            arrayOf(
                Pair(arrayOf("SkladID"), "# компонента "),
                Pair(arrayOf("Naim"), "Наименование "),
                Pair(arrayOf("ActionNme"), "Действие "),
                Pair(arrayOf("Sost"), "Состояние "),
                Pair(arrayOf("kol"), "Кол-во "),
                Pair(arrayOf("Stel"), "Стеллаж "),
                Pair(arrayOf("Yach"), "Ячейка "),
                Pair(arrayOf("Cab"), "№ печки "),
                Pair(arrayOf("DryTmeOst"), "Осталось сушить "),
                Pair(arrayOf("kolpacks"), "Упаковок ")
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
            if (paramValue == "fromDry") {
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
            itemBinding.containerVertical.setOnClickListener {
                dryViewModel.mainActivityRouter.navigate(
                    DryFragmentInfo::class.java,
                    Bundle().apply {
                        putSerializable(DryFragmentInfo.PARAM, itemData.id)
                    }
                )
            }
        }

    }

    sealed class DryFragmentState<out T:Any> {


        data class Error(private var _exception: Throwable?) : DryFragmentState<Nothing>(){
            val exception: Throwable?
                get() {
                    val tmp=_exception
                    _exception=null
                    return tmp
                }
        }
        data class Success<out T : Any>(private var _data: T?) : DryFragmentState<T>(){
            val data:T?
                get() {
                    val tmp=_data
                    _data=null
                    return tmp
                }
        }
        data object Idle:DryFragmentState<Nothing>()
    }

    class DryViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref

    ) : BaseViewModel() {


        var lastStoredStel: String = ""
        var lastStoredCell: String = ""
        fun drySearch(SkladID: Int, Sost: String, cab: Int, ActionNme: String,rgm: String) {
            ioCoroutineScope.launch {
                // Очищаем старое состояние перед запросом
                dryFragmentState.postValue(DryFragmentState.Idle)

                when (val token = loginRepository.user?.token) {
                    null -> dryFragmentState.postValue(DryFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken))
                    else -> {
                        val result = apiPantes.drySearch(token, cab, SkladID, Sost, ActionNme,rgm)
                        when (result) {
                            is ApiPantes.ApiState.Success -> {
                                // Отправляем новый результат
                                dryFragmentState.postValue(DryFragmentState.Success(result.data))
                            }
                            is ApiPantes.ApiState.Error -> {
                                dryFragmentState.postValue(DryFragmentState.Success(null))
                            }
                        }
                    }
                }
                _searchCompleted.postValue(true)

            }
        }
        fun putFromDry2WH(IdresSub : Int, id: Int, num: Int) {
            ioCoroutineScope.launch {
                when(val token=loginRepository.user?.token){
                    null-> DryFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                    else->{
                        when (val result = apiPantes.dryPut2WH(IdresSub, id, num, token)){
                            is ApiPantes.ApiState.Success->

                                dryFragmentState.postValue(DryFragmentState.Success(result.data))
                            is ApiPantes.ApiState.Error->
                                DryFragmentFormState.Error(result.exception)
                        }
                    }
                }
            }
        }


        suspend fun getAllID(num: String): Result<Int> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.dryGetID(token, num)) {
                    is ApiPantes.ApiState.Success -> {
                        if (result.data > 0) {
                            Result.Success(result.data)
                        } else {
                            Result.Failure(Exception("Нет такого элемента в списке"))
                        }
                    }

                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }


        fun resetSearchCompleted() {
            _searchCompleted.value = false
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
            fun getInstance(context: Context): DryViewModel {
                return   DryViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }

        val dryFragmentReady=
            MutableLiveData<Int>()
        val dryFragmentEmpty=
            MutableLiveData<Int>()
        val dryFragmentTitle=
            MutableLiveData<String>()
        val dryFragmentSubtitle=
            MutableLiveData<String>()
        private val _searchCompleted = MutableLiveData<Boolean>()
        val searchCompleted: LiveData<Boolean> = _searchCompleted
        val dryFragmentState=

            MutableLiveData<DryFragmentState<*>>()
        val refreshListEvent = MutableLiveData<Unit>()


    }
    sealed class Result<out T : Any> {
        data class Success<out T : Any>(val data: T) : Result<T>()
        data class Failure(val exception: Throwable) : Result<Nothing>()
    }

    sealed class DryFragmentFormState<out T:Any>
    {

        data class Error(var exception: Throwable): DryFragmentFormState<Nothing>()
    }

}