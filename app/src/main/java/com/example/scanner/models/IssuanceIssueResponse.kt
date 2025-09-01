package com.example.scanner.models

import java.io.Serializable

data class IssuanceIssueResponse(
    val collected: Long,
    val invoice: Invoice,
    val line: Line?,
): Serializable{

    data class Invoice(
        val client: String,
        val collected: Boolean,
        val date: String,
        val name: String,
        val note: String,
        val number: String,
        val owner: String,
        val partial: Boolean,
    ): Serializable

    data class Line(
        val amount: String,
        val attributes: List<Attribute>,
        val cell: String,
        val coil: Boolean,
        val coils: List<Coil>,
        val collected: Boolean,
        val comment: String,
        val document: String,
        val id: String,
        val elevator: Boolean,
        val markings: List<Any?>,
        val name: String,
        val nominal: String,
        val note: String,
        val number: String?,
        val quantity: String,
        val rack: Long,
        val separate: Boolean,
    ): Serializable

    data class Attribute(
        val name: String,
        val value: String,
    ): Serializable

    data class Coil(
        val amount: Long,
        val collected: Boolean,
        val number: Long,
        val quantity: Long,
    ): Serializable

}

