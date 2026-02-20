package com.example.scanner.ui.navigation

import android.content.Context
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.scanner.R
import com.example.scanner.app.setAttribute
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateFrameBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.databinding.TemplateTabLayoutBinding
import com.example.scanner.models.ComponentInfoResponse
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import com.example.scanner.app.downloadFile
import com.example.scanner.app.valueByName
import com.example.scanner.ui.base.NonFatalExceptionShowToaste


class ComponentFragmentInfo: BaseFragment() {

    companion object{
        const val PARAM="param"
    }
    //private val tabMainName="Характеристики"
    private val componentsInfoViewModel:ComponentsInfoViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply {

                toolbar.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }

                root.addView(
                    TemplateTabLayoutBinding.inflate(inflater,root,false)
                        .apply {
                            tabLayout.removeAllTabs()
                            componentsInfoViewModel.componentsInfoFormState.observe(viewLifecycleOwner){
                                when(val state=it){
                                    is ComponentsInfoFormState.SuccessComponentInfo->{
                                        state.data as ComponentInfoResponse
                                        toolbar.title=state.data.name
                                        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                                            override fun onTabSelected(tab: TabLayout.Tab) {
                                                childFragmentManager.fragments.forEach {oldFragment->
                                                    childFragmentManager.beginTransaction().remove(oldFragment).commitNow()
                                                }
                                                childFragmentManager
                                                    .beginTransaction()
                                                    .replace(
                                                        R.id.fragment_container,
                                                        when(tab.position){
//                                                            0->ComponentInfoFragmentTabMain()
//                                                                .apply {
//                                                                    arguments=
//                                                                        Bundle()
//                                                                            .apply {
//                                                                                putSerializable(
//                                                                                    ComponentInfoFragmentTabMain.PARAM,
//                                                                                    state.data)
//                                                                            }
//                                                                }

                                                            else->ComponentInfoFragmentTabOther()
                                                                .apply {
                                                                    arguments=
                                                                        Bundle()
                                                                            .apply {
                                                                                putSerializable(
                                                                                    ComponentInfoFragmentTabOther.PARAM,
                                                                                    state.data)
                                                                                putSerializable(
                                                                                    ComponentInfoFragmentTabOther.PARAM1,
                                                                                    tab.position)
                                                                                    //tab.position-1)
                                                                            }
                                                                }
                                                        }
                                                    ).commit()
                                            }

                                            override fun onTabUnselected(tab: TabLayout.Tab?) {

                                            }

                                            override fun onTabReselected(tab: TabLayout.Tab?) {

                                            }
                                        })
//                                        tabLayout.addTab(tabLayout.newTab().apply {
//                                            setText(tabMainName)
//                                        })
                                        state.data.tabs.forEach { tabItem->
                                            tabLayout.addTab(
                                                tabLayout.newTab().apply {
                                                    this.setText(StringBuilder(tabItem.name))
                                                })
                                        }

                                    }
                                    is ComponentsInfoFormState.Error-> {
                                        componentsInfoViewModel.mainActivityRouter
                                            .navigate(
                                                ErrorsFragment::class.java,
                                                Bundle().apply {
                                                    putSerializable(
                                                        ErrorsFragment.PARAM,
                                                        state.exception
                                                    )
                                                })
                                    }
                                }
                            }
                        }
                        .root
                )

                root.addView(
                    TemplateFrameBinding.inflate(inflater,root,false).root
                )


                componentsInfoViewModel.requestComponentInfo(
                    Other.getInstanceSingleton().parseArguments(requireArguments(), PARAM))

            }
            .root
    }

    sealed class ComponentsInfoFormState<out T : Any> {
        data class SuccessComponentInfo<out T : Any>(val data:T): ComponentsInfoFormState<T>()
        data class Error(val exception: Throwable) : ComponentsInfoFormState<Nothing>()
    }

    class ComponentsInfoViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository) :
        BaseViewModel() {
        fun requestComponentInfo(id: String) {
            ioCoroutineScope.launch {
                componentsInfoFormState.postValue(
                    when(val token=loginRepository.user?.token) {
                        null -> ComponentsInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when(val result=apiPantes.componentInfo(token,id)){
                            is ApiPantes.ApiState.Success->
                                ComponentsInfoFormState.SuccessComponentInfo(result.data)
                            is ApiPantes.ApiState.Error->
                                ComponentsInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }

        companion object {
            fun getInstance(context: Context): ComponentsInfoViewModel {
                return ComponentsInfoViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context)
                )
            }
        }

        val componentsInfoFormState=
            MutableLiveData<ComponentsInfoFormState<*>>()

    }

    class ComponentInfoFragmentTabOther: BaseFragment() {
        private val componentsInfoViewModel:ComponentsInfoViewModel by viewModels { viewModelFactory }

        companion object{
            const val PARAM="param"
            const val PARAM1="param1"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val componentInfoResponse: ComponentInfoResponse =
                Other.getInstanceSingleton()
                    .parseArguments(requireArguments(),PARAM)
            val tabNumber:Int=
                Other.getInstanceSingleton()
                    .parseArguments(requireArguments(),PARAM1)


            return TemplateCardBinding.inflate(inflater,container,false)
                .apply {
                    componentInfoResponse.tabs[tabNumber].attributes.forEach { tabAttributeItem ->
                        containerVertical.addView(
                            TemplatePresenterBinding.inflate(inflater,this.root,false)
                                .apply {
                                    setAttribute(
                                        Pair(
                                            if (tabAttributeItem.type=="document")
                                                arrayOf(
                                                    tabAttributeItem.type,
                                                    tabAttributeItem.name,
                                                    object :ClickableSpan(){
                                                        override fun onClick(
                                                            widget: View
                                                        ) {
                                                            try {
                                                                componentInfoResponse.valueByName("document").toString().downloadFile(requireContext())
//                                                                val  strSrc= Uri.decode(componentInfoResponse.valueByName("document").toString())
//                                                                val request = DownloadManager.Request(strSrc.toUri())
//                                                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                                                                request.setDestinationUri(Uri.fromFile(
//                                                                    File(
//                                                                        Environment.getExternalStoragePublicDirectory(
//                                                                            Environment.DIRECTORY_DOWNLOADS
//                                                                        ), File(strSrc).name
//                                                                    )
//                                                                ))
//                                                                val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                                                                downloadManager.enqueue(request)

                                                            }catch (e: Exception){
                                                                componentsInfoViewModel.mainActivityRouter.navigate(
                                                                    ErrorsFragment::class.java,
                                                                    Bundle().apply {
                                                                        putSerializable(
                                                                            ErrorsFragment.PARAM,
                                                                            NonFatalExceptionShowToaste("Ошибка загрузки ${e.message}"))
                                                                    }
                                                                )
                                                            }

                                                        }
                                                    }
                                                )
                                            else
                                                arrayOf(
                                                    tabAttributeItem.type,
                                                    tabAttributeItem.name
                                                ),
                                            tabAttributeItem.name
                                        ),
                                        componentInfoResponse,
                                        componentsInfoViewModel.mainActivityRouter
                                    )
                                }
                                .root
                        )

                    }
                }
                .root


        }

    }
}