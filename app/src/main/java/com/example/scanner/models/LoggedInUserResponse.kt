package com.example.scanner.models

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */

data class LoggedInUserResponse(
    var name: String?=null,
    var token: String?=null,
    val access:Access
){

    data class Access(
        val isolator:Boolean=false,
        val issuance:Boolean=false,
        val search:Boolean=false,
        val accept:Boolean=false,
        val incontrol: Boolean=false,
        val token:String
        )
}

