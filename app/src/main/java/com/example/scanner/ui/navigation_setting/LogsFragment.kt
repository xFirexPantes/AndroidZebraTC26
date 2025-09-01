package com.example.scanner.ui.navigation_setting

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanner.R
import com.example.scanner.app.App
import com.example.scanner.app.toChunks
import com.example.scanner.databinding.FragmentLogsBinding
import com.example.scanner.databinding.ItemLogBinding
import com.example.scanner.databinding.TemplateDialogBinding
import com.example.scanner.modules.ApiPantes
import com.example.scanner.modules.Other
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.dialogs.ClearLogsFilesDialog
import com.example.scanner.ui.dialogs.ShareFilesDialog
import com.example.scanner.ui.navigation.login.LoginRepository
import com.example.scanner.ui.navigation_over.TransparentFragment
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import kotlin.collections.forEach

class LogsFragment : BaseFragment() {

    private var _binding: FragmentLogsBinding? = null
    private val viewModelLogs: ViewModelLogs by viewModels { viewModelFactory }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.apply {

            viewModelLogs.recyclerAdapterLog
                .observe(viewLifecycleOwner,{
                    logsRecycler.adapter=it
                })

            toolbar.setNavigationOnClickListener {
                if (!findNavController().navigateUp())
                    requireActivity().finish()
            }

            viewModelLogs.arrayAdapterTags.observe(viewLifecycleOwner){
                spinner.adapter=it
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        viewModelLogs.recyclerAdapterLog.value=
                            ViewModelLogs.LogAdapter(
                                ArrayList(
                                    when(p2){
                                        0->viewModelLogs.data
                                        else->viewModelLogs.data.filter { r-> r.contains((p1 as AppCompatCheckedTextView).text) }
                                    }
                                ),
                                requireContext()
                            )
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }
                }
            }

            logsRecycler.layoutManager=
                LinearLayoutManager(context)

            toolbar.inflateMenu(R.menu.main)

            toolbar.setOnMenuItemClickListener { item->

                when(item.itemId){

                    R.id.action_settings->
                        ShareFilesDialog().show(
                            childFragmentManager,
                            ShareFilesDialog::class.java.name)

                    R.id.action_clear->{
                            ClearLogsFilesDialog().show(
                                childFragmentManager,
                                ClearLogsFilesDialog::class.java.name)
                    }
                    R.id.action_refresh->{
                        viewModelLogs.data.clear()
                        viewModelLogs.recyclerAdapterLog.value=null
                        viewModelLogs.arrayAdapterTags.value=null
                        viewModelLogs.fillLog(requireContext())
                    }
                    R.id.action_to_server->{
                        val dialog: Dialog
                        val binder= TemplateDialogBinding.inflate(layoutInflater)
                        dialog= AlertDialog.Builder(requireContext())
                            .setView(binder.root)
                            .setCancelable(false)
                            .show()
                        binder.text.text="Идет отправка"
                        binder.progress.visibility= View.VISIBLE
                        binder.ok.visibility= View.GONE
                        binder.cancel.setOnClickListener {
                            dialog.dismiss()
                        }

                        Other.Companion.getInstanceSingleton().ioCoroutineScope.launch {
                            viewModelLogs.fillLog(requireContext())
                            var errorUploadLog: String?=null
                            try {
                                binder.progress.tag=0
                                viewModelLogs.data.forEach {
                                    if (dialog.isShowing && errorUploadLog.isNullOrBlank()){
                                        val complete=binder.progress.tag.toString().toInt()
                                        it.toChunks(1024*4).forEach {
                                            val response=viewModelLogs.requestLog(it)
                                            if (response.isSuccessful){
                                                binder.progress.tag=complete+1
                                                Other.Companion.getInstanceSingleton().mainCoroutineScope.launch {
                                                    binder.progress.progress=100*complete/viewModelLogs.data.size
                                                }
                                            }
                                            else{
                                                Other.Companion.getInstanceSingleton().mainCoroutineScope.launch {
                                                    errorUploadLog="${response.errorBody()?.string()}"
                                                    binder.text.text=errorUploadLog
                                                }
                                            }

                                        }
                                    }
                                }
                            }catch (e: Exception){
                                    errorUploadLog=e.message
                            }

                            Other.Companion.getInstanceSingleton().mainCoroutineScope.launch {
                                if (errorUploadLog.isNullOrEmpty()){
                                    dialog.dismiss()
                                }else{
                                    binder.text.text=errorUploadLog
                                }
                                viewModelLogs.mainActivityRouter.navigate(
                                    TransparentFragment::class.java
                                )
                            }

                        }

                    }
                }

                true
            }

        }

        return root
    }

    override fun onStart() {
        super.onStart()
        viewModelLogs.fillLog(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ViewModelLogs(private val apiPantes: ApiPantes, private val loginRepository: LoginRepository) : BaseViewModel() {

        val data=ArrayList<String>()

        fun fillLog(requireContext: Context) {

            var lineTail=""
            data.clear()
            App.Companion.fileLoggerTree?.files?.forEach { file: File? ->
                file?.readLines()?.forEach { line->
                    if(line.indexOf(" : ")>=0){
                        if (lineTail.isNotEmpty()){
                            data.add(0,lineTail)
                            lineTail=""
                        }
                        lineTail=lineTail.plus(line)
                    }
                    else{
                        lineTail=lineTail.plus("\n").plus(line)
                    }
                }
                data.add(0,lineTail)
            }
            data.sortDescending()

            if (recyclerAdapterLog.value==null){
                fillTags(requireContext,data)
            }

//        recyclerAdapterLog.value=
//            LogAdapter(data,requireContext)
        }

        private fun fillTags(requireContext: Context, data:ArrayList<String>){

            arrayAdapterTags.value= ArrayAdapter<String>(
                requireContext,
                android.R.layout.simple_spinner_dropdown_item,
                ArrayList<String?>().apply {
                    add("All")
                    data.forEach { line ->
                        val tag =
                            try {
                                val temp = line.split(" : ")[0].split("/")[1]
                                val endIndex = temp.indexOf(" ")
                                temp.substring(
                                    0,
                                    if (endIndex > 0) endIndex else temp.length
                                )
                            } catch (_: Exception) {
                                "Ошибка"
                            }
                        if (!this.contains(tag)) {
                            this.add(tag)
                        }
                    }
                }
            )
        }

        val recyclerAdapterLog=
            MutableLiveData<RecyclerView.Adapter<RecyclerView.ViewHolder>?>()

        val arrayAdapterTags=
            MutableLiveData<ArrayAdapter<String>?>()

        class LogAdapter(private val data:ArrayList<String>,private val requireContext: Context):
            RecyclerView.Adapter<RecyclerView.ViewHolder>(){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(ItemLogBinding.inflate(LayoutInflater.from(requireContext)).root){}
            }

            override fun getItemCount(): Int {
                return data.size
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val titlePlusContent=
                    data[position].split(" : ")
                ItemLogBinding.bind(holder.itemView)
                    .let {itemLog->
                        itemLog.title.text=titlePlusContent[0]
                        itemLog.title.setOnClickListener{
                            itemLog.content.visibility=
                                if(itemLog.content.isGone)  View.VISIBLE else View.GONE
                            if (titlePlusContent.size>1)
                                itemLog.content.text=titlePlusContent[1]
                        }
                        itemLog.content.visibility= View.GONE
                    }

            }

        }

        suspend fun requestLog(line: String): Response<ResponseBody?> {
            return apiPantes.log(loginRepository.user!!.token!!,line)
        }

    }
}