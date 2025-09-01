package com.example.scanner.modules

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.example.scanner.ui.dialogs.ManualSearchDialog

class ScannerApiEmulator:IScannerApi {
    override fun softScan(fragmentManager: FragmentManager, context: Context, isTypeInputEnable: Boolean) {
        ManualSearchDialog(isTypeInputEnable)
            .show(
                fragmentManager,
                ManualSearchDialog::class.java.name)

    }

    override fun resume() {
        
    }

    override fun pause() {
        
    }

    override suspend fun start(requireContext: Context) {

    }

    override fun finish() {

    }

    override var scannerIndex: Int=0
    override val friendlyNameList: ArrayList<String> = ArrayList()
    override var checkBoxCode128: Boolean=true
    override var checkBoxCode39: Boolean=true
    override var checkBoxEAN13: Boolean=true
    override var checkBoxEAN8: Boolean=true
    override var onScanningResult:((scannedData: String?, isTypeInputScanner: Boolean)->Unit)?=null
    override val isScannerReadyToScanning: Boolean=true
}