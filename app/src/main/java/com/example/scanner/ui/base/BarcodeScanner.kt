package com.example.scanner.ui.base

import androidx.fragment.app.FragmentManager


interface BarcodeScanner {
    suspend fun init(): Boolean
    fun startScan(
        onScanResult: (String, Boolean) -> Unit,  // ← имя параметра
        fragmentManager: FragmentManager
    ): Boolean
    fun stopScan(): Boolean
    fun release()
}