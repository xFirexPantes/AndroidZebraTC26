package com.example.scanner.models

data class IsolatorIsolateResponse(
    val amount:String,
    val cell:String,
    val coil:Boolean,
    val id:String,
    val isolated:Int,
    val name:String,
    val nominal:String,
    val rack:Int
)

//{"amount":"3.00",
// "cell":"4.44",
// "coil":false,
// "id":"14097",
// "isolated":0,
// "name":"SMD 10V","nominal":"10uF M LowESR","rack":9}