package com.example.scanner.models

data class InControlUrgentSearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String = "",
    val total:Long = 0
) {
    data class Item(
        val amount:Double,
        val id:String,
        val name: String,
        val nominal:String,
    )
}
