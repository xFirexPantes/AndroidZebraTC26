package com.example.scanner.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.R
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import kotlinx.coroutines.launch
import com.example.scanner.models.IsolatorListInfoResponse


class IsolatorListFragmentInfo : BaseFragment() {

    companion object {
        const val PARAM = "param"
    }

    private val isolatorListIViewModel: IsolatorListInfoViewModel by viewModels { viewModelFactory }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Устанавливаем layout
        val view = inflater.inflate(R.layout.fragment_isolator_list_info, container, false)

        // Находим view по ID
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        val containerList: LinearLayout = view.findViewById(R.id.containerList)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()  // или activity.onBackPressed()
        }
        // Настройка toolbar

        toolbar.apply {

            title = "Данные упаковок"
        }
        // Наблюдаем за состоянием ViewModel

        isolatorListIViewModel.isolatorListInfoFormState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is IsolatorListInfoFormState.SuccessIsolatorListInfo -> {
                    val data = state.data as IsolatorListInfoResponse
                   // toolbar.title = "Данные катушек"  // Можно взять из ответа, если есть поле

                    // Очищаем контейнер
                    containerList.removeAllViews()

                    // Заполняем список
                    data.coils.forEach { attribute ->
                        // Инфлейтим элемент
                        val itemView = inflater.inflate(R.layout.item_isolator_list_info, containerList, false)

                        // Находим TextView
                        val tvType: TextView = itemView.findViewById(R.id.type)
                        val tvNumber: TextView = itemView.findViewById(R.id.number)
                        val tvOst: TextView = itemView.findViewById(R.id.ost)
                        val tvCab: TextView = itemView.findViewById(R.id.Cab)

                        // Заполняем данные
                        tvType.text = attribute.type
                        tvNumber.text = attribute.number.toString()
                        tvOst.text = "Остаток: ${attribute.ost}"
                        tvCab.text = "Печь: ${attribute.Cab}"

                        // Можно добавить разделитель
                        val separator = View(requireContext()).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                1
                            )
                            setBackgroundColor(Color.LTGRAY)
                        }
                        containerList.addView(separator)

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
            }
        }

        // Запрашиваем данные
        isolatorListIViewModel.requestIsolatorListInfo(
            Other.getInstanceSingleton().parseArguments(requireArguments(), PARAM)
        )

        return view
    }
    sealed class IsolatorListInfoFormState<out T : Any> {
        data class SuccessIsolatorListInfo<out T : Any>(val data:T): IsolatorListInfoFormState<T>()
        data class Error(val exception: Throwable) : IsolatorListInfoFormState<Nothing>()
    }

    class IsolatorListInfoViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository) :
        BaseViewModel() {
        fun requestIsolatorListInfo(id: Int) {
            ioCoroutineScope.launch {
                isolatorListInfoFormState.postValue(
                    when(val token=loginRepository.user?.token) {
                        null -> IsolatorListInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when(val result=apiPantes.isolatorListInfo(token,id)){
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
            MutableLiveData<IsolatorListInfoFormState<*>>()

    }

}


