package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.R
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch
import com.example.scanner.models.IsolatorListInfoResponse
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.navigation.InControlFragment.Result

import com.google.android.material.dialog.MaterialAlertDialogBuilder


class IsolatorListFragmentInfo : BaseFragment() {

    companion object {
        const val PARAM = "param"
    }

    private val isolatorListIViewModel: IsolatorListInfoViewModel by viewModels { viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private var isbottle: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)

    }
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Устанавливаем layout
        val view = inflater.inflate(R.layout.isolator_add, container, false)

        // Находим view по ID
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        val containerList: LinearLayout= view.findViewById(R.id.containerList)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()  // или activity.onBackPressed()
        }
        // Настройка toolbar
        val skladid: TextView = view.findViewById(R.id.skladid)
        val naim: TextView = view.findViewById(R.id.naim)
        val el: TextView = view.findViewById(R.id.el)
        val ser: TextView = view.findViewById(R.id.ser)
        val nom: TextView = view.findViewById(R.id.nom)
        val krp: TextView = view.findViewById(R.id.krp)


        val addBtn : Button = view.findViewById(R.id.buttonIso);
        val clearBtn : Button = view.findViewById(R.id.buttonClear);
        toolbar.apply {
            title = "Изолируем вручную"
        }
        // Наблюдаем за состоянием ViewModel

        addBtn.setOnClickListener {
            showIsolationDialog()
        }

        clearBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Подтверждение")
                .setMessage("Вы уверены, что хотите очистить данные?")
                .setPositiveButton("Да") { _, _ ->
                    isolatorListIViewModel.clearData()
                }
                .setNegativeButton("Нет", null)
                .show()
        }

        isolatorListIViewModel.isolatorListInfoFormState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is IsolatorListInfoFormState.SuccessIsolatorListInfo -> {
                    val data = state.data as IsolatorListInfoResponse
                    // toolbar.title = "Данные катушек"  // Можно взять из ответа, если есть поле

                    // Очищаем контейнер
                    containerList.removeAllViews()

                        skladid.text = data.head.skladid ?: ""
                        naim.text = data.head.naim ?: ""
                        el.text = data.head.el ?: ""
                        ser.text = data.head.ser ?: ""
                        nom.text = data.head.nom ?: ""
                        krp.text = data.head.krp ?: ""

                    // Заполняем список
                    data.coils.forEach { attribute ->
                        // Инфлейтим элемент
                        val itemView = inflater.inflate(R.layout.item_isolator_list_info, containerList, false)

                        // Находим TextView
                        val tvNumber: TextView = itemView.findViewById(R.id.number)
                        val tvnumNakl: TextView = itemView.findViewById(R.id.numNakl)
                        val tvNaklDT: TextView = itemView.findViewById(R.id.NaklDT)

                        tvNumber.text = attribute.number.toString() ?: ""
                        tvnumNakl.text = attribute.numNakl ?: "-"
                        tvNaklDT.text = attribute.NaklDT ?: "-"
                        // Можно добавить разделитель
//                        val separator = View(requireContext()).apply {
//                            layoutParams = ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                1
//                            )
//                            setBackgroundColor(Color.LTGRAY)
//                        }
//                        containerList.addView(separator)

                        // Добавляем элемент
                        containerList.addView(itemView)
                    }
                }
                is IsolatorListInfoFormState.Error -> {
                    isolatorListIViewModel.mainActivityRouter.navigate(
                        ErrorsFragment::class.java,
                        Bundle().apply {
                            putSerializable(
                                ErrorsFragment.PARAM,
                                state.exception
                            )
                        }
                    )
                }

                is IsolatorListInfoFormState.SuccessAdd -> {
                    if (state.message == "skladid") {
                                Toast.makeText(
                                    context,
                                    "Другой элемент необходимо изолировать отдельно",
                                    Toast.LENGTH_LONG
                                ).show();

                    }
                    else {
                        Toast.makeText(
                            context,
                            "Упаковка добавлена",
                            Toast.LENGTH_LONG
                        ).show();
                        isolatorListIViewModel.isolatorListSearch()
                    }
                }
                is IsolatorListInfoFormState.SuccessClear -> {
                    if (state.message == "ok") {
                        isolatorListIViewModel.isolatorListSearch()
                    }
                }
                is IsolatorListInfoFormState.SuccessIso -> {
                    if (state.message == "ok") {
                        isolatorListIViewModel.isolatorListSearch()
                    }
                }
            }
        }

        // Запрашиваем данные
        isolatorListIViewModel.isolatorListSearch()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){


                    when(val stateScan=it){
                        is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult->{
                            stateScan.stringScanResult?.let { stringScanResult ->
                                when {
                                    stringScanResult.startsWith("3N0") -> handle3N0Scan(stringScanResult)
//                                    stringScanResult.startsWith('C') -> handleCScan(stringScanResult)
                                    (stringScanResult.split('$')).size == 5 -> handleScanBottle(stringScanResult)
                                    else -> showErrorMessage("Неподдерживаемый формат QR-кода")
                                }
                            }
                        }
                        else->{}
                    }



        }
    }
    private fun showIsolationDialog() {
        // Шаг 1: выбор причины
        val reasons = mapOf(
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
        val reasonNames = reasons.values.toTypedArray()
        var selectedReasonCode: Int? = null

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Причина изоляции")
            .setSingleChoiceItems(reasonNames, -1) { dialog, which ->
                selectedReasonCode = reasons.keys.elementAt(which)
            }
            .setPositiveButton("Далее") { dialog, _ ->
                if (selectedReasonCode == null) {
                    Toast.makeText(requireContext(), "Выберите причину", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                dialog.dismiss()
                // Шаг 2: выбор даты
                showDatePicker(selectedReasonCode!!)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDatePicker(reasonCode: Int) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                // Шаг 3: запрос комментария
                showCommentInput(reasonCode, formattedDate)
            },
            year, month, day
        ).show()
    }

    private fun showCommentInput(reasonCode: Int, dt: String) {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Введите комментарий"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 3
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Комментарий")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val comment = input.text.toString().trim()
                if (comment.isEmpty()) {
                    Toast.makeText(requireContext(), "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // Вызов ViewModel
                isolatorListIViewModel.isoData(reasonCode, dt, comment)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun handle3N0Scan(stringScanResult: String) {
        isbottle = false
        val parts = stringScanResult.split('$')
        if (parts.size > 1) {
            val num = parts[1]   // предполагаем, что номер находится во второй части
            if (num.isNotBlank()) {
                isolatorListIViewModel.addScannedItem(num, isbottle = false)

            } else {
                showErrorMessage("Не удалось извлечь номер из QR-кода")
            }
        } else {
            showErrorMessage("Неверный формат QR-кода 3N0")
        }
    }



    private fun handleScanBottle(stringScanResult: String) {
        isbottle = true

            val parts = stringScanResult.split('$')
            if (parts.size == 5) {

                val num = parts[1]   // предполагаем, что номер находится во второй части
                if (num.isNotBlank()) {
                    isolatorListIViewModel.addScannedItem(num, isbottle = true)

                } else {
                    showErrorMessage("Не удалось извлечь номер из QR-кода банки")
                }
            } else {
                showErrorMessage("Неверный формат QR-кода банки (ожидается 5 частей)")
            }


    }


    sealed class IsolatorListInfoFormState {
        data class SuccessIsolatorListInfo(val data: IsolatorListInfoResponse) : IsolatorListInfoFormState()
        data class SuccessClear(val message: String) : IsolatorListInfoFormState()
        data class SuccessAdd(val message: String) : IsolatorListInfoFormState()
        data class SuccessIso(val message: String) : IsolatorListInfoFormState()
        data class Error(val exception: Throwable) : IsolatorListInfoFormState()
    }


    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    class IsolatorListInfoViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository) :
        BaseViewModel() {



        /**
         * Добавить отсканированный элемент в список.
         * @param num номер (из QR-кода)
         * @param isbottle true – бутылка, false – катушка 3N0
         */
        fun addScannedItem(num: String, isbottle: Boolean) {

            isolatorAddNum(num,isbottle)
        }

        /**
         * Получить все накопленные элементы и очистить список.
         * @return список всех добавленных элементов
         */
        fun isoData(reason: Int, dt: String, prim: String) {
            ioCoroutineScope.launch {
                isolatorListInfoFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> IsolatorListInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when (val result = apiPantes.isolatorIsolate(token, reason, dt, prim)) {
                            is ApiPantes.ApiState.Success -> IsolatorListInfoFormState.SuccessIso(result.data)
                            is ApiPantes.ApiState.Error -> IsolatorListInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }
        fun clearData() {
            ioCoroutineScope.launch {
                isolatorListInfoFormState.postValue(
                    when (val token = loginRepository.user?.token) {
                        null -> IsolatorListInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when (val result = apiPantes.isolatorClear(token)) {
                            is ApiPantes.ApiState.Success->
                                IsolatorListInfoFormState.SuccessClear(result.data)
                            is ApiPantes.ApiState.Error->
                                IsolatorListInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }
        private fun isolatorAddNum(num: String, isbottle:Boolean) {
            ioCoroutineScope.launch {
                isolatorListInfoFormState.postValue(
                    when(val token=loginRepository.user?.token) {
                        null -> IsolatorListInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when(val result=apiPantes.isolatorAddNum(token,num,isbottle)){
                            is ApiPantes.ApiState.Success->
                                IsolatorListInfoFormState.SuccessAdd(result.data)
                            is ApiPantes.ApiState.Error->
                                IsolatorListInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }
        fun isolatorListSearch() {
            ioCoroutineScope.launch {
                isolatorListInfoFormState.postValue(
                    when(val token=loginRepository.user?.token) {
                        null -> IsolatorListInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when(val result=apiPantes.isolatorListInfo(token)){
                            is ApiPantes.ApiState.Success->
                                IsolatorListInfoFormState.SuccessIsolatorListInfo(result.data)
                            is ApiPantes.ApiState.Error->
                                IsolatorListInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }

        companion object {
            fun getInstance(context: Context): IsolatorListInfoViewModel {
                return IsolatorListInfoViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context)
                )
            }
        }

        val isolatorListInfoFormState=
            MutableLiveData<IsolatorListInfoFormState>()

    }

}


