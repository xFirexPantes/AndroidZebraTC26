package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.scanner.R
import com.example.scanner.app.batch2
import com.example.scanner.app.templateInputTextContainer
import com.example.scanner.app.templateInputTextMyTextInput
import com.example.scanner.app.templateInputTextTextLayout
import com.example.scanner.app.onRightDrawableClicked
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.databinding.TemplateRecyclerBinding
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateResultEmptyBinding
import com.example.scanner.databinding.TemplateScannerReadyBinding
import com.example.scanner.models.AcceptScanResponse
import com.example.scanner.models.AcceptSearchResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Pref
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseRecyclerAdapter
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.NonFatalExceptionShowToaste
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.base.NonFatalExceptionShowDialogMessage
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.example.scanner.ui.navigation_over.TransparentFragment
import kotlinx.coroutines.launch
import timber.log.Timber

class ReceiveFragment : BaseFragment() {

    companion object{
        const val PARAM_STEP_1_VALUE="param"
        const val PARAM_STEP_2_VALUE="param2"
        const val EXTRA_RGM = "rgm"
    }
    private var rgmValue: String = ""
    private var pendingScrollAttempts = 0
    private var isCoilsContainerReady = false
    private var pendingCoilScroll: String? = null
    private var horizontalScrollViewCoils: HorizontalScrollView? = null
    private var coilsContainer: LinearLayout? = null
    private val receiveViewModel: ReceiveViewModel by viewModels{ viewModelFactory}
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    private val adapterReceive=AdapterReceive()
    private lateinit var layoutManager:LinearLayoutManager
    private var layoutManagerOnSaveInstanceStateParcelable:Parcelable?=null
    private lateinit var step1: EditText
    private var stelFromQR: String = ""
    private var yachFromQR: String = ""
    private var Nkat: String = ""
    private var lastStel = ""
    private var lastCell = ""
    private var isBottle: Boolean = false
    private var lastQR: String = ""
    private lateinit var infoTextView :TextView
    var currentStel: String = ""
    var currentYach : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        scanViewModelReference=scanViewModel
        super.onCreate(savedInstanceState)
        rgmValue = arguments?.getString(EXTRA_RGM) ?: "first"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        layoutManager=object : LinearLayoutManager(requireContext()) {
            override fun onScrollStateChanged(state: Int) {
                super.onScrollStateChanged(state)
                if (findLastVisibleItemPosition()+1 == adapterReceive.itemCount) {
                    receiveViewModel.step2AcceptScan(
                        getArgument(PARAM_STEP_1_VALUE),
                        adapterReceive.last.toString())
                }
            }
        }



        // Добавляем в infoContainer (из разметки)

        return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply {

                toolbar.apply {

                    setNavigationOnClickListener {
                        when{
                            !getArgument<String?>(PARAM_STEP_2_VALUE).isNullOrEmpty()->{
                                arguments?.putSerializable(PARAM_STEP_2_VALUE,null)
                                receiveViewModel.receiveFragmentFormState.postValue(
                                    ReceiveFragmentFormState.ResetScan
                                )
                            }
                            !getArgument<String?>(PARAM_STEP_1_VALUE).isNullOrEmpty()->{
                                arguments?.putSerializable(PARAM_STEP_1_VALUE,null)
                                viewLifecycleOwner.batch2(
                                    receiveViewModel.receiveFragmentFormState,
                                    ArrayList<ReceiveFragmentFormState<*>>()
                                        .apply {
                                            add(ReceiveFragmentFormState.ResetSearch)
                                            add(ReceiveFragmentFormState.ResetScan)
                                        }
                                )

                            }
                            else->findNavController().navigateUp()
                        }
                    }

                    receiveViewModel.receiveFragmentTitle
                        .observe(viewLifecycleOwner){
                            title=it
                        }
                    receiveViewModel.receiveFragmentSubtitle
                        .observe(viewLifecycleOwner){
                            subtitle=it
                        }

                    //region iconManual
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                receiveViewModel.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner){
                                    root.visibility=it
                                }
                                src= ResourcesCompat.getDrawable(resources,R.drawable.ic_search,null)
                                image.setOnClickListener {
                                    scanViewModel.scannerApiEmulator.softScan(childFragmentManager,requireContext())
                                }

                            }
                            .root
                    )
                    //endregion
                    //region iconScan
                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,toolbar,false)
                            .apply {
                                receiveViewModel.pref.scannerIconDrawableId.observe(viewLifecycleOwner){
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
                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply {
                            var currentComponentId = ""
                            receiveViewModel.receiveFragmentAcceptSearchResponse.observe(viewLifecycleOwner) {
                                containerVertical.removeAllViews()

                                when (it) {
                                    null -> {
                                        containerVertical.visibility = View.GONE
                                    }
                                    else -> {
                                        containerVertical.visibility = View.VISIBLE

                                        // 1. Добавляем колонки таблицы (как было)
                                        arrayOf(
                                            Pair(arrayOf("name"), "Наим. "),
                                            Pair(arrayOf("id"), "#компонента "),
                                            Pair(arrayOf("batch"), "Серия "),
                                            Pair(arrayOf("case"), "Корпус "),
                                            Pair(arrayOf("element"), "Элемент "),
                                            Pair(arrayOf("nominal"), "Номинал "),
                                            Pair(arrayOf("stel"), "Ст. "),
                                            Pair(arrayOf("cell"), "Яч. "),
                                            Pair(arrayOf("coil"), "Кат. ")
                                        ).forEach { pair ->
                                            containerVertical.addView(
                                                TemplatePresenterBinding.inflate(inflater, containerVertical, false)
                                                    .apply { setAttribute(pair, it) }

                                                    .root
                                            )
                                        }
                                        currentComponentId = it.id
                                        // 2. Контейнер для заголовков (БЕЗ HorizontalScrollView!)
                                        val headerContainer = LinearLayout(context).apply {
                                            orientation = LinearLayout.HORIZONTAL
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,  // ВАЖНО: вся ширина экрана
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                            setPadding(0, 16, 0, 0)  // отступы сверху/снизу
                                        }

                                        // 3. Создаём заголовки с весами
                                        listOf(
                                            "ВК" to R.color.red_half,
                                            "Сушка" to R.color.yellow_highlight,
                                            "Принято" to R.color.green_border
                                        ).forEachIndexed { index, (text, colorRes) ->
                                            val tv = TextView(context).apply {
                                                setText(text)
                                                setTextColor(if (index == 0 || index == 2) Color.WHITE else Color.BLACK)
                                                setBackgroundColor(ContextCompat.getColor(context, colorRes))
                                                textSize = 14f
                                                gravity = Gravity.CENTER_HORIZONTAL
                                            }
                                            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                                weight = 1f  // равномерное распределение
                                            }
                                            tv.layoutParams = lp
                                            headerContainer.addView(tv)
                                        }

                                        // 4. Добавляем headerContainer в containerVertical (без HorizontalScrollView!)
                                        containerVertical.addView(headerContainer)

                                        // 5. Горизонтальный скролл для катушек (если есть данные)
                                        if (it.coils.isNotEmpty()) {
                                            horizontalScrollViewCoils = HorizontalScrollView(context).apply {
                                                layoutParams = LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                )
                                                setPadding(0, 16, 0, 0)
                                            }

                                            coilsContainer = LinearLayout(context).apply {
                                                orientation = LinearLayout.HORIZONTAL
                                                layoutParams = LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                )
                                            }
                                                         it.coils.forEach { coil ->
                                                val coilView = LinearLayout(context).apply {
                                                    orientation = LinearLayout.VERTICAL
                                                    setPadding(8, 4, 8, 4)
                                                }

                                                val tvType = TextView(context).apply {
                                                    text = coil.type
                                                    setTextColor(Color.BLACK)
                                                    textSize = 14f
                                                    gravity = Gravity.CENTER_HORIZONTAL
                                                }

                                                val tvNum = TextView(context).apply {
                                                    text = "№${coil.num}"
                                                    setTextColor(Color.GRAY)
                                                    textSize = 12f
                                                    gravity = Gravity.CENTER_HORIZONTAL
                                                }

                                                // Логика подсветки
                                                if (coil.isScanned) {
                                                    coilView.setBackgroundColor(
                                                        ContextCompat.getColor(coilView.context, R.color.yellow_highlight)
                                                    )
                                                } else {
                                                    coilView.background = null
                                                    tvNum.setTextColor(Color.GRAY)
                                                }

                                                coilView.addView(tvType)
                                                coilView.addView(tvNum)
                                                coilsContainer!!.addView(coilView)

                                                when (coil.st) {
                                                    1 -> {
                                                        coilView.setBackgroundColor(
                                                            ContextCompat.getColor(coilView.context, R.color.red_half)
                                                        )
                                                        tvNum.setTextColor(Color.WHITE)
                                                        tvType.setTextColor(Color.WHITE)
                                                    }
                                                    2 -> {
                                                        coilView.setBackgroundColor(
                                                            ContextCompat.getColor(coilView.context, R.color.yellow_highlight)
                                                        )
                                                        tvNum.setTextColor(Color.BLACK)
                                                        tvType.setTextColor(Color.BLACK)
                                                    }
                                                    3 -> {
                                                        coilView.setBackgroundColor(
                                                            ContextCompat.getColor(coilView.context, R.color.green_border)
                                                        )
                                                        tvNum.setTextColor(Color.WHITE)
                                                        tvType.setTextColor(Color.WHITE)
                                                    }
                                                    else -> {
                                                        coilView.background = null
                                                        tvNum.setTextColor(Color.BLACK)
                                                        tvType.setTextColor(Color.BLACK)
                                                    }
                                                }
                                            }

                                            horizontalScrollViewCoils!!.addView(coilsContainer)
                                            // 6. Добавляем скролл с катушками ПОСЛЕ заголовков
                                            containerVertical.addView(horizontalScrollViewCoils)
                                        }

                                    }
                                }
                                isCoilsContainerReady = true
                                //tryExecutePendingScroll()
                            }

                            containerVertical.setOnClickListener {
                                receiveViewModel.mainActivityRouter.navigate(
                                    ReceiveFragmentInfo::class.java,
                                    Bundle().apply {
                                        putSerializable(ReceiveFragmentInfo.PARAM, currentComponentId)
                                    }
                                )

                            }
                        }
                        .root
                )

                //region recycler
                root.addView(
                    TemplateRecyclerBinding.inflate(inflater,root,false)
                        .apply {
                            recycler.adapter=adapterReceive
                            recycler.layoutManager=layoutManager

                            //region empty
                            containerContent.addView(
                                TemplateResultEmptyBinding.inflate(inflater,containerContent,false)
                                    .root
                                    .apply {
                                        receiveViewModel.receiveFragmentVisibleEmpty
                                            .observe(viewLifecycleOwner){
                                                this.visibility=it
                                            }
                                        receiveViewModel.receiveFragmentVisibleEmpty
                                            .postValue(
                                                View.GONE
                                            )
                                    }
                            )
                            //endregion

                            //region input Component
                            containerContent.addView(
                                TemplateCardBinding.inflate(inflater,containerContent,false)
                                    .apply {
                                        receiveViewModel.receiveFragmentVisibleStep1
                                            .observe(viewLifecycleOwner){
                                                root.visibility= it
                                            }
                                        //region inputText
                                        containerVertical.addView(
                                            TemplatePresenterBinding.inflate(inflater,containerContent,false)
                                                .apply {
                                                    templateInputTextContainer.visibility= View.VISIBLE
                                                    templateInputTextTextLayout.hint="Введите код товара"
                                                    step1=templateInputTextMyTextInput
                                                    templateInputTextMyTextInput.onRightDrawableClicked {templateInputTextMyTextInput.text=null}
                                                    templateInputTextMyTextInput.setOnEditorActionListener { _, _, _->
                                                        scanViewModel.scanFragmentBaseFormState.postValue(
                                                            ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult(templateInputTextMyTextInput.text?.toString(),false)
                                                        )
                                                        true}
                                                }
                                                .root
                                        )
                                        //endregion
                                        //region promo
                                        containerHorizon.addView(
                                            TemplateScannerReadyBinding.inflate(inflater,containerHorizon,false)
                                                .apply {
                                                    icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_qr,null)
                                                    title="Сканируйте код элемента"
                                                }
                                                .root
//                                            TemplateButton4Binding.inflate(inflater,containerHorizon,false)
//                                                .apply {
//                                                    buttonScan.setOnClickListener {
//                                                        scanViewModel.scannerApi.softScan(childFragmentManager,requireContext())
//                                                    }
//                                                }
//                                                .root
                                        )
                                        //endregion
                                        //region tune card
                                        containerHorizon.gravity= Gravity.CENTER
                                        root.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        cardView.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        containerHorizon.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        (containerHorizon.parent as ViewGroup).layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        //endregion
                                    }
                                    .root
                            )
                            //endregion

                            //region scan
                            containerContent.addView(
                                TemplateCardBinding.inflate(layoutInflater,containerContent,false)
                                    .apply {
                                        receiveViewModel.receiveFragmentVisibleStep2
                                            .observe(viewLifecycleOwner){
                                                root.visibility=it
                                            }
                                        //region promo
                                        containerHorizon.addView(
                                            TemplateScannerReadyBinding.inflate(inflater,containerHorizon,false)
                                                .apply {
                                                    icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_qr,null)
                                                    title="Сканируйте штрих-код производителя"
                                                }
                                                .root

//                                            TemplateButton4Binding.inflate(inflater,containerHorizon,false)
//                                                .apply {
//                                                    buttonScan.setOnClickListener {
//                                                        scanViewModel.scannerApi.softScan(childFragmentManager,requireContext())
//                                                    }
//                                                }
//                                                .root
                                        )
                                        //endregion
                                        //region tune card
                                        containerHorizon.gravity= Gravity.CENTER
                                        root.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        cardView.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        containerHorizon.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT

                                        (containerHorizon.parent as ViewGroup).layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                        //endregion

                                    }
                                    .root

//                                TemplateScannerReadyBinding.inflate(inflater,containerContent,false)
//                                    .apply {
//                                        titleSpannable=SpannableString("Шаг\n2\n(штрих-код)")
//                                            .apply {
//                                                setSpan(RelativeSizeSpan(6f), 4,5, 0)
//                                            }
//
//                                        receiveViewModel.receiveFragmentVisibleStep2
//                                            .observe(viewLifecycleOwner){
//                                                root.visibility=it
//                                            }
//                                    }
//                                    .root
                            )
                            //endregion

                        }
                        .root
                )
                //endregion

            }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiveViewModel.receiveFragmentFormState.value=
            ReceiveFragmentFormState.SetupForm

        receiveViewModel.receiveFragmentFormState.observe(viewLifecycleOwner){

            when(val state=it){
                is ReceiveFragmentFormState.SetupForm->{

                    viewLifecycleOwner.batch2(
                        receiveViewModel.receiveFragmentFormState,
                        ArrayList<ReceiveFragmentFormState<*>>()
                            .apply {

                                if (receiveViewModel.receiveFragmentAcceptSearchResponse.value==null){
                                    add(ReceiveFragmentFormState.ResetSearch)
                                    if (!getArgument<String?>(PARAM_STEP_1_VALUE).isNullOrBlank()){
                                        add(ReceiveFragmentFormState.RequestSearch)
                                    }
                                }

                                if (adapterReceive.isResetContent){
                                    add(ReceiveFragmentFormState.ResetScan)
                                    if (!getArgument<String?>(PARAM_STEP_2_VALUE).isNullOrBlank()) {
                                        add(ReceiveFragmentFormState.RequestScan)
                                    }
                                }


                            }
                    )

                }
                is ReceiveFragmentFormState.ResetSearch-> {
                    receiveViewModel.receiveFragmentAcceptSearchResponse
                        .postValue(null)
                    receiveViewModel.receiveFragmentTitle
                        .postValue(getString(R.string.button_receive))
                }
                is ReceiveFragmentFormState.ResetScan-> {
                    adapterReceive.setContent(AcceptScanResponse())
                    adapterReceive.resetContent()
                }
                is ReceiveFragmentFormState.PutKatSuccess -> {
                    // PUT выполнен, теперь обновляем поиск
                    receiveViewModel.step1AcceptSearch(lastQR, Nkat)
                }
                is ReceiveFragmentFormState.Error ->{
                        if(state.exception is NonFatalExceptionShowToaste){
                            AlertDialog.Builder(requireContext())
                                .setTitle("Ошибка")
                                .setMessage(
                                    StringBuilder()
                                        .append(state.exception.message)
                                )
                                .setPositiveButton(android.R.string.cancel){_,_->}
                                .show()
                            receiveViewModel.mainActivityRouter
                                .navigate(TransparentFragment::class.java)
                        }
                        else{
                            receiveViewModel.mainActivityRouter.navigate(
                                ErrorsFragment::class.java,
                                Bundle().apply {
                                    putSerializable(
                                        ErrorsFragment.PARAM,
                                        state.exception
                                    )
                                })
                        }

                        viewLifecycleOwner.batch2(
                            receiveViewModel.receiveFragmentFormState,
                            ArrayList<ReceiveFragmentFormState<*>>()
                                .apply {
                                    add(ReceiveFragmentFormState.ResetScan)
                                    add(ReceiveFragmentFormState.ResetSearch)
                                }
                        )
                }
                is ReceiveFragmentFormState.SuccessScan ->{

                    state.data as AcceptScanResponse
                    if (adapterReceive.isResetContent){
                        receiveViewModel.receiveFragmentTitle
                            .postValue(
                                getString(R.string.format_title,"${state.data.total}")
                            )

                    }
                    adapterReceive.appendContent(state.data)
                    layoutManagerOnSaveInstanceStateParcelable?.let {
                        layoutManager.onRestoreInstanceState(it)
                        layoutManagerOnSaveInstanceStateParcelable=null
                    }

                }
                is ReceiveFragmentFormState.SuccessSearch ->{
                    val response = state.data
                    val scannedNkat = Nkat // сохраняем, т.к. Nkat может измениться при следующем сканировании

                    Timber.d("SuccessSearch: stel=${response.stel}, cell=${response.cell}, lastStel=$lastStel, lastCell=$lastCell")
                    receiveViewModel.receiveFragmentAcceptSearchResponse.value = response
                    state.scrollToCoil?.let { Nkat ->
                        requestScrollToCoil(Nkat)
                    }

                    // Если это не первое сканирование (lastStel не пуст)
                    if (lastStel.isNotEmpty() && lastCell.isNotEmpty() &&
                        response.stel == lastStel && response.cell == lastCell) {

                        if (!receiveViewModel.skipNextPut) {
                            // Это тот же компонент - выполняем put
                            if (isBottle) {
                                receiveViewModel.putBottle2Sklad(
                                    response.stel,
                                    response.cell,
                                    Nkat,
                                    true,  // isOk = true,
                                    rgmValue
                                )
                            }
                            else {
                                receiveViewModel.putKat2Sklad(
                                    stel = response.stel,
                                    shelf = response.cell,
                                    curKat = scannedNkat,
                                    isOk = true,
                                    coil = false,
                                    rgmValue
                                )
                            }
                            updateInfoTextView(isMatch = true)
                            // Устанавливаем флаг, чтобы следующий SuccessSearch (после обновления) не вызвал put снова
                            receiveViewModel.setSkipNextPut(true)
                        } else {
                            // Пропускаем put, так как он только что был выполнен
                            receiveViewModel.setSkipNextPut(false) // сбрасываем для будущих
                            // Просто обновляем UI
                        }
                    } else {
                        // Новый элемент или первое сканирование
                        if (lastStel!="") {
                            updateInfoTextView(isMatch = false)
                        }// или true, если первое?
                        // Сбрасываем флаг на всякий случай
                        receiveViewModel.setSkipNextPut(false)
                    }


                    // В любом случае обновляем последние значения

                    // Обновляем UI с новыми данными
                    receiveViewModel.receiveFragmentAcceptSearchResponse.value = response
                    state.scrollToCoil?.let { Nkat ->
                        requestScrollToCoil(Nkat)
                    }
                }
                is ReceiveFragmentFormState.RequestSearch->{
                        receiveViewModel.step1AcceptSearch(
                            getArgument(PARAM_STEP_1_VALUE)
                        )
                }
                is ReceiveFragmentFormState.RequestScan->{
                    adapterReceive.setContent(AcceptScanResponse())
                    adapterReceive.resetContent()
                    receiveViewModel.step2AcceptScan(getArgument(PARAM_STEP_2_VALUE),"")
                }
                is ReceiveFragmentFormState.NoOp -> {
                    // Ничего не делаем — экран не обновляется
                    // Можно оставить пустым или добавить комментарий
                }

                ReceiveFragmentFormState.RequestSearchBottle -> {
                    receiveViewModel.step3AcceptSearch(
                        getArgument(PARAM_STEP_1_VALUE)
                    )
                }

                ReceiveFragmentFormState.PutKatError -> TODO()
            }

            when{
                receiveViewModel.receiveFragmentAcceptSearchResponse.value==null
                        && adapterReceive.isResetContent
                            ->{
                                receiveViewModel.receiveFragmentVisibleStep1
                                    .postValue(View.VISIBLE)
                                receiveViewModel.receiveFragmentVisibleStep2
                                    .postValue(View.GONE)
                                receiveViewModel.receiveFragmentVisibleEmpty
                                    .postValue(View.GONE)

                            }
                adapterReceive.isResetContent
                        && receiveViewModel.receiveFragmentAcceptSearchResponse.value!=null
                            ->{
                                receiveViewModel.receiveFragmentVisibleStep2
                                    .postValue(View.VISIBLE)
                                receiveViewModel.receiveFragmentVisibleStep1
                                    .postValue(View.VISIBLE)
                                receiveViewModel.receiveFragmentVisibleEmpty
                                    .postValue(View.GONE)
                            }
                !adapterReceive.isResetContent
                        && receiveViewModel.receiveFragmentAcceptSearchResponse.value!=null->{

                            receiveViewModel.receiveFragmentVisibleStep1
                                .postValue(View.GONE)
                            receiveViewModel.receiveFragmentVisibleStep2
                                .postValue(View.GONE)
                            receiveViewModel.receiveFragmentVisibleEmpty
                                .postValue(
                                    if (adapterReceive.itemCount==0)
                                        View.VISIBLE
                                    else
                                        View.GONE
                                )

                        }

            }
        }

        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner){
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

    }
    private fun requestScrollToCoil(coilNumber: String) {
        pendingCoilScroll = coilNumber
        // Если данные уже есть — пробуем прокрутить сразу
        if (receiveViewModel.receiveFragmentAcceptSearchResponse.value != null) {
            tryExecutePendingScroll()
        }
    }

    // Вызывается при обновлении данных
    private fun tryExecutePendingScroll() {
        val coilToScroll = pendingCoilScroll ?: return
        val response = receiveViewModel.receiveFragmentAcceptSearchResponse.value ?: return


        // Проверяем готовность контейнера
        if (!isCoilsContainerReady) {

            return
        }

        val scrollView = horizontalScrollViewCoils ?: return
        val container = coilsContainer ?: return

        // Ищем катушку
        val coilIndex = response.coils.indexOfFirst { it.num == coilToScroll.toIntOrNull() }
        if (coilIndex == -1) {
            showErrorMessage("Катушка $coilToScroll не найдена")
            pendingCoilScroll = null
            return
        }

        // Получаем View (может быть null, если ещё не отрисована)
        val coilView = container.getChildAt(coilIndex)
        if (coilView == null) {
            // Если View нет — ждём следующего кадра
            scrollView.post {
                tryExecutePendingScroll()
            }
            return
        }

        // Выполняем прокрутку
        scrollView.post {
            scrollView.scrollTo(coilView.left, 0)

        }

        pendingCoilScroll = null
        isCoilsContainerReady = false
        pendingScrollAttempts = 0
    // сбрасываем для следующего обновления
    }

    private fun handle3N0Scan(stringScanResult: String) {
        val parts = stringScanResult.split('$')
        if (parts.size > 1) Nkat = parts[1]

        requireArguments().putSerializable(PARAM_STEP_1_VALUE, stringScanResult)
        step1.setText(stringScanResult)
        lastQR = stringScanResult

        // Всегда просто запускаем поиск
        receiveViewModel.step1AcceptSearch(stringScanResult, Nkat)
    }

    private fun handleCScan(stringScanResult: String) {
        // Извлекаем данные из QR-кода
        val content = stringScanResult.substring(1)
        if (content.length != 12) {
            showErrorMessage("QR-код после 'C' должен содержать 12 цифр")
            return
        }

        val shelfPart = content.substring(0, 4)
        val levelPart = content.substring(4, 8)
        val cellPart = content.substring(8, 12)

        val stel = shelfPart.toIntOrNull()?.toString() ?: ""
        val level = levelPart.toIntOrNull()?.toString() ?: "0"
        val cell = cellPart.toIntOrNull()?.toString() ?: "0"
        val yach = if (level.isNotEmpty() && cell.isNotEmpty()) "${level}.${cell}" else ""


        // Получаем текущие данные
        val currentItem = receiveViewModel.receiveFragmentAcceptSearchResponse.value
        if (currentItem != null) {
            infoTextView.visibility = View.GONE
            val currentStel = currentItem.stel
            val currentYach = currentItem.cell


            if (stel == currentStel && yach == currentYach) {
                if (isBottle)
                {
                    receiveViewModel.putBottle2Sklad(
                        lastStel,
                        lastCell,
                        Nkat,
                        true,  // isOk = true,
                        rgmValue
                    )
                }
                else{
                    receiveViewModel.putKat2Sklad(
                        currentStel, currentYach, Nkat,
                        isOk = true,
                        coil = false,
                        rgmValue
                    )
                }
                // Совпадение: выполняем putKat2Sklad


                // Сохраняем stel и yach
                lastStel = stel
                lastCell = yach

                updateInfoTextView(isMatch = true)
                receiveViewModel.setSkipNextPut(true)

            } else {
                updateInfoTextView(isMatch = false)
                showErrorMessage("Данные не совпадают с текущим элементом")
            }
        } else {
            showErrorMessage("Нет данных для сравнения (ответ пуст)")
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
            stelFromQR = ""
            yachFromQR = ""

            // Обновляем UI
            infoTextView.visibility = View.VISIBLE
            requireArguments().putSerializable(PARAM_STEP_1_VALUE, Nkat)
            step1.setText(stringScanResult)
            lastQR = stringScanResult

            receiveViewModel.step1AcceptSearch(stringScanResult,Nkat)
            Timber.tag("ReceiveFragment").d("Обработан QR бутылки: Nkat=$Nkat")

        } catch (e: Exception) {
            showErrorMessage("Ошибка обработки QR-кода бутылки: ${e.message}")
            Timber.tag("ReceiveFragment").e(e, "Bottle scan error")
        }
    }

    private fun updateInfoTextView(isMatch: Boolean) {

            infoTextView.visibility = View.VISIBLE
            if (isMatch) {
                infoTextView.setBackgroundColor(Color.argb(255, 0, 255, 0)) // Зелёный

            } else {
                infoTextView.setBackgroundColor(Color.argb(255, 255, 0, 0)) // Красный
                val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f)
            }

    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("qq",layoutManager.onSaveInstanceState())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            @Suppress("DEPRECATION")
            layoutManagerOnSaveInstanceStateParcelable=it.getParcelable("qq")
        }
    }

    inner class AdapterReceive:BaseRecyclerAdapter<AcceptScanResponse>(AcceptScanResponse()){
        override fun getCallback(dataOld: AcceptScanResponse?): DiffUtil.Callback {
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

        override fun appendData(dataNew: AcceptScanResponse) {
            if (dataNew.total>0){
                data.last=dataNew.last
                data.found.addAll(dataNew.found)
            }
        }

        override fun getLastId(): Any {
            return data.last
        }

        override fun cloneData(): AcceptScanResponse {
            return data.copy(found = ArrayList(data.found))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(
                TemplateCardBinding.inflate(layoutInflater,parent,false).root){}
        }

        override fun getItemCount(): Int {
            return data.found.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bindingItem=
                TemplateCardBinding.bind(holder.itemView)
            val  dataItem=
                data.found[position]

            bindingItem.apply {
                containerVertical.removeAllViews()
                containerVertical.setOnClickListener {
                    receiveViewModel.mainActivityRouter.navigate(
                        ReceiveFragmentInfo::class.java,
                        Bundle().apply {
                            putSerializable(ReceiveFragmentInfo.PARAM,dataItem.id)
                        }
                    )
                }
                arrayOf(
                    Pair(arrayOf("name"),""),
                    Pair(arrayOf("id"),"#компонента "),
                    Pair(arrayOf("batch"),"Серия "),
                    Pair(arrayOf("element"),"Элемент "),
                    Pair(arrayOf("nominal"),"Номинал "),
                    Pair(arrayOf("case"),"Корпус "),
                    Pair(arrayOf("rack"),"Ст. "),
                    Pair(arrayOf("cell"),"Яч. "),
                ).forEach{
                    containerVertical.addView(
                        TemplatePresenterBinding.inflate(layoutInflater,containerVertical,false)
                            .apply {
                                setAttribute(it,dataItem)

                            }
                            .root
                    )
                }
                if (dataItem.coils.isNotEmpty()) {
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
                    dataItem.coils.forEach { coil ->
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
                        when (coil.st) {
                            1 -> {
                                coilView.setBackgroundColor(
                                    ContextCompat.getColor(coilView.context, R.color.red_half)
                                )
                            }
                            2 -> {
                                coilView.setBackgroundColor(
                                    ContextCompat.getColor(coilView.context, R.color.red_half)
                                )
                            }
                            3 -> {
                                coilView.setBackgroundColor(
                                    ContextCompat.getColor(coilView.context, R.color.yellow_highlight)
                                )
                            }
                            else
                            -> {
                                coilView.background = null
                                tvNum.setTextColor(Color.GRAY)
                            }
                        }
                        // endregion

                        coilView.addView(tvType)
                        coilView.addView(tvNum)
                        coilsContainer.addView(coilView)
                    }

                    horizontalScrollView.addView(coilsContainer)
                    bindingItem.containerVertical.addView(horizontalScrollView)
                }
            }
        }

    }


    class ReceiveViewModel(
        private val apiPantes: ApiPantes,
        private val loginRepository: LoginRepository,
        val pref: Pref
    ) : BaseViewModel()
    {
        var skipNextPut = false
            private set
        fun setSkipNextPut(skip: Boolean) {
            skipNextPut = skip
        }
//        var lastStoredStel: String = ""
//        var lastStoredCell: String = ""

//        fun saveStelAndCell(stel: String, cell: String) {
//            lastStoredStel = stel
//            lastStoredCell = cell
//        }
//
//        fun clearStelAndCell() {
//            lastStoredStel = ""
//            lastStoredCell = ""
//        }
        fun step2AcceptScan(query: String, last:String) {
            ioCoroutineScope.launch {
                receiveFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> ReceiveFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.acceptScan(
                                token = token,
                                last = last,
                                query = query,
                            )){
                                is ApiPantes.ApiState.Success->
                                    ReceiveFragmentFormState.SuccessScan(result.data)
                                is ApiPantes.ApiState.Error->
                                    ReceiveFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )
            }
        }
        fun step1AcceptSearch(query: String, scrollToCoil: String? = null) {
            ioCoroutineScope.launch {
                receiveFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> ReceiveFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.acceptSearch(
                                token = token,
                                query = query,
                            )){
                                is ApiPantes.ApiState.Success->
                                    ReceiveFragmentFormState.SuccessSearch(result.data,
                                        scrollToCoil)
                                is ApiPantes.ApiState.Error->
                                    ReceiveFragmentFormState.Error(
                                        if (result.exception is NonFatalExceptionShowDialogMessage){
                                            NonFatalExceptionShowToaste(result.exception.message)
                                        }
                                        else{
                                            result.exception
                                        }
                                    )
                            }
                        }
                    }
                )
            }
        }

        fun step3AcceptSearch(query: String, scrollToCoil: String? = null) {
            ioCoroutineScope.launch {
                receiveFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> ReceiveFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.acceptSearchBottle(
                                token = token,
                                query = query,
                            )){
                                is ApiPantes.ApiState.Success->
                                    ReceiveFragmentFormState.SuccessSearch(result.data,scrollToCoil)
                                is ApiPantes.ApiState.Error->
                                    ReceiveFragmentFormState.Error(
                                        if (result.exception is NonFatalExceptionShowDialogMessage){
                                            NonFatalExceptionShowToaste(result.exception.message)
                                        }
                                        else{
                                            result.exception
                                        }
                                    )
                            }
                        }
                    }
                )
            }
        }
        fun putKat2Sklad(
            stel: String,
            shelf: String,
            curKat: String,
            isOk: Boolean,
            coil: Boolean,
            rgm: String
        ) {
            ioCoroutineScope.launch {

                    when(val token=loginRepository.user?.token){
                        null-> ReceiveFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.acceptPutkat(
                                token = token,
                                Stel= stel,
                                Shelf = shelf,
                                curKat= curKat,
                                isOk = isOk,
                                coil = coil,
                                rgm = rgm
                            )){
                                is ApiPantes.ApiState.Success -> {
                                    receiveFragmentFormState.postValue(ReceiveFragmentFormState.PutKatSuccess)
                                }
                                is ApiPantes.ApiState.Error -> {
                                    receiveFragmentFormState.postValue(
                                        ReceiveFragmentFormState.Error(result.exception)
                                    )
                                }
                            }
                        }
                    }

            }
        }
        fun putBottle2Sklad(stel : String,shelf : String,curKat : String,isOk: Boolean,rgm: String) {
            ioCoroutineScope.launch {

                when(val token=loginRepository.user?.token){
                    null-> ReceiveFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                    else->{
                        when(val result = apiPantes.acceptPutbottle(
                            token = token,
                            Stel= stel,
                            Shelf = shelf,
                            curKat= curKat,
                            isOk = isOk,
                            rgm = rgm
                        )){
                            is ApiPantes.ApiState.Success->
                                receiveFragmentFormState.postValue(ReceiveFragmentFormState.PutKatSuccess)
                            is ApiPantes.ApiState.Error->
                                ReceiveFragmentFormState.Error(result.exception)
                        }
                    }
                }

            }
        }
        val receiveFragmentTitle=
            MutableLiveData<String?>()
        val receiveFragmentSubtitle=
            MutableLiveData<String?>()
        val receiveFragmentAcceptSearchResponse=
            MutableLiveData<AcceptSearchResponse>()


        val receiveFragmentVisibleEmpty=
            MutableLiveData<Int>()
        val receiveFragmentVisibleStep1=
            MutableLiveData<Int>()
        val receiveFragmentVisibleStep2=
            MutableLiveData<Int>()

        val receiveFragmentFormState=
            MutableLiveData<ReceiveFragmentFormState<*>>()

        companion object {
            fun getInstance(context: Context): ReceiveViewModel {
                return ReceiveViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context),
                    Pref.getInstanceSingleton(context)
                )
            }
        }
    }

    sealed class ReceiveFragmentFormState<out T:Any>
    {

        data class Error(var exception: Throwable): ReceiveFragmentFormState<Nothing>()
        data class SuccessScan<out T : Any>(val data:T): ReceiveFragmentFormState<T>()
        data class SuccessSearch<out T : Any>(val data: AcceptSearchResponse,val scrollToCoil: String? ): ReceiveFragmentFormState<T>()
        data object ResetSearch: ReceiveFragmentFormState<Nothing>()
        data object ResetScan: ReceiveFragmentFormState<Nothing>()
        data object RequestSearch: ReceiveFragmentFormState<Nothing>()
        data object RequestSearchBottle: ReceiveFragmentFormState<Nothing>()
        data object RequestScan: ReceiveFragmentFormState<Nothing>()
        data object SetupForm: ReceiveFragmentFormState<Nothing>()
        data object NoOp : ReceiveFragmentFormState<Nothing>()
        data object PutKatSuccess : ReceiveFragmentFormState<Nothing>() // сигнал, что PUT выполнен
        data object PutKatError : ReceiveFragmentFormState<Nothing>()   // если нужно обработать ошибк

    }
}