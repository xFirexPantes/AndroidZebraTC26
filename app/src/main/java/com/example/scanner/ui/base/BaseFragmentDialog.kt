package com.example.scanner.ui.base


import androidx.fragment.app.DialogFragment
import com.example.scanner.modules.Other


open class BaseFragmentDialog: DialogFragment() {

    fun finalize(){
        "".toString()
    }

    fun <T>getArgument(key: String):T{
        return Other.getInstanceSingleton().parseArguments<T>(requireArguments(),key)
    }

}