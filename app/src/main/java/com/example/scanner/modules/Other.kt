package com.example.scanner.modules

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scanner.app.App
import com.example.scanner.ui.base.ScanFragmentBase
import com.example.scanner.ui.dialogs.FileDownload.FileDownloadViewModel
import com.example.scanner.ui.dialogs.IssuanceIssueDialogViewModel
import com.example.scanner.ui.navigation.ComponentFragment
import com.example.scanner.ui.navigation.InvoiceFragment
import com.example.scanner.ui.navigation.HomeFragment
import com.example.scanner.ui.navigation.InvoiceFragmentInfo
import com.example.scanner.ui.navigation.InvoiceFragmentInfoLine
import com.example.scanner.ui.navigation.ComponentFragmentInfo
import com.example.scanner.ui.navigation.InControlFragment
import com.example.scanner.ui.navigation.InControlFragmentInfo
import com.example.scanner.ui.navigation.InvoiceFragmentLines
import com.example.scanner.ui.navigation.IsolatorFragment
import com.example.scanner.ui.navigation.IsolatorFragmentIsolate
import com.example.scanner.ui.navigation.IsolatorFragmentMinus
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation.login.LoginViewModel
import com.example.scanner.ui.navigation.ReceiveFragment
import com.example.scanner.ui.navigation.ReceiveFragmentInfo.ReceiveInfoModelView
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.example.scanner.ui.navigation_setting.LogsFragment.ViewModelLogs
import com.example.scanner.ui.navigation_setting.SettingFragment.ViewModelSetting
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.Serializable

class Other {
    @Suppress("UNCHECKED_CAST")
    fun <T> parseArguments(requireArguments: Bundle, keyParams: String): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments.getSerializable(keyParams, Serializable::class.java) as T
        }else{
            requireArguments.getSerializable(keyParams) as T
        }

    }

    companion object {
        private lateinit var other: Other
        fun getInstanceSingleton(): Other {

            if (!Companion::other.isInitialized)
                other = Other()

            return other
        }
    }

    val mainCoroutineScope= CoroutineScope(Job() + Dispatchers.Main)
    val ioCoroutineScope= CoroutineScope(Job() + Dispatchers.IO)
    val defaultCoroutineScope= CoroutineScope(Job() + Dispatchers.Default)

    val gson= Gson()

    data class SAD<T>(private var _data:T?){//Singe Access Data (SAD)
        var data:T?
            get() {
                val temp=_data
                _data=null
                return temp
            }
        set(value) {
            _data=value
        }
    }

    val picasso= Picasso.get()

}

val viewModelFactory= viewModelFactory {
    initializer {
        HomeFragment.HomeViewModel.getInstanceSingleton((this[APPLICATION_KEY] as App))
    }
    initializer {
        InvoiceFragment.InvoicesViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        ReceiveFragment.ReceiveViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        ViewModelLogs(
            ApiPantes.getInstanceSingleton(),
            LoginRepository.getInstanceSingleton((this[APPLICATION_KEY] as App)))
    }
    initializer {
        LoginViewModel.getInstanceSingleton((this[APPLICATION_KEY] as App))
    }
    initializer {
        ScanFragmentBase.ScanViewModel.getInstanceSingleton(
            Pref.getInstanceSingleton((this[APPLICATION_KEY] as App)))
    }
    initializer {
        ErrorsFragment.ErrorsViewModel.getInstanceSingleton()
    }
    initializer {
        IsolatorFragment.IsolatorViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        ComponentFragment.ComponentsViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        InvoiceFragmentInfo.InvoiceInfoViewModel.getInstanceSingleton((this[APPLICATION_KEY] as App))
    }
    initializer {
        InvoiceFragmentLines.InvoiceLinesViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        ComponentFragmentInfo.ComponentsInfoViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        IssuanceIssueDialogViewModel(
            ApiPantes.getInstanceSingleton(),
            LoginRepository.getInstanceSingleton((this[APPLICATION_KEY] as App))
        )
    }
    initializer {
        IsolatorFragmentIsolate.IsolatorIsolateViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        IsolatorFragmentMinus.IsolatorMinusViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        ReceiveInfoModelView.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        InvoiceFragmentInfoLine.InvoiceLineInfoViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        ViewModelSetting(Pref.getInstanceSingleton((this[APPLICATION_KEY] as App)))
    }
    initializer {
        FileDownloadViewModel(Pref.getInstanceSingleton((this[APPLICATION_KEY] as App)))
    }
    initializer {
        InControlFragment.InControlViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
    initializer {
        InControlFragmentInfo.InControlInfoViewModel.getInstance((this[APPLICATION_KEY] as App))
    }
}
