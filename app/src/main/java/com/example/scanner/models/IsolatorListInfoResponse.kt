package com.example.scanner.models

import java.io.Serializable

data class IsolatorListInfoResponse(
    val coils: List<Attribute>,
    val head: Head,
):Serializable
{
    data class Attribute(
        val number: Int,
        val numNakl: String,
        val NaklDT: String,
        val msg: String = ""
    ):Serializable

    data class Head(
        val skladid: String,
        val naim: String,
        val el: String,
        val ser: String,
        val nom: String,
        val krp: String,
    ):Serializable

}
