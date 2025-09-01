package com.example.scanner.models

data class IssuanceMinusResponse(
    val amount: String,
    val cell: String,
    val coil: Boolean,
    val id: String,
    val isolated: Long,
    val name: String,
    val nominal: String,
    val rack: Long,
)
//{"amount":"2.00","cell":"4.44","coil":false,"id":"14097","isolated":0,"name":"SMD 10V","nominal":"10uF M LowESR","rack":9}