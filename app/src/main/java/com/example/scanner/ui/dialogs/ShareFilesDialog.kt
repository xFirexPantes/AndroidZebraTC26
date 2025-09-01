package com.example.scanner.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.app.App
import com.example.scanner.databinding.DialogFilesSelectViewBinding
import com.example.scanner.ui.base.BaseFragmentDialog
import java.io.File

class ShareFilesDialog : BaseFragmentDialog(){

    private val shareFilesDialogViewModel: ShareFilesDialogViewModel by viewModels()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (savedInstanceState==null){
            shareFilesDialogViewModel.clearAdapter()
        }

        val dialogFilesSelectViewBinding=
            DialogFilesSelectViewBinding.inflate(layoutInflater)

        dialogFilesSelectViewBinding.selectAll.setOnClickListener {
            var check=false
            for (i in 0 until dialogFilesSelectViewBinding.filesSelectView.childCount) {
                dialogFilesSelectViewBinding.filesSelectView
                    .let{
                        it.performItemClick(
                            it.getChildAt(i),
                            i,
                            it.getItemIdAtPosition(i)
                        )
                        check=(it.getChildAt(i) as AppCompatCheckedTextView).isChecked
                    }
            }
            dialogFilesSelectViewBinding.selectAll.setImageResource(
                if(check){
                    android.R.drawable.checkbox_off_background
                }
                else{
                    android.R.drawable.checkbox_on_background
                }
            )
        }
        dialogFilesSelectViewBinding.share.setOnClickListener {
            shareFilesDialogViewModel.share(requireContext())
        }

        App.fileLoggerTree?.let { fileLoggerTree->
            dialogFilesSelectViewBinding.filesSelectView.adapter=
                shareFilesDialogViewModel.getAdapter(requireContext(), fileLoggerTree.files)

            dialogFilesSelectViewBinding.filesSelectView.onItemClickListener=
                AdapterView.OnItemClickListener { _, view, position, _ ->
                    (view as CheckedTextView).isChecked=!view.isChecked
                    if(view.isChecked){
                        shareFilesDialogViewModel.add(position)
                    }else{
                        shareFilesDialogViewModel.remove(position)
                    }
                }

        }

        return AlertDialog.Builder(requireContext())
            .setView(dialogFilesSelectViewBinding.root)
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss()}
            .create()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

