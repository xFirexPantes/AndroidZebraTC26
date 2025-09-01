package com.example.scanner.modules

import android.content.Context
import androidx.fragment.app.FragmentManager

interface IScannerApi {
    fun softScan(fragmentManager: FragmentManager, context: Context, isTypeInputEnable: Boolean=false)

    fun resume()

    fun pause()

    suspend fun start(requireContext: Context)

    fun finish()

    var scannerIndex: Int
    val friendlyNameList: ArrayList<String>
    var checkBoxCode128: Boolean
    var checkBoxCode39: Boolean
    var checkBoxEAN13: Boolean
    var checkBoxEAN8: Boolean
    var onScanningResult:((scannedData: String?, isTypeInputScanner: Boolean)->Unit)?
    val isScannerReadyToScanning: Boolean

}