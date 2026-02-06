package com.example.scanner.ui.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<T>(var data: T): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected abstract fun getCallback(dataOld:T?):DiffUtil.Callback

    var isResetContent: Boolean=true
        private set


    protected abstract fun appendData(dataNew: T)

    val last
        get() = getLastId()


    protected abstract fun getLastId(): Any

    protected abstract fun cloneData():T

    fun resetContent(){
        isResetContent=true
    }

    open fun setContent(dataNew:T){
        isResetContent=false
        val dataOld=cloneData()
        data=dataNew
        DiffUtil.calculateDiff(getCallback(dataOld),true).dispatchUpdatesTo(this)

    }


    fun appendContent(dataAppend: T) {
        val dataOld= cloneData()
        appendData(dataAppend)
        DiffUtil.calculateDiff(getCallback(dataOld),true).dispatchUpdatesTo(this)
        isResetContent=false
    }

}
