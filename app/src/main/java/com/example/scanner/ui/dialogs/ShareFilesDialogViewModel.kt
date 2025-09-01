package com.example.scanner.ui.dialogs

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.core.content.FileProvider
import com.example.scanner.ui.base.BaseViewModel
import java.io.File

class ShareFilesDialogViewModel: BaseViewModel() {
    private val listUris = ArrayList<Uri>()
    private val arrayListSelectedFiles=ArrayList<File>()
    private var arrayAllFiles:Array<File>?=null
    private var  arrayAdapter: ArrayAdapter<String>?=null

    fun getAdapter(requireContext: Context, files:Collection<File>): ArrayAdapter<String>? {

        if(arrayAdapter==null){
            ArrayList(files).let { listFiles->
                arrayAllFiles=Array(listFiles.size,{pos-> listFiles[(listFiles.size-1)-pos]})
                arrayAdapter = object : ArrayAdapter<String>(
                    requireContext,
                    android.R.layout.simple_list_item_checked,
                    Array<String>(arrayAllFiles!!.size, { p -> arrayAllFiles!![p].name })
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {

                        val view=super.getView(position, convertView, parent)
                        (view as CheckedTextView).isChecked=
                            (arrayListSelectedFiles.contains(arrayAllFiles!![position]))

                        return view
                    }
                }

            }
        }


        return arrayAdapter

    }

    fun add(position: Int) {
        arrayListSelectedFiles.add(
            arrayAllFiles!![position])
    }

    fun remove(position: Int) {
        arrayListSelectedFiles.remove(
            arrayAllFiles!![position])

    }

    fun share(requireContext: Context) {
        arrayListSelectedFiles.forEach { file ->
            listUris.add(
                FileProvider.getUriForFile(
                    requireContext,
                    requireContext.packageName+".fileprovider",
                    file
                )
            )
        }

        val intentShareFile = Intent(Intent.ACTION_SEND_MULTIPLE)
        intentShareFile.setType("*/*")
        intentShareFile.putExtra(Intent.EXTRA_STREAM, listUris)
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT,"Sharing logs...")
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing logs...")

        val chooser= Intent.createChooser(intentShareFile,"Share logs")
        val resInfoList: List<ResolveInfo> =
            requireContext.packageManager
                .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            listUris.forEach { f->
                requireContext.grantUriPermission(
                    packageName,
                    f,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        requireContext.startActivity(chooser)

    }

    fun clearAdapter() {
        arrayAdapter=null
    }

}