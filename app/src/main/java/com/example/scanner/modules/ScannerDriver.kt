package com.example.scanner.modules

import android.content.Context

interface ScannerDriver {
    fun init(context: Context, callback: ScannerDriverCallback): Boolean
    fun startScan()
    fun stopScan()
    fun release()
    fun setConfig(config: ScannerConfig)
}

interface ScannerDriverCallback {
    fun onDataReceived(data: String)
    fun onStateChanged(state: ScannerState)
    fun onError(throwable: Throwable)
}

data class ScannerConfig(
    var ean8Enabled: Boolean = true,
    var ean13Enabled: Boolean = true,
    var code39Enabled: Boolean = true,
    var code128Enabled: Boolean = true
)

enum class ScannerState {
    IDLE, SCANNING, DISABLED, ERROR
}