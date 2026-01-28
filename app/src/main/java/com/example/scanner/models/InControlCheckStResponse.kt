package com.example.scanner.models

import java.io.Serializable

data class InControlCheckStResponse(
    val isOk: String? = "",
    val IDAll: String? = "",
    val action15: Boolean? = false,
    val action23: Boolean? = false,

): Serializable{

}
