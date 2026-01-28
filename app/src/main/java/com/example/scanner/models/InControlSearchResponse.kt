package com.example.scanner.models

data class InControlSearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String = "",
    val total:Long = 0
) {
    data class Item(
        val amount:Double,
        val id:String,
        val name: String,
        val nominal:String,
        val cell: String,
        val rack:Int,
        val coil: Boolean,
        val DT:String,
        val IDAll:Int,
        val inBox:Boolean,
        val NumNakl: String,
        val kolpacks:Int,
    )
}
