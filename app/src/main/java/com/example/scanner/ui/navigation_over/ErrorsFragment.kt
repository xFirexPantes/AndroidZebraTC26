package com.example.scanner.ui.navigation_over

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.app.softInput
import com.example.scanner.databinding.TemplateCardBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateScannerReadyBinding
import com.example.scanner.modules.Other
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseViewModel
import com.example.scanner.ui.base.NonFatalExceptionShowToaste
import com.example.scanner.ui.base.NonFatalExceptionShowDialogMessage
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference

class ErrorsFragment : BaseFragment() {

    companion object{
        const val PARAM="param"
        val nonFatalExceptionShowToasteToken= NonFatalExceptionShowToaste("Token is null")
    }

    private val errorsViewModel: ErrorsViewModel by viewModels { viewModelFactory  }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TemplateFragmentBinding.inflate(inflater,container,false)
            .apply {

                root.visibility= View.GONE

                toolbar.apply {
                    setNavigationOnClickListener {
                        errorsViewModel.mainActivityRouter.navigate(
                            TransparentFragment::class.java
                        )
                    }
                    title="Сервер не отвечает"
                }

                requireContext().softInput(
                    requireActivity().findViewById<View>(android.R.id.content),
                    false
                )

                Other.getInstanceSingleton().parseArguments<Exception>(requireArguments(), PARAM)
                    .let {e->
                        Timber.tag("Exception").d(e)
                        when(e){
                            is NonFatalExceptionShowToaste->{
                                errorsViewModel.mainActivityRouter
                                    .navigate(TransparentFragment::class.java)

                                Toast.makeText(
                                    requireContext(),
                                    e.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            is NonFatalExceptionShowDialogMessage ->{
                                errorsViewModel.mainActivityRouter
                                    .navigate(TransparentFragment::class.java)
                                AlertDialog.Builder(requireContext())
                                    .setMessage(e.message)
                                    .setPositiveButton(android.R.string.ok){_,_->}
                                    .show()

                            }
                            else->{
                                if (e.message?.contains("lateinit property api has not been initialized")==true){
                                    errorsViewModel.mainActivityRouter
                                        .navigate(TransparentFragment::class.java)
                                    AlertDialog.Builder(requireContext())
                                        .setMessage("Установите ip или имя сервера БД")
                                        .setPositiveButton(android.R.string.ok){_,_->}
                                        .show()


                                }else{
                                    root.visibility= View.VISIBLE

                                    root.addView(
                                        TemplateCardBinding.inflate(inflater,root,false)
                                            .apply {
                                                containerHorizon.addView(
                                                    TemplateScannerReadyBinding.inflate(inflater,containerVertical,false)
                                                        .apply {
                                                            icon= ResourcesCompat.getDrawable(resources,R.drawable.ic_error,null)
                                                            title="Пожалуйста, попробуйте повторить запрос позже"
                                                            textReady.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                                                            textReady.setTextColor(
                                                                TextView(context).textColors
                                                                //ResourcesCompat.getColor(resources,android.R.color.black,null)
                                                            )
                                                            iconLayout.root.setOnClickListener {

                                                                val stackTrace = StringWriter()

                                                                e.printStackTrace(PrintWriter(stackTrace))

                                                                AlertDialog.Builder(requireContext())
                                                                    .setMessage(
                                                                        stackTrace.toString()
                                                                    )
                                                                    .setPositiveButton(android.R.string.ok){_,_->}
                                                                    .show()

                                                            }
                                                        }
                                                        .root
                                                )
                                                //region tune card
                                                containerHorizon.gravity= Gravity.CENTER
                                                root.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                                cardView.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                                containerHorizon.layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                                (containerHorizon.parent as ViewGroup).layoutParams.height= FrameLayout.LayoutParams.MATCH_PARENT
                                                //endregion

                                            }
                                            .root
                                    )

//                                    shortException.text=e.toString()
//                                    viewException.setOnClickListener {
//                                        viewException.isSelected=!viewException.isSelected
//                                        if (viewException.isSelected){
//                                            val stackTrace = StringWriter()
//                                            e.printStackTrace(PrintWriter(stackTrace))
//                                            viewException.text=stackTrace.toString()
//                                        }else{
//                                            viewException.setText(R.string.view_exception)
//                                        }
//                                    }
                                }
                            }
                        }
                    }
            }
            .root

    }

    class ErrorsViewModel: BaseViewModel() {
        companion object {
            private var _errorsViewModel: WeakReference<ErrorsViewModel>? = null
            fun getInstanceSingleton(): ErrorsViewModel {
                return _errorsViewModel
                    ?.get()
                    ?:run {
                        ErrorsViewModel().apply { _errorsViewModel = WeakReference(this) }
                    }
            }
        }

    }
}