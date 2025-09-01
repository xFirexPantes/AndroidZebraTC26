package com.example.scanner.models

import java.io.Serializable

data class AcceptInfoResponse(
    val attributes: List<Attribute>,
    val batch: String,
    val coil: Boolean,
    val document: String,
    val element: String,
    val id: String,
    val image: String,
    val markings: List<MarkingItem>,
    val name: String,
    val nominal: String,
    val tabs: List<TabItem>,
): Serializable {

    val attributesToHashMap
        get() = HashMap<Any,Any>().apply {
            attributes.forEach { put(it.name,it.value) }
        }

    data class Attribute(
        val name: String,
        val value: String,
    ): Serializable

    data class TabItem(
        val attributes: List<TabAttribute>,
        val name: String,
    ): Serializable

    data class TabAttribute(
        val name: String,
        val type: String,
    ): Serializable

    data class MarkingItem(
        val label: String,
    ): Serializable

}

