package com.example.scanner.models

import java.io.Serializable

data class IsolatorListInfoResponse(
    val coils: List<Attribute>,
):Serializable
{
    data class Attribute(
        val type: String,
        val number: Int,
        val ost: Int,
        val Cab: Int,
        val inDry: String,
        val take: Int
    ):Serializable


}
