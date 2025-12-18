package com.example.scanner.models

data class AcceptSearchResponse(
    val batch: String,
    val case: String,
    val element: String,
    val id: String,
    val name: String,
    val nominal: String,
    val stel: String,
    val cell: String,
    val coil: Boolean
)