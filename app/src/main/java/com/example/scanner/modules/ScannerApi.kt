package com.example.scanner.modules

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.example.scanner.ui.base.NonFatalExceptionShowToaste
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKManager.FEATURE_TYPE
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.BarcodeManager.ScannerConnectionListener
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerInfo
import com.symbol.emdk.barcode.ScannerResults
import com.symbol.emdk.barcode.StatusData
import com.symbol.emdk.barcode.StatusData.ScannerStates

class ScannerApi:
    EMDKListener,
    Scanner.DataListener,
    Scanner.StatusListener,
    ScannerConnectionListener,
    IScannerApi
{
    private var emdkManager:EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var deviceList: List<ScannerInfo>? = null
    private var statusString = ""
    private var isResumed=false
    private var bDecoderSettingsChanged = false
    private val lock = Any()
    private var scanner: Scanner? = null
    private var bExtScannerDisconnected = false
    private var bSoftTriggerSelected = false
    var exceptionScanner=
        NonFatalExceptionShowToaste("Ошибка сканера!")

    override val friendlyNameList=ArrayList<String>()
    override var scannerIndex = 0  // Keep the selected scanner
        set(value) {
            if ((scannerIndex != value) || (scanner == null)) {
                field = value
                bSoftTriggerSelected = false
                bExtScannerDisconnected = false
                deInitScanner()
                initScanner()
                onChangeScannerIndex?.invoke(value)
            }
        }
    @Suppress("MemberVisibilityCanBePrivate")
    var updateStatus:((status: String)->Unit)?=null
    override var onScanningResult:((scannedData: String?, isTypeInputScanner: Boolean)->Unit)?=null
    var onChangeScannerIndex:((newIndex:Int)->Unit)?=null
    var onScannerException:((e:Exception?)->Unit)?=null
    var onScannerReadyToScanning:(()->Unit)?=null
    var onScanningProgress:(()->Unit)?=null
    override var checkBoxEAN8: Boolean=true
    override var checkBoxEAN13: Boolean=true
    override var checkBoxCode39: Boolean=true
    override var checkBoxCode128: Boolean=true
    private var statusData: StatusData?=null
    override val isScannerReadyToScanning
        get() = statusData?.state != null && statusData?.state== ScannerStates.WAITING


    override fun onOpened(emdkManager: EMDKManager?) {
        this.emdkManager = emdkManager
        this.emdkManager
            ?.let {
                initBarcodeManager()
                // Enumerate scanner devices
                enumerateScannerDevices()
                if (scanner==null && isResumed){
                    resume()
                }

            }
            ?:run {
                onScannerException?.invoke(Exception("emdkManager is null"))
            }
    }
    override fun onClosed() {
        // Release all the resources
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager = null
        }
        updateStatus?.invoke("EMDK closed unexpectedly! Please close and restart the application.")
    }
    override fun onData(scanDataCollection: ScanDataCollection?) {
        if ((scanDataCollection != null) && (scanDataCollection.result == ScannerResults.SUCCESS)) {
            val scanData: java.util.ArrayList<ScanDataCollection.ScanData> =
                scanDataCollection.scanData
            for (data in scanData) {
                    onScanningResult?.invoke(data.data,true)
                    //onScanningResult?.invoke("N135807$93880-0")
            }
        }
    }
    override fun onStatus(statusData: StatusData?) {
        this.statusData=statusData
        statusData?.let {
            val state: ScannerStates = statusData.state
            //Timber.tag("ScannerState").d("$state")
            when (state) {
                ScannerStates.IDLE -> {

                    statusString = statusData.friendlyName + " is enabled and idle..."
                    updateStatus?.invoke(statusString)
                    // set trigger type
                    if (bSoftTriggerSelected) {
                        scanner!!.triggerType = Scanner.TriggerType.SOFT_ONCE
                        bSoftTriggerSelected = false
                    } else {
                        scanner!!.triggerType = Scanner.TriggerType.HARD
                    }
                    // set decoders
                    if (bDecoderSettingsChanged) {
                        setDecoders()
                        bDecoderSettingsChanged = false
                    }
                    // submit read
                    if (!scanner!!.isReadPending && !bExtScannerDisconnected) {
                        try {
                            scanner!!.read()
                        } catch (e: ScannerException) {
                            onScannerException?.invoke(e)
                        }
                    } else {

                    }
                }

                ScannerStates.WAITING -> {
                    onScannerReadyToScanning?.invoke()
                    statusString = "Scanner is waiting for trigger press..."
                    updateStatus?.invoke(statusString)
                }

                ScannerStates.SCANNING -> {
                    onScanningProgress?.invoke()
                    statusString = "Scanning..."
                    updateStatus?.invoke(statusString)
                }

                ScannerStates.DISABLED -> {
                    statusString = statusData.friendlyName + " is disabled."
                    updateStatus?.invoke(statusString)
                }

                ScannerStates.ERROR -> {
                    statusString = "An error has occurred."
                    updateStatus?.invoke(statusString)
                }

                else -> {}
            }

        }
    }
    override fun onConnectionChange(scannerInfo: ScannerInfo?, connectionState: BarcodeManager.ConnectionState?) {
        val status: String
        var scannerName = ""
        val statusExtScanner: String = connectionState.toString()
        val scannerNameExtScanner: String? = scannerInfo?.friendlyName
        if (deviceList!!.isNotEmpty()) {
            scannerName = deviceList!![scannerIndex].friendlyName
        }
        if (scannerName.equals(scannerNameExtScanner, ignoreCase = true)) {
            when (connectionState) {
                BarcodeManager.ConnectionState.CONNECTED -> {
                    bSoftTriggerSelected = false
                    synchronized(lock) {
                        initScanner()
                        bExtScannerDisconnected = false
                    }
                }

                BarcodeManager.ConnectionState.DISCONNECTED -> {
                    bExtScannerDisconnected = true
                    synchronized(lock) {
                        deInitScanner()
                    }
                }

                else->{

                }
            }
            status = "$scannerNameExtScanner:$statusExtScanner"
            updateStatus?.invoke(status)
        } else {
            bExtScannerDisconnected = false
            status = "$statusString $scannerNameExtScanner:$statusExtScanner"
            updateStatus?.invoke(status)
        }
    }

    private fun initBarcodeManager() {
        barcodeManager = emdkManager!!.getInstance(FEATURE_TYPE.BARCODE) as BarcodeManager
        // Add connection listener
        if (barcodeManager != null) {
            barcodeManager!!.addConnectionListener(this)
        }
    }
    private fun enumerateScannerDevices() {
        if (barcodeManager != null) {

            var spinnerIndex = 0
            deviceList = barcodeManager!!.supportedDevicesInfo
            if ((deviceList != null) && (deviceList?.size != 0)) {
                val it: Iterator<ScannerInfo> = deviceList!!.iterator()
                while (it.hasNext()) {
                    val scnInfo = it.next()
                    friendlyNameList.add(scnInfo.friendlyName)
                    if (scnInfo.isDefaultScanner) {
                        this.scannerIndex = spinnerIndex
                    }
                    ++spinnerIndex
                }
            } else {
                friendlyNameList.clear()
                updateStatus?.invoke("Failed to get the list of supported scanner devices! Please close and restart the application.")
            }
        }
    }
    private fun setDecoders() {
        if (scanner != null) {
            try {
                val config = scanner!!.config
                // Set EAN8
                config.decoderParams.ean8.enabled = checkBoxEAN8
                // Set EAN13
                config.decoderParams.ean13.enabled = checkBoxEAN13
                // Set Code39
                config.decoderParams.code39.enabled = checkBoxCode39
                //Set Code128
                config.decoderParams.code128.enabled = checkBoxCode128
                scanner!!.config = config
            } catch (e: ScannerException) {
                onScannerException?.invoke(e)
            }
        }
    }
    private fun deInitBarcodeManager() {
        if (emdkManager != null) {
            emdkManager!!.release(FEATURE_TYPE.BARCODE)
        }
    }
    private fun initScanner() {
        if (scanner == null) {
            if ((deviceList != null) && (deviceList!!.isNotEmpty())) {
                if (barcodeManager != null) {
                    scanner =
                        barcodeManager!!.getDevice(deviceList!![scannerIndex])
                }
            } else {
                updateStatus?.invoke("Failed to get the specified scanner device! Please close and restart the application.")
                return
            }
            if (scanner != null) {
                scanner!!.addDataListener(this)
                scanner!!.addStatusListener(this)
                try {
                    scanner!!.enable()
                } catch (e: ScannerException) {
                    onScannerException?.invoke(e)
                    deInitScanner()
                }
            } else {
                updateStatus?.invoke("Failed to initialize the scanner device.")
            }
        }
    }
    private fun deInitScanner() {
        if (scanner != null) {
            try {
                scanner!!.disable()
            } catch (e: java.lang.Exception) {
                onScannerException?.invoke(e)
            }

            try {
                scanner!!.removeDataListener(this)
                scanner!!.removeStatusListener(this)
            } catch (e: java.lang.Exception) {
                onScannerException?.invoke(e)
            }

            try {
                scanner!!.release()
            } catch (e: java.lang.Exception) {
                onScannerException?.invoke(e)
            }
            scanner = null
            isResumed=false
        }
    }
    private fun cancelRead() {
        if (scanner != null) {
            if (scanner!!.isReadPending) {
                try {
                    scanner!!.cancelRead()
                } catch (e: ScannerException) {
                    onScannerException?.invoke(e)
                }
            }
        }
    }

    override suspend fun start(requireContext: Context) {
        try {
            val results = EMDKManager.getEMDKManager(requireContext, this)
            if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                onScannerException?.invoke(Exception("EMDKManager object request failed!"))
                updateStatus?.invoke("EMDKManager object request failed!")
            }
        }catch (e:Exception){
            onScannerException?.invoke(e)
        }

    }
    override fun resume() {
        isResumed=true
        if (emdkManager != null) {
            // Acquire the barcode manager resources
            initBarcodeManager()
            // Enumerate scanner devices
            enumerateScannerDevices()
            // Initialize scanner
            deInitScanner()
            initScanner()
        }
    }
    override fun pause() {
        isResumed=false
        // The application is in background
        // Release the barcode manager resources
        deInitScanner()
        deInitBarcodeManager()
    }
    override fun finish() {
        // Release all the resources
        scanner = null
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager = null
        }

    }
    override fun softScan(fragmentManager: FragmentManager, context: Context, isTypeInputEnable: Boolean) {
        if (scanner==null){
            onScannerException?.invoke(Exception("Scanner is NULL"))
        }else{
            bSoftTriggerSelected = true
            cancelRead()
        }
    }

init {
    "".toString()
}
}