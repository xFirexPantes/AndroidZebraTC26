package com.example.scanner.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.scanner.modules.Other
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragmentDialog
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.navigation_over.ErrorsFragment
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.example.scanner.R
import retrofit2.http.Url
import java.io.FileInputStream
import java.net.URI
import java.net.URL

class FileDownload(
    private val strSourceFilePath: String,
    private val strReceivePath: String =Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toString(),
    private val onSuccess: ((receivedFile: File) -> Unit)?=null
): BaseFragmentDialog() {
    private val fileDownloadViewModel:FileDownloadViewModel by viewModels{viewModelFactory}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView:FrameLayout=FrameLayout(requireContext())
            .apply {
                addView(
                    ProgressBar(requireContext())
                )
            }

        fileDownloadViewModel.downloadFileFromLocalNetwork(
            strSourceFilePath,
            strReceivePath,
            onSuccess={file->
                onSuccess?.invoke(file)
                dialogView.removeAllViews()
                dialogView.addView(
                        TextView(requireContext())
                            .apply {
                                text="\nУспешно!\n${file.name} -> ${Environment.DIRECTORY_DOWNLOADS}"
                                gravity= Gravity.CENTER
                            }
                    )
                (dialog as AlertDialog?)?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled=true

            },
            onError = {e->
                dialogView.removeAllViews()
                dialogView.addView(
                    TextView(requireContext())
                        .apply {
                            text=e.message
                            gravity= Gravity.CENTER
                        }
                )
            }
        )

        return   AlertDialog.Builder(requireContext())
                    .setTitle("Загрузка")
                    .setView(dialogView)
                    .setNegativeButton(android.R.string.cancel) { _,_-> }
                    .setPositiveButton("Открыть"){ _, _ -> startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))}
                    .create().apply {
                        view?.visibility= View.GONE
                        setOnShowListener { d->

                            this.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled=false
                        }
                    }
    }

    class FileDownloadViewModel(private val pref: Pref): BaseViewModel(){
        fun downloadFileFromLocalNetwork(
            strSourceFileFullPath: String,
            strReceiveFolderFullPath: String,
            onSuccess: ((receivedFile: File) -> Unit)?=null,
            onError: ((e: Exception) -> Unit)?=null
        ){
            try {
                var strFileName: String=""

                Other.getInstanceSingleton().ioCoroutineScope.launch {
                    try {

                        val fileInputStream=
                            when{
                                strSourceFileFullPath.startsWith("http")||
                                strSourceFileFullPath.startsWith("https")->{
                                    val url=URL(Uri.decode(strSourceFileFullPath))
                                    strFileName=File(url.file).name
                                    url.openStream()
                                }
                                else->{
                                    val smbFileSrc=
                                        SmbFile("smb:/"+ strSourceFileFullPath
                                            .replace("\\\\".toRegex(),"/")
                                            .replace("\\\\\\\\".toRegex(),"/")
                                            .replace("\\\\".toRegex(),"/")
                                            .replace("//".toRegex(),"/")
                                        )
                                    strFileName=smbFileSrc.name
                                    SmbFileInputStream(smbFileSrc)

                                }
                            }

                        val fileReceived=File(
                            strReceiveFolderFullPath+ File.separator+strFileName)

                        val buffer = ByteArray(1024)

                        val fileOutputStream=
                            FileOutputStream(fileReceived)


                        var bytesRead = 1

                        while (bytesRead>0) {

                            bytesRead = fileInputStream.read(buffer)

                            if (bytesRead>0) {
                                fileOutputStream.write(buffer,0,bytesRead)
                            }

                        }

                        fileOutputStream.flush()

                        Other.getInstanceSingleton().mainCoroutineScope.launch {
                            onSuccess?.invoke(fileReceived)
                        }

                    }catch (e: Exception){
                        Other.getInstanceSingleton().mainCoroutineScope.launch {
                            onError?.invoke(Exception(strFileName+" -> "+e.message))
                        }
                    }
                }
            }catch (e: Exception){
                onError?.invoke(Exception(strSourceFileFullPath+" -> "+e.message))
            }
        }
    }
}