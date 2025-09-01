package com.example.scanner.models

data class InvoiceInfoResponse(
    val client:String?=null,
    val date:String?=null,
    val name:String?=null,
    val note:String?=null,
    val number: String?=null,
    val owner:String?=null,
    val partial:Boolean?=null
) {
}
//{"client":"861","date":"20.05.2024","name":"Тестовая накладная 2","note":"На СИ","number":"93880-0","owner":"Литвинов Артем","partial":false}