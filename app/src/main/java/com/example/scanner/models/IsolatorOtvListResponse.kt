package com.example.scanner.models

import java.io.Serializable

data class IsolatorOtvListResponse(
    val list: List<Attribute>,
):Serializable
{
    data class Attribute(
        val name: String,
        val id: Int,
    ):Serializable

}
