package com.example.scanner.ui.dialogs

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.example.scanner.BuildConfig
import com.example.scanner.R
import com.example.scanner.app.isValidAction
import com.example.scanner.app.softInput
import com.example.scanner.databinding.DialogManualInputScanDataBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragmentDialog
import com.example.scanner.ui.base.ScanFragmentBase
import com.google.android.material.textview.MaterialTextView

class ManualSearchDialog(private val emulateScannerInput: Boolean=false) :BaseFragmentDialog(),
    TextView.OnEditorActionListener {
    private val scanViewModel: ScanFragmentBase.ScanViewModel by viewModels{ viewModelFactory  }
    lateinit var binding:DialogManualInputScanDataBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding=DialogManualInputScanDataBinding.inflate(inflater,container,false)
            .apply {
                toolbar.title="Ручной поиск"
                toolbar.subtitle="LongClick - удалить из истроии"
                iconContainer.addView(
                    TemplateIconBinding.inflate(inflater,iconContainer,false)
                        .apply {
                            src=ResourcesCompat.getDrawable(resources,R.drawable.ic_clear_all,null)
                            image.setOnClickListener{
                                dismiss()
                            }
                        }
                        .root
                )

                containerRadio.visibility=
                    if (emulateScannerInput)
                        View.VISIBLE
                    else
                        View.GONE

                historyList.apply {
                        inputText.setOnEditorActionListener(this@ManualSearchDialog)
                        inputText.setOnFocusChangeListener { v, hasFocus ->
                            view?.context?.softInput(v,hasFocus)
                        }
                    inputTextContainer.setEndIconOnClickListener{
                        inputText.text=null
                    }
                        if (scanViewModel.pref.manualInputHistory.isEmpty()){

                            if (BuildConfig.DEBUG) {
                                val tmp = scanViewModel.pref.manualInputHistory
                                tmp.add("01")
                                tmp.add("93880-0")
                                tmp.add("11311")
                                tmp.add("r2010")
                                scanViewModel.pref.manualInputHistory =
                                    tmp
                            }
                        }

                        val arrayList=
                            ArrayList<String>(scanViewModel.pref.manualInputHistory)
                        adapter =
                            ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_list_item_1,
                                arrayList
                            )
                        setOnItemClickListener { _, v, _, _ ->
                            scanViewModel.scannerApiEmulator.onScanningResult?.invoke(
                                (v as MaterialTextView).text.toString(),
                                scannerInput.isChecked)
//                            scanViewModel.scanFragmentBaseFormState.postValue(
//                                ScanFragmentBase.ScanFragmentBaseFormState.ShowScanResult((v as MaterialTextView).text.toString())
//                            )
                            dismiss()
                        }

                    setOnItemLongClickListener { parent, view, position, id ->
                        arrayList.removeAt(position)
                        scanViewModel.pref.manualInputHistory=arrayList
                        adapter =
                            ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_list_item_1,
                                arrayList
                            )

                        true
                    }

                    }

                }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            view.context?.softInput(binding.inputText,false)
        }
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        if (isValidAction(p1,p2)){
            scanViewModel.pref.manualInputHistory.let {
                if (!binding.inputText.text.isNullOrBlank()){
                    scanViewModel.scannerApiEmulator.onScanningResult?.invoke(
                        binding.inputText.text.toString(),
                        binding.scannerInput.isChecked)
                    dismiss()
                }
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
    }
}