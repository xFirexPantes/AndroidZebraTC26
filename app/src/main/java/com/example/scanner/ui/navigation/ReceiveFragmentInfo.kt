package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.app.batch2
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateContentBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateTabLayoutBinding
import com.example.scanner.models.AcceptInfoResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.launch
import kotlin.Any

class ReceiveFragmentInfo:BaseFragment() {
    companion object{
        const val PARAM="param"
    }

    private val receiveInfoModelView: ReceiveInfoModelView by viewModels{ viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiveInfoModelView.receiveInfoFragmentFormState
            .postValue(ReceiveInfoFragmentFormState.SetupForm)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply {

                toolbar.apply {
                    setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }
                    receiveInfoModelView.receiveInfoFragmentTitle.observe(viewLifecycleOwner){
                        title=it
                    }
                }

                val layoutContent =
                    TemplateCardBinding.inflate(inflater,root,false)

                val layoutTabs=
                    TemplateTabLayoutBinding.inflate(inflater,root,false)
                        .apply {

                            tabLayout.addOnTabSelectedListener(
                                object : OnTabSelectedListener {
                                    override fun onTabSelected(tab: TabLayout.Tab?) {
                                        layoutContent.containerVertical.removeAllViews()
                                        when(val pos=tab?.position){
//                                            0->{
//                                                layoutContent.containerVertical.addView(
//                                                    TemplateContentBinding.inflate(inflater, root, false)
//                                                        .apply {
//                                                            arrayOf(
//                                                                Pair(arrayOf("id"), "#компонента "),
//                                                                Pair(arrayOf("batch"), "Серия "),
//                                                                Pair(arrayOf("element"), "Элемент "),
//                                                                Pair(arrayOf("nominal"), "Номинал "),
//                                                                Pair(arrayOf("markings"), "Маркировка "),
//                                                                Pair(arrayOf("coil"), "Катушка "),
//                                                                Pair(arrayOf("image"), ""),
//                                                            ).forEach { pair ->
//                                                                root.addView(
//                                                                    TemplatePresenterBinding.inflate(
//                                                                        inflater,
//                                                                        root,
//                                                                        false
//                                                                    ).apply {
//                                                                        receiveInfoModelView.receiveInfoFragmentAcceptInfoResponse.value
//                                                                            ?.let {
//                                                                                setAttr(pair,it)
//                                                                            }
//                                                                    }
//                                                                        .root
//                                                                )
//                                                            }
//                                                        }
//                                                        .root
//                                                )
//                                            }
                                            null->{}
                                            else -> {
//                                                val tabNumber=pos-1
                                                val tabNumber=pos
                                                layoutContent.containerVertical.addView(
                                                    TemplateContentBinding.inflate(inflater,layoutContent.containerVertical,false)
                                                        .apply {
                                                            receiveInfoModelView.receiveInfoFragmentAcceptInfoResponse.value
                                                                ?.let {acceptInfoResponse->
                                                                    val tab=acceptInfoResponse.tabs[tabNumber]
                                                                    tab.attributes.forEach {
                                                                        root.addView(
                                                                            TemplatePresenterBinding.inflate(inflater,this.root,false)
                                                                                .apply {
                                                                                    setAttribute(
                                                                                        Pair(arrayOf(it.type,it.name),it.name.toString()),
                                                                                        acceptInfoResponse
                                                                                    )
                                                                                }
                                                                                .root
                                                                        )
                                                                    }
                                                                }
                                                        }
                                                        .root
                                                )
                                            }
                                        }
                                    }

                                    override fun onTabUnselected(tab: TabLayout.Tab?) {

                                    }

                                    override fun onTabReselected(tab: TabLayout.Tab?) {

                                    }

                                })

                            receiveInfoModelView.receiveInfoFragmentAcceptInfoResponse.observe(viewLifecycleOwner){
                                tabLayout.removeAllTabs()
                                it?.let {
                                    receiveInfoModelView.receiveInfoFragmentTitle.postValue(
                                        it.name
                                    )
                                    //tabLayout.addTab(tabLayout.newTab().apply { setText("Характеристики") })
                                    it.tabs.forEach { tabItem->
                                        tabLayout.addTab(
                                            tabLayout.newTab()
                                                .apply {
                                                    setText(tabItem.name)
                                                }
                                        )
                                    }
                                }
                            }

                        }

                root.addView(layoutTabs.root)
                root.addView(layoutContent.root)
            }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiveInfoModelView.receiveInfoFragmentFormState
            .postValue(ReceiveInfoFragmentFormState.SetupForm)

        receiveInfoModelView.receiveInfoFragmentFormState.observe(viewLifecycleOwner){
            when(val state=it){
                is ReceiveInfoFragmentFormState.SetupForm->{
                    viewLifecycleOwner.batch2(
                        receiveInfoModelView.receiveInfoFragmentFormState,
                        ArrayList<ReceiveInfoFragmentFormState<Any>>()
                            .apply {
                                if (receiveInfoModelView.receiveInfoFragmentAcceptInfoResponse.value==null){
                                    add(ReceiveInfoFragmentFormState.Request)
                                }
                            }
                    )

                }
                is ReceiveInfoFragmentFormState.Error ->{
                    state.exception?.let {
                        findNavController().navigateUp()
                        receiveInfoModelView.mainActivityRouter.navigate(
                            ErrorsFragment::class.java,
                            Bundle().apply {
                                putSerializable(
                                    ErrorsFragment.PARAM,
                                    state.exception
                                )
                            })
                    }
                }
                is ReceiveInfoFragmentFormState.Success ->{
                    state.data?.let {
                        it as AcceptInfoResponse
                        receiveInfoModelView.receiveInfoFragmentAcceptInfoResponse
                            .postValue(it)
                    }
                }
                is ReceiveInfoFragmentFormState.Request->{
                    receiveInfoModelView
                        .requestAcceptInfo(
                            getArgument(PARAM)
                        )
                }
            }
        }

    }

    sealed class ReceiveInfoFragmentFormState<out T:Any>  {
        data class Error(private var _exception: Throwable?): ReceiveInfoFragmentFormState<Nothing>(){
            val exception: Throwable?
                get() {
                    val temp=_exception
                    _exception=null
                    return temp
                }
        }
        data class Success<out T : Any>(private var _data:T?): ReceiveInfoFragmentFormState<T>(){
            val data:T?
                get() {
                    val tmp:T?=_data
                    _data=null
                    return tmp
                }
        }
        data object Request: ReceiveInfoFragmentFormState<Nothing>()
        data object SetupForm: ReceiveInfoFragmentFormState<Nothing>()
    }

    class ReceiveInfoModelView(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository):BaseViewModel(){

        companion object {
            fun getInstance(context: Context): ReceiveInfoModelView {
                return ReceiveInfoModelView(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context)
                )
            }
        }

        fun requestAcceptInfo(id: String) {
            ioCoroutineScope.launch {
                receiveInfoFragmentFormState.postValue(
                    when(val token=loginRepository.user?.token){
                        null-> ReceiveInfoFragmentFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else->{
                            when(val result = apiPantes.acceptInfo(
                                token = token,
                                component = id
                            )){
                                is ApiPantes.ApiState.Success->
                                    ReceiveInfoFragmentFormState.Success(result.data)
                                is ApiPantes.ApiState.Error->
                                    ReceiveInfoFragmentFormState.Error(result.exception)
                            }
                        }
                    }
                )
            }
        }

        val receiveInfoFragmentTitle=
            MutableLiveData<String?>()

        val receiveInfoFragmentAcceptInfoResponse=
            MutableLiveData<AcceptInfoResponse?>()

        val receiveInfoFragmentFormState=
            MutableLiveData<ReceiveInfoFragmentFormState<*>>()

    }

}

