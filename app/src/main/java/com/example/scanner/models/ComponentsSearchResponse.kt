package com.example.scanner.models

data class ComponentsSearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String = "",
    val total:Long = 0
) {
    data class Item(
        val amount:Double,
        val cell:String,
        val case:String,
        val coil:Boolean,
        val id:String,
        val name: String,
        val nominal:String,
        val rack:Int
    )
}
