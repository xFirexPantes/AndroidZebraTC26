package com.example.scanner.models

import java.io.Serializable

data class InvoiceSearchResponse(
    val found:ArrayList<Item> =ArrayList(),
    var request:String?=null,
    var last:String = ""
) {
    data class Item(val id:String, val name:String, val number:String, val partial:Boolean,var collected: Boolean):Serializable

}
