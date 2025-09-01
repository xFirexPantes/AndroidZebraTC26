package com.example.scanner.models

import com.example.scanner.modules.Other
import java.io.Serializable

data class IsolatorSearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String = "",
    val total:Long = 0

): Serializable {
    data class Item(
        var amount:Double,
        var cell:String,
        var coil:Boolean,
        val id:Long,
        var isolated:Long,
        var name:String,
        var nominal:String,
        var rack:Long
    ): Serializable

    override fun toString(): String {
        return Other.getInstanceSingleton().gson.toJson(this)
    }
}