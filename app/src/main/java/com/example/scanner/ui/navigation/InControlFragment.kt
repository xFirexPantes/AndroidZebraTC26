package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import timber.log.Timber

class InControlFragment: BaseFragment() {
    companion object{
        const val PARAM="param"
    }

    private var paramValue: String? = ""
    private var curNum : String? = ""
    private lateinit var toolbarlnk: androidx.appcompat.widget.Toolbar
    private lateinit var recyclerView: RecyclerView
    private val incontrolViewModel: InControlViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterincontrol=
        Adapterincontrol()
    private lateinit var infoTextView : TextView
    private var msg: String = ""
    private var box: Int = 0
    private var IDAll: String = ""
    private var curIDAll: String = ""
    private var curPrim: String = ""
    private var action15: Boolean = false
    private var action23: Boolean = false
    private var isbottle: Boolean = false


    sealed class Back2SkladState<out T : Any> {
        data class Success(val message: String) : Back2SkladState<String>()
        data class CheckST(val IDAll: String,val isOk: String,val action15: Boolean,val action23: Boolean) : Back2SkladState<String>()
        data class Put2Box(val isOk: String) : Back2SkladState<String>()
        data class Put2WH(val isOk: String) : Back2SkladState<String>()
        data class TakeBox(val isOk: String) : Back2SkladState<String>()
        data class Error(val exception: Throwable) : Back2SkladState<Nothing>()
        data object Idle : Back2SkladState<Nothing>()
    }
    private val itemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0, 0) {
            // Отключаем автоматическое удаление
            override fun getSwipeThreshold(viewHolder: ViewHolder): Float = 1f

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                // НЕ удаляем элемент автоматически!
                // Вместо этого — показываем диалог
                val position = viewHolder.adapterPosition
                val item = adapterincontrol.data.found[position]
                if (paramValue in listOf("toBox", "WHtoIncontrol")) {
                    showRemoveFromBoxDialog(item, position)
                }
            }

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
                val alpha = 1 - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                viewHolder.itemView.alpha = alpha
            }
        }
    )
    private fun showRemoveFromBoxDialog(item: InControlSearchResponse.Item, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Убрать из коробки?")
            .setMessage("Вы уверены, что хотите убрать элемент из коробки?")
            .setPositiveButton("Да") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val result = incontrolViewModel.incontrolRemoveFromBox(item.IDAll, box)
                        when (result) {
                            is Result.Success -> {
                                adapterincontrol.removeItem(position)
                                showResponse("Элемент убран из коробки")
                            }
                            is Result.Failure -> showError(result.exception)
                        }
                    } catch (e: Exception) {
                        showError(e)
                    }
                }
            }
            .setNegativeButton("Нет") { dialog, _ ->
                // Возвращаем элемент на место (перерисовываем)
                adapterincontrol.notifyItemChanged(position)
                dialog.dismiss()
            }
            .show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)
        paramValue = arguments?.getString(PARAM)
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
                                incontrolViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
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
                when (paramValue) {
                    "WHtoIncontrol" -> toolbar.title = "ОТправить на ВК"
                    "toIncontrol" -> toolbar.title = "Принять на ВК"
                    "toBox" -> toolbar.title = "В коробку"  // или "В коробку 0", если нужно
                    "toWH" -> toolbar.title = "Принять на склад"
                    else -> toolbar.title = "Неизвестный режим"
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
                    TemplateRecyclerBinding.inflate(inflater, root, false)
                        .apply {
                            // Сохраняем ссылку на RecyclerView из текущего binding
                            recyclerView = recycler

                            // Настраиваем RecyclerView
                            recycler.adapter = adapterincontrol
                            recycler.layoutManager = LinearLayoutManager(requireContext())
                            recycler.setHasFixedSize(true) // опционально
                            // Подключаем ItemTouchHelper
                            itemTouchHelper.attachToRecyclerView(recycler)
                            // region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater, containerContent, false)
                                    .root
                                    .apply {
                                        incontrolViewModel.incontrolFragmentEmpty
                                            .observe(viewLifecycleOwner) { this.visibility = it }
                                        incontrolViewModel.incontrolFragmentEmpty.postValue(View.GONE)
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

            is Back2SkladState.Put2Box -> {
                showResponse(state.isOk)
            }

            is Back2SkladState.TakeBox -> {
                showResponse(state.isOk)
            }

            is Back2SkladState.Put2WH -> {
                showResponse(state.isOk)
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
            incontrolViewModel.incontrolSearch(paramValue!!, "",box)

        }
        incontrolViewModel.searchCompleted.observe(viewLifecycleOwner) { isCompleted ->
            if (isCompleted && curNum!!.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val result :  Result< Back2SkladState. CheckST>
                        if (isbottle) {
                            result = incontrolViewModel.checkst("bottle"+curNum!!)
                           }
                        else{
                            result  = incontrolViewModel.checkst(curNum!!)
                        }
                        when (result) {
                            is Result.Success -> {
                                IDAll = result.data.IDAll
                                val position = adapterincontrol.findPosition(IDAll)
                                if (position != null && position != -1) {
                                    adapterincontrol.scrollToPosition(position, recyclerView)
                                } else {
                                    showResponse("Элемент с IDAll=$IDAll не найден в списке")
                                }
                            }
                            is Result.Failure -> showError(result.exception)
                        }
                    } catch (e: Exception) {
                        showError(e)
                    }
                    // Сброс флага для будущих вызовов
                    incontrolViewModel.resetSearchCompleted()
                }
            }
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

            if (box == 0 && (paramValue!="toIncontrol" && paramValue!="WHtoIncontrol")){
                Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
            }
            adapterincontrol.onRemoveClick = { item, position ->
                showRemoveFromBoxDialog(item, position)
            }
        }




        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner) {
            when (paramValue) {
                "WHtoIncontrol" -> {
                    when (val stateScan = it) {
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult -> {
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan1(
                                        stringScanResult
                                    )

                                    stringScanResult.startsWith('z') -> handleZScan1(
                                        stringScanResult
                                    )

                                    (stringScanResult.split('$')).size == 5 -> handleScanBottle1(
                                        stringScanResult
                                    )

                                    else -> showResponse("Неизвестный QR")
                                }
                            }
                        }

                        else -> {}
                    }
                }

                "toIncontrol" -> {
                    when (val stateScan = it) {
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult -> {
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan2(
                                        stringScanResult
                                    )

                                    stringScanResult.startsWith('z') -> handleZScan2(
                                        stringScanResult
                                    )

                                    (stringScanResult.split('$')).size == 5 -> handleScanBottle2(
                                        stringScanResult
                                    )

                                    else -> showResponse("Неизвестный QR")
                                }
                            }
                        }

                        else -> {}
                    }
                }

                "toBox" -> {
                    when (val stateScan = it) {
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult -> {
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan3(
                                        stringScanResult
                                    )

                                    stringScanResult.startsWith('z') -> handleZScan3(
                                        stringScanResult
                                    )

                                    (stringScanResult.split('$')).size == 5 -> handleScanBottle3(
                                        stringScanResult
                                    )

                                    else -> showResponse("Неизвестный QR")
                                }
                            }
                        }

                        else -> {}
                    }
                }

                "toWH" -> {
                    when (val stateScan = it) {
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult -> {
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan4(
                                        stringScanResult
                                    )

                                    stringScanResult.startsWith('z') -> handleZScan4(
                                        stringScanResult
                                    )
                                    stringScanResult.startsWith('C') -> handleCScan4(
                                        stringScanResult
                                    )

                                    (stringScanResult.split('$')).size == 5 -> handleScanBottle4(
                                        stringScanResult
                                    )

                                    else -> showResponse("Неизвестный QR")
                                }
                            }
                        }

                        else -> {}
                    }
                }

                else -> {}
            }

            incontrolViewModel.incontrolFragmentState.postValue(
                InControlFragmentState.Idle
            )

        }
        incontrolViewModel.incontrolSearch(paramValue!!,"",box)
    }
    fun showPrimInputDialog( onConfirm: (String) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT  // или нужный тип ввода
        val dtValue = adapterincontrol.findDtByIdAll(IDAll)
        var actions = ""
        if (action15) {
            actions += "\nТест на паяемость и теплостойкость при пайке"
        }
        if (action23) {
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
    fun handleIDAllList(idAllList: ArrayList<Int>) {
        val lastStel = incontrolViewModel.lastStoredStel
        val lastCell = incontrolViewModel.lastStoredCell
        val firstIdAll = idAllList.firstOrNull()
        IDAll = firstIdAll.toString()
        val currentItem = adapterincontrol.findByIdAll(IDAll)
        if (currentItem == null) {
            showResponse("Элемент не найден")
            return
        }
        val stel = currentItem.rack
        val cell = currentItem.cell
        if (firstIdAll != null) {
            val position = adapterincontrol.findPosition(firstIdAll.toString())
            if (position != null && position != -1) {
                adapterincontrol.scrollToPosition(position,recyclerView)
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

                lifecycleScope.launch {
                    val putResult:   Result<Unit>
                    if (isbottle){
                        putResult  = incontrolViewModel.put2WH("bottle"+curNum!!)
                    }
                    else {
                        putResult =  incontrolViewModel.put2WH(curNum!!)
                    }


                    when (putResult) {
                        is Result.Success<Unit> -> {
                            // Успех: запрашиваем обновление списка
                            incontrolViewModel.refreshListEvent.postValue(Unit)
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
                incontrolViewModel.clearStelAndCell()
            }

        }


    }
    private fun handle3N0Scan1(stringScanResult: String) {
        isbottle = false
        if (box == 0){
            Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
        }
        else {
            val parts = stringScanResult.split('$')
            if (parts.size > 1) {
                val num = parts[1]
                curNum = num


                // 2. Вызываем WHtoBox()

                lifecycleScope.launch {
                    val putResult = incontrolViewModel.WHtoBox(num, box)

                    when (putResult) {
                        is Result.Success<Unit> -> {
                            // Успех: запрашиваем обновление списка
                            incontrolViewModel.refreshListEvent.postValue(Unit)
                        }
                        is Result.Failure -> {
                            // Ошибка: показываем сообщение
                            showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                        }
                    }
                }



            }
        }
    }

    private fun handleZScan1(stringScanResult: String) {
        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            box = parts[1].toInt()
            toolbarlnk.title= "В коробку $box"
            incontrolViewModel.incontrolSearch(paramValue!!, "", box)
        }
    }

    private fun handleScanBottle1(stringScanResult: String) {
        isbottle = true
        if (box == 0){
            Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
        }
        val parts = stringScanResult.split('$')
        // Номер катушки находится на 1‑й позиции (индекс 1)
        val num = parts[2]
        curNum = num


        // 2. Вызываем put2Box()

        lifecycleScope.launch {
            val putResult = incontrolViewModel.WHtoBox("bottle" + curNum, box)

            when (putResult) {
                is Result.Success<Unit> -> {
                    // Успех: запрашиваем обновление списка
                    incontrolViewModel.refreshListEvent.postValue(Unit)
                }
                is Result.Failure -> {
                    // Ошибка: показываем сообщение
                    showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                }
            }
        }
    }
    private fun handle3N0Scan2(stringScanResult: String) {
        isbottle = false
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
                                if (curIDAll == IDAll){
                                    val putResult = incontrolViewModel.back2Sklad(num, curPrim)
                                    when (putResult) {
                                        is Result.Success -> {
                                            // putResult.data — это уже готовая строка (isOk) от API
                                            if (putResult.data != "") {
                                                showResponse(
                                                    putResult.data
                                                )  // Показываем её
                                            }
                                            incontrolViewModel.refreshListEvent.postValue(Unit)
                                        }
                                        is Result.Failure -> {
                                            showError(putResult.exception)
                                        }
                                    }
                                }
                                else{
                                    showPrimInputDialog { prim ->
                                        lifecycleScope.launch {
                                            val putResult = incontrolViewModel.back2Sklad(num, prim)
                                            curIDAll = IDAll
                                            curPrim = prim
                                            when (putResult) {
                                                is Result.Success -> {
                                                    // putResult.data — это уже готовая строка (isOk) от API
                                                    if (putResult.data != "") {
                                                        showResponse(
                                                            putResult.data
                                                        )  // Показываем её
                                                    }
                                                    incontrolViewModel.refreshListEvent.postValue(Unit)
                                                }
                                                is Result.Failure -> {
                                                    showError(putResult.exception)
                                                }
                                            }
                                        }
                                    }
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

    private fun handleZScan2(stringScanResult: String) {
        // Проверяем, есть ли элементы в адаптере
        if (adapterincontrol.itemCount > 0) {
            // Показываем диалог подтверждения
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Подтверждение")
                .setMessage("Взять другую коробку?")
                .setPositiveButton("Да") { _, _ ->
                    val parts = stringScanResult.split('$')
                    if (parts.size > 1) {
                        box = parts[1].toInt()
                        toolbarlnk.title= "Коробка $box"
                    }
                    incontrolViewModel.takeboxfromWH(box)

                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {

            val parts = stringScanResult.split('$')
            if (parts.size > 1) {
                box = parts[1].toInt()
                toolbarlnk.title = "Коробка $box"
            }
            // Если список пуст — сразу вызываем takebox()
            incontrolViewModel.takeboxfromWH(box)

        }
    }

    private fun handleScanBottle2(stringScanResult: String) {
        isbottle = true
        val parts = stringScanResult.split('$')
        if (parts.size == 5)  {
            // Номер катушки находится на 1‑й позиции (индекс 1)
            val num = parts[2]
            lifecycleScope.launch {
                try {
                    // 1. Вызываем checkst() и ждём результата
                    val result = incontrolViewModel.checkst("bottle"+num)

                    when (result) {
                        is Result.Success -> {
                            msg = result.data.isOk
                            IDAll = result.data.IDAll
                            action15 = result.data.action15
                            action23 = result.data.action23
                            // 2. Получаем DT по IDAll


                            if (msg.isEmpty()) {
                                if (curIDAll == IDAll){
                                    val putResult = incontrolViewModel.back2Sklad("bottle"+num, curPrim)
                                    when (putResult) {
                                        is Result.Success -> {
                                            // putResult.data — это уже готовая строка (isOk) от API
                                            if (putResult.data != "") {
                                                showResponse(
                                                    putResult.data
                                                )  // Показываем её
                                            }
                                            incontrolViewModel.refreshListEvent.postValue(Unit)
                                        }
                                        is Result.Failure -> {
                                            showError(putResult.exception)
                                        }
                                    }
                                }
                                else{
                                    showPrimInputDialog { prim ->
                                        lifecycleScope.launch {
                                            val putResult = incontrolViewModel.back2Sklad("bottle"+num, prim)
                                            curIDAll = IDAll
                                            curPrim = prim
                                            when (putResult) {
                                                is Result.Success -> {
                                                    // putResult.data — это уже готовая строка (isOk) от API
                                                    if (putResult.data != "") {
                                                        showResponse(
                                                            putResult.data
                                                        )  // Показываем её
                                                    }
                                                    incontrolViewModel.refreshListEvent.postValue(Unit)
                                                }
                                                is Result.Failure -> {
                                                    showError(putResult.exception)
                                                }
                                            }
                                        }
                                    }
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
    private fun handle3N0Scan3(stringScanResult: String) {
        isbottle = false
        if (box == 0){
            Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
        }
        else {
            val parts = stringScanResult.split('$')
            if (parts.size > 1) {
                val num = parts[1]
                curNum = num


                // 2. Вызываем put2Box()

                lifecycleScope.launch {
                    val putResult = incontrolViewModel.put2Box(num, box)

                    when (putResult) {
                        is Result.Success<Unit> -> {
                            // Успех: запрашиваем обновление списка
                            incontrolViewModel.refreshListEvent.postValue(Unit)
                        }
                        is Result.Failure -> {
                            // Ошибка: показываем сообщение
                            showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                        }
                    }
                }



            }
        }
    }

    private fun handleZScan3(stringScanResult: String) {
        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            box = parts[1].toInt()
            toolbarlnk.title= "В коробку $box"
            incontrolViewModel.incontrolSearch(paramValue!!, "", box)
        }
    }

    private fun handleScanBottle3(stringScanResult: String) {
        isbottle = true
        if (box == 0){
            Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
        }
        val parts = stringScanResult.split('$')
        if (parts.size == 5) {
            // Номер катушки находится на 1‑й позиции (индекс 1)
            val num = parts[2]
            curNum = num


            // 2. Вызываем put2Box()

            lifecycleScope.launch {
                val putResult = incontrolViewModel.put2Box("bottle" + curNum, box)

                when (putResult) {
                    is Result.Success<Unit> -> {
                        // Успех: запрашиваем обновление списка
                        incontrolViewModel.refreshListEvent.postValue(Unit)
                    }
                    is Result.Failure -> {
                        // Ошибка: показываем сообщение
                        showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                    }
                }
            }
        }
    }
    private fun handle3N0Scan4(stringScanResult: String) {
        isbottle = false
        if (box == 0){
            Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
        }
        else {
            val parts = stringScanResult.split('$')
            if (parts.size > 1) {
                val num = parts[1]
                curNum = num
                lifecycleScope.launch {
                    val result = incontrolViewModel.getAllID(num)
                    when (result) {
                        is Result.Success -> {
                            val IDAllList = result.data
                            // Теперь можно работать с полученным списком
                            handleIDAllList(IDAllList)
                            incontrolViewModel.refreshListEvent.postValue(Unit)
                        }

                        is Result.Failure -> {
                            showError(result.exception)
                        }
                    }

                }

            }
        }
    }

    private fun handleZScan4(stringScanResult: String) {

        // Проверяем, есть ли элементы в адаптере
        if (adapterincontrol.itemCount > 0) {
            // Показываем диалог подтверждения
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Подтверждение")
                .setMessage("Взять другую коробку?")
                .setPositiveButton("Да") { _, _ ->
                    val parts = stringScanResult.split('$')
                    if (parts.size > 1) {
                        box = parts[1].toInt()
                        toolbarlnk.title= "Коробка $box"
                    }
                    incontrolViewModel.takebox(box)

                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            val parts = stringScanResult.split('$')
            if (parts.size > 1) {
                box = parts[1].toInt()
                toolbarlnk.title= "Коробка $box"
            }
            // Если список пуст — сразу вызываем takebox()
            incontrolViewModel.takebox(box)
        }
    }

    private fun handleScanBottle4(stringScanResult: String) {
        isbottle = true
        if (box == 0){
            Toast.makeText(requireContext(), "Сначала отсканируйте коробку", Toast.LENGTH_SHORT).show()
        }
        else {
            val parts = stringScanResult.split('$')
            if (parts.size == 5) {
                curNum = parts[2]
                lifecycleScope.launch {
                    val result = incontrolViewModel.getAllID(curNum!!)
                    when (result) {
                        is Result.Success -> {
                            val IDAllList = result.data
                            // Теперь можно работать с полученным списком
                            handleIDAllList(IDAllList)
                            //incontrolViewModel.refreshListEvent.postValue(Unit)
                        }

                        is Result.Failure -> {
                            showError(result.exception)
                        }
                    }

                }

            }

        }
    }
    private fun handleCScan4(stringScanResult: String) {
        // Отбрасываем первый символ 'C'
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
                val currentItem =  adapterincontrol.getItemByIdAll(IDAll.toInt())
                if (currentItem != null) {
                    val currentStel = currentItem.rack.toString()
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
                            val putResult : Result<Unit>
                            if (isbottle)
                            {
                                putResult = incontrolViewModel.put2WH("bottle"+curNum!!)
                            }
                            else
                            {
                                putResult = incontrolViewModel.put2WH(curNum!!)
                            }


                            when (putResult) {
                                is Result.Success<Unit> -> {
                                    // Успех: запрашиваем обновление списка
                                    incontrolViewModel.refreshListEvent.postValue(Unit)
                                }
                                is Result.Failure -> {
                                    // Ошибка: показываем сообщение
                                    showError(putResult.exception) // Или putResult.exception — см. примечание ниже
                                }
                            }

                        }
                        curNum = ""
                        incontrolViewModel.saveStelAndCell(stel,yach)
                    } else {
                        // Несовпадение → красный фон
                        infoTextView.visibility = View.VISIBLE
                        infoTextView.setBackgroundColor(Color.argb(255,255,0,0))
                        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
                        incontrolViewModel.clearStelAndCell()
                    }
                } else {
                    showResponse("Нет данных для сравнения (receiveFragmentAcceptSearchResponse пуст)")
                }
            } else {
                showResponse("QR-код после 'C' должен содержать 12 цифр, получено: ${content.length}")
            }
        }
    }





    inner class Adapterincontrol: BaseRecyclerAdapter<InControlSearchResponse>(InControlSearchResponse()) {

        private var selectedPosition: Int = -1 // -1 = ничего не выделено
        var onRemoveClick: ((item: InControlSearchResponse.Item, position: Int) -> Unit)? = null

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
        fun findPosition(idAll: String): Int? {
            val targetId = idAll.toInt()
            val index = data.found.indexOfFirst { it.IDAll == targetId }
            return if (index != -1) index else null
        }

        fun findDtByIdAll(idAll: String): String? {
            return data.found
                .firstOrNull { it.IDAll == idAll.toInt() }  // ищем первый элемент с совпадающим id
                ?.DT                                   // предполагаем, что у InControlSearchResponse.found.item есть поле dt
        }
        fun findByIdAll(idAll: String): InControlSearchResponse.Item? {
            return data.found
                .firstOrNull { it.IDAll == idAll.toInt() }  // ищем первый элемент с совпадающим id
                                     // предполагаем, что у InControlSearchResponse.found.item есть поле dt
        }
        @SuppressLint("NotifyDataSetChanged")
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


        fun getItemByIdAll(idAll: Int): InControlSearchResponse.Item? {
            return data.found.firstOrNull { it.IDAll == idAll }
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
        fun removeItem(position: Int) {
            data.found.removeAt(position)
            notifyItemRemoved(position)  // Только этот элемент удаляется
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
        @SuppressLint("SuspiciousIndentation", "SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemBinding =TemplateCardBinding.bind(holder.itemView)
            val itemData =
                data.found[position]
            itemBinding.containerVertical.removeAllViews()
            itemBinding.containerHorizon.removeAllViews()
            //region content
            arrayOf(
                Pair(arrayOf("name"),""),
                Pair(arrayOf("NumNakl"),"№ накладной "),
                Pair(arrayOf("id"),"# компонента "),
                Pair(arrayOf("nominal"),"Номинал "),
                Pair(arrayOf("horizontalDivider"),""),
                Pair(arrayOf("amount"),"На складе "),
                Pair(arrayOf("kolpacks"),"Упаковок "),
                //Pair(arrayOf("isolated"),"В изоляторе "),
            )
                .filter { pair ->
                    !pair.first.contains("DT") // Дополнительная фильтрация
                    !pair.first.contains("inBox") // Дополнительная фильтрация
                }
                .forEach {pair->
                    itemBinding.containerVertical.addView(
                        TemplatePresenterBinding.inflate(layoutInflater,itemBinding.containerVertical,false)
                            .apply {
                                setAttribute(pair,itemData)
//                                if (pair.first.contains("name") && itemData.inBox && paramValue == "toBox") {
//                                    root.setBackgroundColor(
//                                        ContextCompat.getColor(root.context, android.R.color.holo_green_light)
//                                    )
//                                }
                            }
                            .root
                    )
                }
            //endregion
            if (position == selectedPosition) {
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

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
            if (paramValue in listOf("toBox", "WHtoIncontrol")) {
                val deleteIcon = ImageView(holder.itemView.context).apply {
                    setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // или свой ресурс
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.END
                    }
                    setPadding(16, 16, 16, 16)
                    setOnClickListener {
                        onRemoveClick?.invoke(itemData, position)
                    }
                }
                itemBinding.containerHorizon.addView(deleteIcon)
            }
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

        var lastStoredStel: String = ""
        var lastStoredCell: String = ""

        fun saveStelAndCell(stel: String, cell: String) {
            lastStoredStel = stel
            lastStoredCell = cell
        }

        fun clearStelAndCell() {
            lastStoredStel = ""
            lastStoredCell = ""
        }

        fun incontrolSearch(param: String, last: String, box: Int) {
            ioCoroutineScope.launch {
                incontrolFragmentState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> InControlFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when (
                            val result = apiPantes.incontrolSearch(
                                token = token,
                                query = param,
                                box = box,
                                last = last,
                            )
                        ) {
                            is ApiPantes.ApiState.Success -> {
                                // Проверяем на null и пустоту
                                if (result.data.found.isNotEmpty()) {
                                    InControlFragmentState.Success(result.data)
                                } else {

                                    incontrolFragmentEmpty.postValue(View.VISIBLE)
                                    InControlFragmentState.Success(result.data)

                                }
                            }

                            is ApiPantes.ApiState.Error ->
                                InControlFragmentState.Error(result.exception)
                        }

                    }
                )
                _searchCompleted.postValue(true)
            }

        }




        suspend fun checkst(num: String): Result<Back2SkladState.CheckST> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolCheckst(token, num)) {
                    is ApiPantes.ApiState.Success -> {
                        if (result.data.toString().isNotEmpty()) {

                            val DT = result.data.isOk!!
                            val IDAll = result.data.IDAll!!
                            val action15 = result.data.action15!!
                            val action23 = result.data.action23!!

                            DT.let {
                                Back2SkladState.CheckST(
                                    IDAll,
                                    it, action15, action23
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
                }
            }
        suspend fun getAllID(num: String): Result<ArrayList<Int>>  =
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
        suspend fun back2Sklad(num: String, prim: String): Result<String> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)


                when (val result = apiPantes.incontrolBack2sklad(
                    token = token,
                    num = num,
                    prim = prim,
                )) {
                    is ApiPantes.ApiState.Success -> {
                        // Предполагаем, что result.data содержит поле isOk
                        Result.Success(result.data) // Возвращаем isOk
                    }
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }

        suspend fun put2Box(num: String, box: Int): Result<Unit> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolPut2box(token, num, box)) {
                    is ApiPantes.ApiState.Success -> Result.Success(Unit) // Возвращаем Unit
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }

        suspend fun WHtoBox(num: String, box: Int): Result<Unit> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolWHtoBox(token, num, box)) {
                    is ApiPantes.ApiState.Success -> Result.Success(Unit) // Возвращаем Unit
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }
        fun takebox(box: Int) {

            ioCoroutineScope.launch {

                when (val token = loginRepository.user?.token) {
                    null -> InControlFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                    else -> {
                        when (val result = apiPantes.incontrolTakebox(
                            token = token,
                            box = box,
                        )) {
                            is ApiPantes.ApiState.Success -> {
                                if (result.data.isNotEmpty()) {
                                    back2SkladState.postValue(Back2SkladState.TakeBox(result.data))
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
        fun takeboxfromWH(box: Int) {

            ioCoroutineScope.launch {

                when (val token = loginRepository.user?.token) {
                    null -> InControlFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                    else -> {
                        when (val result = apiPantes.incontrolTakeboxFromWH(
                            token = token,
                            box = box,
                        )) {
                            is ApiPantes.ApiState.Success -> {
                                if (result.data.isNotEmpty()) {
                                    back2SkladState.postValue(Back2SkladState.TakeBox(result.data))
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
        suspend fun put2WH(num: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)

                when (val result = apiPantes.incontrolPut2WH( num,token)) {
                    is ApiPantes.ApiState.Success -> Result.Success(Unit) // Возвращаем Unit
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }
        suspend fun incontrolRemoveFromBox(IDAll: Int, box: Int): Result<Unit> =
            withContext(Dispatchers.IO) {
                val token = loginRepository.user?.token
                    ?: return@withContext Result.Failure(ErrorsFragment.nonFatalExceptionShowToasteToken)


                when (val result = apiPantes.incontrolRemoveFromBox(token, IDAll, box)) {
                    is ApiPantes.ApiState.Success -> Result.Success(Unit)
                    is ApiPantes.ApiState.Error -> Result.Failure(result.exception)
                }
            }
//        fun put2WH(num: String,paramValue: String?,box: Int) {
//
//            ioCoroutineScope.launch {
//
//                when (val token = loginRepository.user?.token) {
//                    null -> InControlFragmentState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
//                    else -> {
//                        when (val result = apiPantes.incontrolPut2WH(
//                            num = num,
//                            token = token,
//                        )) {
//                            is ApiPantes.ApiState.Success -> {
//                                if (result.data.isNotEmpty()) {
//                                    back2SkladState.postValue(Back2SkladState.Put2WH(result.data))
//                                }
//                                refreshListEvent.postValue(Unit)
//
//
//                            }
//
//                            is ApiPantes.ApiState.Error -> {
//                                back2SkladState.postValue(Back2SkladState.Error(result.exception))
//                            }
//                        }
//                    }
//                }
//
//            }
//        }

        fun resetSearchCompleted() {
            _searchCompleted.value = false
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
        private val _searchCompleted = MutableLiveData<Boolean>()
        val searchCompleted: LiveData<Boolean> = _searchCompleted
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
    //   val Result<*>.isSuccess: Boolean get() = this is Result.Success
    //  val Result<*>.isFailure: Boolean get() = this is Result.Failure

//    inline fun <R : Any> Result<R>.onSuccess(action: (R) -> Unit): Result<R> {
//        if (this is Result.Success) action(data)
//        return this
//    }

//    inline fun Result<*>.onFailure(action: (Exception) -> Unit): Result<*> {
//        if (this is Result.Failure) action(exception)
//        return this
//    }

//    fun action(exception: Throwable) {
//        TODO("Not yet implemented")
//    }
}