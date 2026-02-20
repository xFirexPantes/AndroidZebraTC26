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
import com.example.scanner.models.InControlInfoResponse
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


class InControlFragmentInfo: BaseFragment() {

    companion object{
        const val PARAM="param"
    }
    //private val tabMainName="Характеристики"
    private val incontrolInfoViewModel:InControlInfoViewModel by viewModels { viewModelFactory }

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
                            incontrolInfoViewModel.incontrolInfoFormState.observe(viewLifecycleOwner){
                                when(val state=it){
                                    is InControlInfoFormState.SuccessInControlInfo->{
                                        state.data as InControlInfoResponse
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
//                                                            0->incontrolInfoFragmentTabMain()
//                                                                .apply {
//                                                                    arguments=
//                                                                        Bundle()
//                                                                            .apply {
//                                                                                putSerializable(
//                                                                                    incontrolInfoFragmentTabMain.PARAM,
//                                                                                    state.data)
//                                                                            }
//                                                                }

                                                            else->IncontrolInfoFragmentTabOther()
                                                                .apply {
                                                                    arguments=
                                                                        Bundle()
                                                                            .apply {
                                                                                putSerializable(
                                                                                    IncontrolInfoFragmentTabOther.PARAM,
                                                                                    state.data)
                                                                                putSerializable(
                                                                                    IncontrolInfoFragmentTabOther.PARAM1,
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
                                    is InControlInfoFormState.Error-> {
                                        incontrolInfoViewModel.mainActivityRouter
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


                incontrolInfoViewModel.requestincontrolInfo(
                    Other.getInstanceSingleton().parseArguments(requireArguments(), PARAM))

            }
            .root
    }

    sealed class InControlInfoFormState<out T : Any> {
        data class SuccessInControlInfo<out T : Any>(val data:T): InControlInfoFormState<T>()
        data class Error(val exception: Throwable) : InControlInfoFormState<Nothing>()
    }

    class InControlInfoViewModel(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository) :
        BaseViewModel() {
        fun requestincontrolInfo(id: String) {
            ioCoroutineScope.launch {
                incontrolInfoFormState.postValue(
                    when(val token=loginRepository.user?.token) {
                        null -> InControlInfoFormState.Error(ErrorsFragment.nonFatalExceptionShowToasteToken)
                        else -> when(val result=apiPantes.incontrolInfo(token,id)){
                            is ApiPantes.ApiState.Success->
                                InControlInfoFormState.SuccessInControlInfo(result.data)
                            is ApiPantes.ApiState.Error->
                                InControlInfoFormState.Error(result.exception)
                        }
                    }
                )
            }
        }

        companion object {
            fun getInstance(context: Context): InControlInfoViewModel {
                return InControlInfoViewModel(
                    ApiPantes.getInstanceSingleton(),
                    LoginRepository.getInstanceSingleton(context)
                )
            }
        }

        val incontrolInfoFormState=
            MutableLiveData<InControlInfoFormState<*>>()

    }

    class IncontrolInfoFragmentTabOther: BaseFragment() {
        private val incontrolInfoViewModel:InControlInfoViewModel by viewModels { viewModelFactory }

        companion object{
            const val PARAM="param"
            const val PARAM1="param1"
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val incontrolInfoResponse: InControlInfoResponse =
                Other.getInstanceSingleton()
                    .parseArguments(requireArguments(),PARAM)
            val tabNumber:Int=
                Other.getInstanceSingleton()
                    .parseArguments(requireArguments(),PARAM1)


            return TemplateCardBinding.inflate(inflater,container,false)
                .apply {
                    incontrolInfoResponse.tabs[tabNumber].attributes.forEach { tabAttributeItem ->
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
                                                                incontrolInfoResponse.valueByName("document").toString().downloadFile(requireContext())
//                                                                val  strSrc= Uri.decode(incontrolInfoResponse.valueByName("document").toString())
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
                                                                incontrolInfoViewModel.mainActivityRouter.navigate(
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
                                        incontrolInfoResponse,
                                        incontrolInfoViewModel.mainActivityRouter
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