package com.example.scanner.models

data class TrueSignSearchResponse(
    val customer: String ="",
    val numNakl: String ="",
    var dt:String = "",
    val kol:Long = 0,
    val msg:String = "",
)
