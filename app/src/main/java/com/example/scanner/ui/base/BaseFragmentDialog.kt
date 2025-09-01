package com.example.scanner.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.scanner.modules.Other
import timber.log.Timber

open class BaseFragmentDialog: DialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Timber.tag("onViewCreated").i("$this")
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroy() {
        //Timber.tag("onDestroy").i("$this")
        super.onDestroy()
    }
    fun finalize(){
        "".toString()
    }

    fun <T>getArgument(key: String):T{
        return Other.getInstanceSingleton().parseArguments<T>(requireArguments(),key)
    }

}