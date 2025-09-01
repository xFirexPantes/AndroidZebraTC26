package com.example.scanner.ui.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber

class BaseUiMutableLiveData<T>:MutableLiveData<T>() {
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, object : Observer<T>{
            override fun onChanged(value: T) {
                Timber.tag("UI").d("Start:"+value!!::class.simpleName)
                observer.onChanged(value)
                Timber.tag("UI").d("Finish:"+value!!::class.simpleName)
            }

        })
    }
}