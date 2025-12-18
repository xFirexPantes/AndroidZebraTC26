package com.example.scanner.models

data class AcceptScanResponse(
    val found: ArrayList<FoundItem> = ArrayList(),
    var last: String = "",
    val total: Long =0,
){

}
    data class FoundItem(
        val batch: String,
        val case: String,
        val element: String,
        val id: String,
        val name: String,
        val nominal: String,
        val stel: String,
        val cell: String,
    )

