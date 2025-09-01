package com.example.scanner.ui.base

import androidx.lifecycle.ViewModel
import com.example.scanner.modules.Other
import com.example.scanner.ui.MainActivity

open class BaseViewModel: ViewModel() {

    val mainActivityRouter= MainActivity.MainActivityRouter.getInstanceSingleton()
    val mainCoroutineScope=Other.getInstanceSingleton().mainCoroutineScope
    val ioCoroutineScope=Other.getInstanceSingleton().ioCoroutineScope

    fun finalize(){
        "".toString()
    }

    init {
        "".toString()
    }
    
}