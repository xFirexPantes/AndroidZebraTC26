package com.example.scanner.ui.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.Pref

open class BaseActivity:AppCompatActivity() {
    private val apiPantes=ApiPantes.getInstanceSingleton()

    override fun onCreate(savedInstanceState: Bundle?) {

        apiPantes.installApi(Pref.getInstanceSingleton(this).pantesServerName)

        super.onCreate(savedInstanceState)
    }
}