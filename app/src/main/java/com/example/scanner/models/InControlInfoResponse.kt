package com.example.scanner.models

import java.io.Serializable

data class InControlInfoResponse(
    val amount: Double,
    val attributes: List<Attribute>,
    val cell: String,
    val coil: Boolean,
    val document: String,
    val id: String,
    val image: String,
    val images     : ArrayList<Images> = arrayListOf(),
    val markings: List<Marking>,
    val name: String,
    val nominal: String,
    val rack: Long,
    val tabs: List<Tab>,
    val quantity:String?,
    val separate: Boolean?,
    val isokol: Double,
    val drykol: Double,
    val coilslist: List<Coil>,
    val packslist: List<Coil>,

):Serializable
{
    val attributesToHashMap
        get() = HashMap<Any,Any>().apply {
            attributes.forEach { put(it.name,it.value) }
        }
    data class Attribute(
        val name: String,
        val value: String,
    ):Serializable

    data class Marking(
        val label: String,
    ):Serializable
    data class Coil(
        val label: String,
        val ost: String,
        val kol: String,
    ):Serializable
     data class Tab(
        val attributes: List<TabAttribute>,
        val name: String,
    ):Serializable

    data class TabAttribute(
        val name: String,
        val type: String,
    ):Serializable

    data class Images (

        var url : String? = null

    ):Serializable

}
