package com.example.scanner.modules

import androidx.fragment.app.FragmentManager
import com.example.scanner.app.App
import com.example.scanner.ui.base.BarcodeScanner


class ZebraScannerAdapter(
    private val scannerApi: ScannerApi
) : BarcodeScanner {

    override suspend fun init(): Boolean {
        return try {
            scannerApi.start(App.instance.applicationContext)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun startScan(
        callback: (String, Boolean) -> Unit,
        fragmentManager: FragmentManager  // Получаем извне
    ): Boolean {
        // Устанавливаем callback для результата
        scannerApi.onScanningResult = { scannedData, isTypeInputScanner ->
            callback(scannedData ?: "", isTypeInputScanner)
        }

        // Передаем fragmentManager в softScan
        scannerApi.softScan(fragmentManager, App.instance.applicationContext, false)
        return true
    }

    override fun stopScan(): Boolean {
        scannerApi.pause()
        return true
    }

    override fun release() {
        scannerApi.finish()
    }
}