package com.example.scanner.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.example.scanner.app.App
import com.example.scanner.R
import com.example.scanner.ui.base.BaseFragmentDialog
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation_setting.LogsFragment

class ClearLogsFilesDialog : BaseFragmentDialog() {
    private val baseViewModel: BaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setPositiveButton(android.R.string.ok,{_,_->
                App.fileLoggerTree?.clear()
                App.fileLoggerTree=null
                App.plantFileLoggerTree(requireContext().externalCacheDir?.canonicalPath)
                baseViewModel.mainActivityRouter.navigate(LogsFragment::class.java)

            })
            .setMessage("Очистить лог и закрыть ${requireContext().resources.getString(R.string.app_name)}?")
            .create()

    }
}