package com.example.scanner.models

data class IsolatorListSearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String = "",
    val total:Long = 0,

) {
    data class Item(
        val id:Int,
        val SkladID: Int,
        val kol:Int,
        val Krp: String,
        val Naim:String,
        val Nom:String,
        val IDAll:Int,
        val kolpacks:Int,
        val IDResSub:Int,
        val Reason:String,
        val coils:ArrayList<Coil> = ArrayList(),
        var isScanned: Boolean = false
    )
    data class Coil(
        val type:String,
        val num: Int,
        val ost: Int,
        val inDry: String,
        var isScanned: Boolean = false
    )
}
