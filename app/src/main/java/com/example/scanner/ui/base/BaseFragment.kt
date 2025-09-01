package com.example.scanner.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.MutableLiveData
import com.example.scanner.modules.Other
import com.example.scanner.ui.MainActivity
import com.example.scanner.ui.navigation.login.LoginFragment
import com.example.scanner.ui.navigation.login.LoginRepository
import kotlinx.coroutines.launch

open class BaseFragment:Fragment() {

    protected val mutableOnFragmentDetached=MutableLiveData<Other.SAD<Fragment>>()

    private val fragmentLifecycleCallbacks=object : FragmentLifecycleCallbacks(){


        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            super.onFragmentDetached(fm, f)
                mutableOnFragmentDetached.value= Other.SAD(f)
        }

    }

    protected var scanViewModelReference: ScanFragmentBase.ScanViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        parentFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentLifecycleCallbacks,
            true
        )
        super.onCreate(savedInstanceState)
        scanViewModelReference?.installScannerApi()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        Timber.tag("lifecycle").i("onViewCreated:$this")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
//        Timber.tag("lifecycle").i("onDestroy:$this")
        parentFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        Other.getInstanceSingleton().mainCoroutineScope.launch {
            scanViewModelReference?.scannerApi?.apply {
                start(requireContext())
                resume()
            }
        }

        if (LoginRepository.getInstanceSingleton(requireContext()).isSessionTimeExpired()) {
            MainActivity.MainActivityRouter.getInstanceSingleton()
                .navigate(LoginFragment::class.java)
        }

    }

    override fun onPause() {
        scanViewModelReference?.scannerApi?.apply {
            pause()
            finish()
        }
        super.onPause()
    }




    fun finalize(){
        "".toString()
    }

    fun <T>getArgument(key: String):T{
        return Other.getInstanceSingleton().parseArguments<T>(requireArguments(),key)
    }
}