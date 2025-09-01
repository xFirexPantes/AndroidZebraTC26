package com.example.scanner.ui.navigation_setting

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.scanner.app.templateAttributeDataTextView
import com.example.scanner.app.templateCheckBoxCheckBox
import com.example.scanner.app.templateInputTextContainer
import com.example.scanner.app.templateDividerHorizontalDivider
import com.example.scanner.app.templateInputTextMyTextInput
import com.example.scanner.app.templateInputTextTextLayout
import com.example.scanner.app.onRightDrawableClicked
import com.example.scanner.app.templateAttributeTitleTextView
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplatePresenterBinding
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Pref
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel


class SettingFragment : BaseFragment() {

    private val viewModelSetting:ViewModelSetting by viewModels { viewModelFactory }
    private val apiPantes=object : TextWatcher{
        override fun beforeTextChanged(
            p0: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {

        }

        override fun onTextChanged(
            p0: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {

        }

        override fun afterTextChanged(p0: Editable?) {
            if (!p0?.toString().isNullOrEmpty()) {
                viewModelSetting.pref.pantesServerName =
                    p0.toString()
            }
            ApiPantes.getInstanceSingleton()
                .installApi(viewModelSetting.pref.pantesServerName)
        }
    }
    private val ipFileServer=object : TextWatcher{
        override fun beforeTextChanged(
            p0: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {

        }

        override fun onTextChanged(
            p0: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Int
        ) {

        }

        override fun afterTextChanged(p0: Editable?) {
            viewModelSetting.pref.fileServerName=p0.toString()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return TemplateFragmentBinding.inflate(inflater, container, false)
            .apply {
                toolbar.title="Дополнительно"
                root.setBackgroundColor(resources.getColor(android.R.color.transparent,null))
                root.addView(
                    TemplateCardBinding.inflate(inflater,root,false)
                        .apply {

                            //region button Logs
                            containerVertical.addView(
                                Button(requireContext())
                                    .apply {
                                        text="Логи"
                                        setOnClickListener {
                                            viewModelSetting.mainActivityRouter.navigate(
                                                LogsFragment::class.java
                                            )
                                        }
                                    }
                            )
                            //endregion

                            //region manualSearch
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateAttributeTitleTextView.text="Включить ручной поиск"
                                        templateCheckBoxCheckBox
                                            .apply{
                                                viewModelSetting.pref.enableManualInputMutableLiveData.observe(viewLifecycleOwner)
                                                {
                                                    isChecked=View.VISIBLE==it
                                                }

                                                (layoutParams as FrameLayout.LayoutParams).gravity=
                                                    Gravity.START
                                                (parent as View).visibility= View.VISIBLE
                                                setOnClickListener {
                                                    viewModelSetting.pref.manualInputIsEnable=
                                                        if (isChecked) View.VISIBLE else View.GONE
                                                }
                                            }
                                    }
                                    .root
                            )
                            //endregion

                            //region namePantesServer
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateInputTextContainer.visibility= View.VISIBLE

                                        templateInputTextTextLayout.hint="Имя или IP сервера БД"
                                        templateInputTextMyTextInput.onRightDrawableClicked{
                                            templateInputTextMyTextInput.text = null
                                        }
                                        templateInputTextMyTextInput.removeTextChangedListener(apiPantes)
                                        templateInputTextMyTextInput.setText(viewModelSetting.pref.pantesServerName)
                                        templateInputTextMyTextInput.addTextChangedListener(apiPantes)

                                    }
                                    .root
                            )
                            //endregion

//                            //region nameFileServer
//                            containerVertical.addView(
//                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
//                                    .apply {
//                                        titleAttribute.visibility= View.GONE
//                                        dataAttribute.visibility= View.GONE
//                                        dataAttributeNoteViewContainer.visibility= View.VISIBLE
//
//                                        noteLayout.hint="Имя или IP файл-сервера"
//                                        note.onRightDrawableClicked{
//                                            note.text = null
//                                        }
//                                        note.setText(viewModelSetting.pref.fileServerName)
//                                        note.removeTextChangedListener(ipFileServer)
//                                        note.addTextChangedListener(ipFileServer)
//                                    }
//                                    .root
//                            )
//                            //endregion
                            //region divider
                            containerVertical.addView(TextView(requireContext()))
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateDividerHorizontalDivider.visibility= View.VISIBLE

                                    }
                                    .root
                            )
                            //endregion

                            //region update app
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {
                                        templateAttributeDataTextView.text="Обновления:"
                                    }
                                    .root
                            )
                            containerVertical.addView(
                                TemplatePresenterBinding.inflate(inflater,containerVertical,false)
                                    .apply {

                                        templateAttributeTitleTextView.text=
                                            StringBuilder()
                                                .append("10.08.25 - опечатка, очистка комментария, меню сортировки открывается с первого тапа")
                                                .append("\n")
                                                .append("\n")
                                                .append("10.08.25 - оптимизация(3) строк накладной ")
                                                .append("\n")
                                                .append("\n")
                                                .append("06.08.25 - оптимизация(2) строк накладной ")
                                                .append("\n")

                                    }
                                    .root
                            )

                            //endregion
//                            containerVertical.addView(
//                                Button(requireContext())
//                                    .apply {
//                                        text="Test"
//                                        setOnClickListener {
//                                            containerVertical.tag
//                                                ?.let {
//                                                    (containerVertical.tag as TemplatePresenterBinding).setAttribute(
//                                                        Pair(arrayOf(""),"sss0"),"")
//
//                                                }
//                                                ?:run {
//                                                    containerVertical.tag=
//                                                        TemplatePresenterBinding.inflate(inflater,containerVertical,false)
//                                                            .apply {
//                                                                setAttribute(
//                                                                    Pair(arrayOf(""),"sss1"),"")
//                                                            }
//                                                    containerVertical.addView(
//                                                        (containerVertical.tag as TemplatePresenterBinding).root
//                                                    )
//                                                }
//                                        }
//                                        })

//                            //region button Logs
//                            containerVertical.addView(
//                                Button(requireContext())
//                                    .apply {
//                                        text="Test"
//                                        setOnClickListener {
//
//                                            Other.getInstanceSingleton().ioCoroutineScope.launch {
//                                                val  b=ByteArray(1024)
//                                                val conn=URL("http://192.168.3.63:8080/app-debug.apk").openConnection()
//                                                (conn as HttpURLConnection).setRequestMethod("HEAD")
//
//                                                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//
//                                                        val d=conn.getHeaderField("Last-Modified")
//                                                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").parse(d)
//
//                                                }
//                                                conn.connect()
//                                                val zin=ZipInputStream(conn.getInputStream())
//                                                val fout: FileOutputStream =
//                                                    FileOutputStream(context.filesDir.path+"/tmp.loading")
//                                                //val inBufferedInputStream = BufferedInputStream(zin)
//                                                val inBufferedInputStream = BufferedInputStream(conn.getInputStream())
//                                                val outBufferedOutputStream = BufferedOutputStream(fout)
//                                                var n: Int
//                                                while ((inBufferedInputStream.read(b, 0, 1024).also { n = it }) >= 0) {
//                                                    outBufferedOutputStream.write(b, 0, n)
//                                                }
//
//                                                zin.closeEntry()
//                                                outBufferedOutputStream.close()
//
//                                            }
//
//
//                                            //File("http://localhost:8080/app.debug.apk")
////                                            val intent: Intent = Intent(Intent.ACTION_VIEW)
////                                            val uri = FileProvider.getUriForFile(
////                                                context,
////                                                context.packageName+".fileprovider",
////                                                File(
////                                                    context.cacheDir.path+"/app-debug.apk"
////                                                )
////                                            )
////                                            intent.setDataAndType(
////                                                uri,
////                                                "application/vnd.android.package-archive"
////                                            )
////                                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
////                                            startActivity(intent)
//                                        }
//                                    }
//                            )
//                            //endregion

                        }
                        .root
                )
            }
            .root
    }


    class ViewModelSetting(val pref: Pref) : BaseViewModel()
}