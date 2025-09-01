package com.example.scanner.models

import java.io.Serializable

data class LinesSearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String="",
    var total:Int=0,
    var collected: Long=0,

    ) {
    data class Item(
        val amount:Double,
        val cell:String,
        val coil:Boolean,
        var collected:Boolean,
        val id:Int,
        val name:String,
        val nominal:String,
        val quantity:Double,
        val rack:Int,
        val separate:Boolean,
        val number:String,
        var isDirty: Double=0.0,

        ):Serializable
}
