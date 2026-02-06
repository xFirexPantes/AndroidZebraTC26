package com.example.scanner.models

data class DrySearchResponse(
    val found:ArrayList<Item> = ArrayList(),
    var last:String = "",
    val total:Long = 0,

) {
    data class Item(
        val id:Int,
        val SkladID: Int,
        val ActionNme:String,
        val Sost: String,
        val kol:Int,
        val DryTmeOst: String,
        val Naim:String,
        val Nom:String,
        val Stel:Int,
        val Yach: String,
        val IDAll:Int,
        val Cab:Int,
        val kolpacks:Int,
        val IDResSub:Int,
        val coils:ArrayList<Coil> = ArrayList(),
        var isScanned: Boolean = false
    )
    data class Coil(
        val type:String,
        val num: Int,
        val ost: Int,
        val Cab: Int,
        val inDry: String,
        val take: Int,
        var isScanned: Boolean = false
    )
}
