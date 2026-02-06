package com.example.scanner.ui.base

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.example.scanner.R
import com.example.scanner.modules.IScannerApi
import com.example.scanner.modules.Other
import com.example.scanner.modules.Pref
import com.example.scanner.modules.ScannerApi
import com.example.scanner.modules.ScannerApiEmulator
import com.example.scanner.ui.navigation_over.ProgressFragment
import com.example.scanner.ui.navigation_over.ErrorsFragment
import com.example.scanner.ui.navigation_over.TransparentFragment
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

abstract class ScanFragmentBase : BaseFragment(){
    sealed class ScanFragmentBaseFormState<out T : Any> {
        data class Error(val exception: Throwable?) : ScanFragmentBaseFormState<Nothing>()

        data class ShowScanResult(
            private var _stringScanResult: String?,
            val isTypeInputScanner: Boolean, // true = QR/ручной ввод, false = NFC
            val sourceType: SourceType = SourceType.QR // доп. поле для гибкости
        ) : ScanFragmentBaseFormState<Nothing>() {

            val stringScanResult: String?
                get() {
                    val result = _stringScanResult
                    _stringScanResult = null
                    return result
                }
        }

        enum class SourceType {
            QR,
            NFC,
            MANUAL
        }
    }

        class ScanViewModel(
            val pref: Pref
        ) : BaseViewModel()
        {

            companion object {
                private var _ScanViewModel: WeakReference<ScanViewModel>? = null
                fun getInstanceSingleton(pref: Pref): ScanViewModel {
                    return _ScanViewModel
                        ?.get()
                        ?:run {
                            getNewInstanceSingleton(pref)
                                .apply { _ScanViewModel = WeakReference(this) }
                        }
                }
                private fun getNewInstanceSingleton(
                    pref: Pref
                ): ScanViewModel {
                    return ScanViewModel(pref)
                }
            }

            val scanFragmentBaseFormState=
                MutableLiveData<ScanFragmentBaseFormState<*>>()

            lateinit var scannerApiEmulator: IScannerApi
            lateinit var scannerApi: ScannerApi


            fun installScannerApi() {

                scannerApiEmulator=ScannerApiEmulator()
                    .apply {
                        onScanningResult= { resultScanData ,srcScanner->
                            //Timber.tag("scannerApi").d("onScanningResult")
                            Other.getInstanceSingleton().mainCoroutineScope.launch {
                                scanFragmentBaseFormState.value =
                                    ScanFragmentBaseFormState.ShowScanResult(resultScanData,srcScanner)
                                storeHistory(resultScanData)
                            }
                        }
                    }
                //Timber.tag("scannerApi").d("onScannerInstall")
                scannerApi=ScannerApi()
                    .apply {
                        scannerIndex=pref.scannerIndex
                        checkBoxEAN8=pref.checkBoxEAN8
                        checkBoxEAN13=pref.checkBoxEAN13
                        checkBoxCode128=pref.checkBoxCode128
                        checkBoxCode39=pref.checkBoxCode39
                        onScanningResult= { resultScanData , srcScanner->
                            Other.getInstanceSingleton().mainCoroutineScope.launch {
                                scanFragmentBaseFormState.value =
                                    ScanFragmentBaseFormState.ShowScanResult(resultScanData,srcScanner)
                                storeHistory(resultScanData)
                            }
                        }
                        onChangeScannerIndex={newIndex ->
                            pref.scannerIndex=newIndex
                        }
                        onScannerException={ e ->
                            when(pref.scannerIconDrawableId.value){
                                R.drawable.ic_qr->{
                                    Timber.tag("scannerApi").d("onScannerException: $e")
                                    mainActivityRouter.navigate(
                                        ErrorsFragment::class.java,
                                        Bundle().apply { putSerializable(ErrorsFragment.PARAM,exceptionScanner) }
                                    )
                                    pref.scannerIconDrawableId.postValue(R.drawable.ic_qr_error)
                                }
                            }
                        }
                        onScannerReadyToScanning= {
                            //Timber.tag("scannerApi").d("onScannerReadyToScanning")
                            Other.getInstanceSingleton().mainCoroutineScope.launch {
                                mainActivityRouter.navigate(
                                    TransparentFragment::class.java)

                            }
                        }
                        onScanningProgress= {
                            //Timber.tag("scannerApi").d("onScanningProgress")
                            Other.getInstanceSingleton().mainCoroutineScope.launch {
                                mainActivityRouter.navigate(
                                    ProgressFragment::class.java)
                            }
                        }
                    }
            }

            private fun storeHistory(resultScanData: Any?){
                resultScanData?.let {
                    pref.manualInputHistory=
                        pref.manualInputHistory
                            .apply {

                                while (contains(resultScanData.toString()) && isNotEmpty())
                                    remove(resultScanData.toString())

                                add(0,resultScanData.toString())
                            }

                }

            }


        }

    }