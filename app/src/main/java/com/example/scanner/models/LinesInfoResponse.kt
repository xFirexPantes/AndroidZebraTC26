package com.example.scanner.models

import java.io.Serializable

data class LinesInfoResponse(
    val amount:Double,
    val attributes:ArrayList<AttributesItem>,
    val cell:String,
    val coil:Boolean,
    val coils:ArrayList<CoilsItem>,
    val collected:Boolean,
    val document:String,
    val elevator:Boolean,
    val markings:ArrayList<MarkingsItem>,
    val name:String,
    val nominal:Any,
    var comment:String,
    val number:String,
    val quantity:Double,
    val rack:Int,
    val separate:Boolean
): Serializable {
    data class AttributesItem(
        val name:String,
        val value:String
    ): Serializable
    data class CoilsItem(val amount: Int,var collected:Boolean,val number:Int,val quantity:Double,
                         val isused:Boolean = false){
        override fun toString(): String {
            return StringBuilder()
                .append(number)
                .append(" Колич.")
                .append(quantity)
                .toString()
        }
    }
    data class MarkingsItem(val label: String): Serializable
}
