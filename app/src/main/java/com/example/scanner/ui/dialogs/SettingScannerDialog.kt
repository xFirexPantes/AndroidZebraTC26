package com.example.scanner.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.databinding.DialogScannerSettingBinding
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragmentDialog
import com.example.scanner.ui.base.ScanFragmentBase

class SettingScannerDialog: BaseFragmentDialog() {

    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels { viewModelFactory }

    private var _binding: DialogScannerSettingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogScannerSettingBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_setting)
            .setTitle(R.string.scanner_setting)
            .setView(binding.root)
            .create().apply {
                setOnShowListener {
                    _binding?.apply {
                        checkBoxEAN8.isChecked = scanViewModel.scannerApiEmulator.checkBoxEAN8
                        checkBoxEAN8.setOnCheckedChangeListener { _, b ->
                            scanViewModel.scannerApiEmulator.checkBoxEAN8 = b
                        }
                        checkBoxEAN13.isChecked = scanViewModel.scannerApiEmulator.checkBoxEAN13
                        checkBoxEAN13.setOnCheckedChangeListener { _, b ->
                            scanViewModel.scannerApiEmulator.checkBoxEAN13 = b
                        }
                        checkBoxCode39.isChecked = scanViewModel.scannerApiEmulator.checkBoxCode39
                        checkBoxCode39.setOnCheckedChangeListener { _, b ->
                            scanViewModel.scannerApiEmulator.checkBoxCode39 = b
                        }
                        checkBoxCode128.isChecked = scanViewModel.scannerApiEmulator.checkBoxCode128
                        checkBoxCode128.setOnCheckedChangeListener { _, b ->
                            scanViewModel.scannerApiEmulator.checkBoxCode128 = b
                        }

                        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            scanViewModel.scannerApiEmulator.friendlyNameList
                        )
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerScannerDevices.adapter = spinnerAdapter
                        spinnerScannerDevices.onItemSelectedListener =
                            object : OnItemSelectedListener {

                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    arg1: View?,
                                    position: Int,
                                    long: Long
                                ) {
                                    scanViewModel.scannerApiEmulator.scannerIndex=position
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                }

                            }
                        // Set default scanner
                        spinnerScannerDevices.setSelection(scanViewModel.scannerApiEmulator.scannerIndex)

                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        // Set selected scanner
        binding.spinnerScannerDevices.setSelection(scanViewModel.scannerApiEmulator.scannerIndex)
    }
}