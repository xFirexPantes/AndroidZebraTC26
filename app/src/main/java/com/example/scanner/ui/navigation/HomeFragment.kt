package com.example.scanner.ui.navigation

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.scanner.R
import com.example.scanner.app.floatDisable
import com.example.scanner.app.floatEnable
import com.example.scanner.databinding.FragmentHomeBinding
import com.example.scanner.modules.Other
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.dialogs.FileDownload
import com.example.scanner.ui.navigation.login.LoginFragment
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.example.scanner.ui.navigation_over.ProgressFragment
import com.example.scanner.ui.navigation_over.TransparentFragment
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import androidx.core.net.toUri


class HomeFragment : BaseFragment() {

    private val homeViewModel: HomeViewModel by viewModels{ viewModelFactory }
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.loginInit()
        Other.getInstanceSingleton().ioCoroutineScope.launch {
            scanViewModel.mainActivityRouter.navigate(
                ProgressFragment::class.java
            )
            scanViewModel.mainActivityRouter.navigate(
                TransparentFragment::class.java
            )
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return FragmentHomeBinding.inflate(inflater, container, false)
        .apply {
            homeViewModel.homeFragmentFormState.observe(viewLifecycleOwner,{
                when(val state=it){
                    is HomeFragmentFormState.SetView ->{
                        userViewText.text=state.username

                        fun forbiddenToast(){
                            Toast.makeText(requireContext(),"Доступ к данному разделу запрещен",
                                Toast.LENGTH_SHORT).show()
                        }

                        issuance.button.alpha=
                            if(state.issuance) {
                                issuance.button.setOnClickListener {
                                    homeViewModel.mainActivityRouter.navigate(
                                        InvoiceFragment::class.java,
                                        Bundle().apply { putSerializable(InvoiceFragment.PARAM,"") }
                                    )
                                }
                                floatEnable
                            } else {
                                issuance.button.setOnClickListener { forbiddenToast()}
                                floatDisable
                            }

                        search.button.alpha=
                            if(state.search) {
                                search.button.setOnClickListener {
                                    homeViewModel.mainActivityRouter.navigate(
                                        ComponentFragment::class.java,
                                        Bundle().apply { putSerializable(ComponentFragment.PARAM,"") }
                                    )
                                }
                                floatEnable
                            }
                            else{
                                search.button.setOnClickListener { forbiddenToast()}
                                floatDisable
                            }

                        isolator.button.alpha=
                            if(state.isolator) {
                                isolator.button.setOnClickListener {
                                    //homeViewModel.mainActivityRouter.navigate(ScanIsolatorFragment::class.java)
                                    homeViewModel.mainActivityRouter.navigate(
                                        IsolatorFragment::class.java,
                                        Bundle().apply { putSerializable(IsolatorFragment.PARAM,"") }
                                    )
                                }
                                floatEnable
                            }
                            else{
                                isolator.button.setOnClickListener { forbiddenToast()}
                                floatDisable
                            }


                        accept.button.alpha=
                            if(state.accept) {
                                accept.button.setOnClickListener {
    //                            homeViewModel.mainActivityRouter.navigate(
    //                                ScanReceiveFragment::class.java)
                                    homeViewModel.mainActivityRouter.navigate(
                                        ReceiveFragment::class.java,
                                        Bundle().apply { putSerializable(ReceiveFragment.PARAM_STEP_1_VALUE,"") }
                                    )
                                }
                                floatEnable
                            }
                            else{
                                accept.button.setOnClickListener { forbiddenToast()}
                                floatDisable
                            }
                        incontrol.button.alpha=
                            if(state.incontrol) {
                                incontrol.button.setOnClickListener {
                                    //                            homeViewModel.mainActivityRouter.navigate(
                                    //                                ScanReceiveFragment::class.java)
                                    homeViewModel.mainActivityRouter.navigate(
                                        InControlMenuFragment::class.java,
                                        Bundle().apply { putSerializable(InControlMenuFragment.PARAM_STEP_1_VALUE,"") }
                                    )
                                }
                                floatEnable
                            }
                            else{
                                incontrol.button.setOnClickListener { forbiddenToast()}
                                floatDisable
                            }



                        userViewContainer.setOnClickListener {
                            PopupMenu(requireContext(), it)
                                .apply {
                                    setOnMenuItemClickListener { item->
                                        when(item.itemId){
                                            R.id.menu_logout-> {
                                                homeViewModel.loginRepository.logout()
                                                homeViewModel.mainActivityRouter.navigate(LoginFragment::class.java)
                                            }
                                        }
                                        true }
                                    inflate(R.menu.actions_user)
                                    show()
                                }
                        }
                        homeViewModel.batch()

                    }
                }
            })
            val versionTextView: TextView = root.findViewById(R.id.textVersion)

            // Получаем версию приложения
            try {
                val packageInfo = requireActivity().packageManager.getPackageInfo(
                    requireActivity().packageName, 0
                )
                val versionName = packageInfo.versionName
                versionTextView.text = "Версия: $versionName"
            } catch (e: PackageManager.NameNotFoundException) {
                versionTextView.text = "Версия: не найдена"
            }

        }
        .root

    }



    class HomeViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

        companion object {
            private var _HomeViewModel: WeakReference<HomeViewModel>? = null
            fun getInstanceSingleton(app: Context): HomeViewModel {
                return _HomeViewModel
                    ?.get()
                    ?:run {
                        HomeViewModel(LoginRepository.getInstanceSingleton(app))
                            .apply { _HomeViewModel = WeakReference(this) }
                    }
            }
        }

        val homeFragmentFormState = MutableLiveData<HomeFragmentFormState<*>>()



        var array=arrayOf(
//            Pair(
//                InvoiceFragmentLines::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragmentLines.PARAMS_INVOICE_ID,"135807")
//                    putSerializable(InvoiceFragmentLines.PARAMS1_INVOICE_NAME,"93880")
//                    putSerializable(InvoiceFragmentLines.PARAMS2_COLLECTED,false)
//                }
//            ),
//            Pair(
//                InvoiceFragmentLines::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragmentLines.PARAMS_INVOICE_ID,"135807")
//                    putSerializable(InvoiceFragmentLines.PARAMS1_INVOICE_NAME,"93880-0")
//                    putSerializable(InvoiceFragmentLines.PARAMS2_COLLECTED,true)
//                }
//            ),
//            Pair(
//                InvoiceFragment::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragment.PARAM,"93880")
//                }
//            ),
//            Pair(
//                ReceiveFragment::class.java,
//                Bundle().apply {
//
//                }
//            ),
//            Pair(
//                ComponentFragment::class.java,
//                Bundle().apply {
//                    putSerializable(IsolatorFragment.PARAM,"01")
//                }
//            ),
            Pair(
                ErrorsFragment::class.java,
                Bundle().apply {
                    putSerializable(ErrorsFragment.PARAM, Exception("Test Exception"))
                }
            ),
//            Pair(
//                IsolatorFragment::class.java,
//                Bundle().apply {
//                    putSerializable(IsolatorFragment.PARAM,"01")
//                }
//            ),
//            Pair(
//                InvoiceFragmentInfoLine::class.java,
//                Bundle().apply {
//                    putSerializable(InvoiceFragmentInfoLine.PARAM_INVOICE_ID,"135807")
//                    putSerializable(InvoiceFragmentInfoLine.PARAM_LINE_ID,1395758)
//                    putSerializable(InvoiceFragmentInfoLine.PARAM_LINE_COLLECTED,false)
//                }
//            ),

        )


        fun batch(){
            array=arrayOf()
            array.forEach {
                Other.getInstanceSingleton().mainCoroutineScope.launch {
                    mainActivityRouter.navigate(it.first,it.second)
                    Thread.sleep(1000)
                }
            }
            array=arrayOf()
        }

        fun loginInit() {
            if (loginRepository.isLoggedIn)
                loginRepository.pref.getResource().let { resources: Resources ->
                    loginRepository.user?.let {
                        homeFragmentFormState.value= HomeFragmentFormState.SetView(
                            username = it.name ?: resources.getString(android.R.string.unknownName),
//                            accept = true,
//                            issuance = true,
//                            search = true,
//                            isolator = true
                            accept = it.access.accept,
                            issuance = it.access.issuance,
                            search = it.access.search,
                            isolator = it.access.isolator,
                            incontrol = it.access.incontrol
                        )
                    }
                }
            else
                mainActivityRouter.navigate(LoginFragment::class.java)

        }

    }
}