package com.example.scanner.ui.navigation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.Context
import android.graphics.Color
import android.graphics.Color.rgb
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
        val view = inflater.inflate(R.layout.truesign_fragment, container, false)
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()  // или activity.onBackPressed()
        }
        val txtPost : TextView = view.findViewById(R.id.customer)
        val txtnumNakl : TextView = view.findViewById(R.id.numNakl)
        val txtdt : TextView = view.findViewById(R.id.dt)
        val txtkol : TextView = view.findViewById(R.id.kol)
        val txtmsg: TextView = view.findViewById(R.id.msg)
        toolbar.apply {
            title = "Честный Знак"
        }

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
                        if (truesignSearchResponse.msg == "Ok") {
                            txtPost.setText(truesignSearchResponse.customer)
                            txtnumNakl.setText(truesignSearchResponse.numNakl)
                            val dateString = truesignSearchResponse.dt.substringBefore(' ')
                            val date = LocalDate.parse(dateString)
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val formattedDate = date.format(formatter)  // "31.12.2026"
                            txtdt.setText(formattedDate)
                            txtkol.setText(truesignSearchResponse.kol.toString())
                            txtmsg.setTextColor( rgb(0,255,0))
                        }else{
                            txtPost.setText("")
                            txtnumNakl.setText("")
                            txtdt.setText("")
                            txtkol.setText("")
                            txtmsg.setTextColor( rgb(255,0,0))
                        }
                        txtmsg.text = truesignSearchResponse.msg
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


                }
            }

            if (it!= TrueSignFragmentState.Idle){
                truesignViewModel.truesignFragmentState.postValue(
                    TrueSignFragmentState.Idle
                )
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        scanViewModel.scanFragmentBaseFormState.observe(viewLifecycleOwner)
        {
            when(val scanState=it){


                is ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult-> {
                    scanState.stringScanResult?.let { stringScanResult ->

                        arguments = Bundle().apply {
                            putSerializable(PARAM, stringScanResult)
                        }

                        truesignViewModel.truesignFragmentSubtitle
                            .postValue(getString(R.string.format_subtitle, getArgument(PARAM)))
                        truesignViewModel.truesignSearch(getArgument(PARAM),"")

                        truesignViewModel.truesignFragmentReady
                            .postValue(
                                View.GONE
                            )


                    }
                }
                else->{}
            }
        }


        truesignViewModel.truesignFragmentState.postValue(
            TrueSignFragmentState.Idle
        )

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
                            val result = apiPantes.truesignSearch(
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

        val truesignFragmentTitle=
            MutableLiveData<String>()
        val truesignFragmentSubtitle=
            MutableLiveData<String>()

        val truesignFragmentState=
            MutableLiveData<TrueSignFragmentState<*>>()
    }

}