package com.example.scanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.example.scanner.R
import com.example.scanner.modules.Other
import kotlinx.coroutines.launch

class SplashActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Other.getInstanceSingleton().mainCoroutineScope.launch {
            Thread.sleep(1000)
            startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            finish()
        }

    }
}