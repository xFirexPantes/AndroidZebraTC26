package com.example.scanner.ui.navigation_over

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.app.downloadFile
import com.example.scanner.app.loadImage
import com.example.scanner.databinding.FragmentTransparentBinding
import com.example.scanner.databinding.TemplateFragmentBinding
import com.example.scanner.databinding.TemplateFrameBinding
import com.example.scanner.databinding.TemplateIconBinding
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.NonFatalExceptionShowToaste
import com.example.scanner.ui.navigation_over.ErrorsFragment.ErrorsViewModel
import io.getstream.photoview.PhotoView
import kotlin.getValue

class TransparentFragment : BaseFragment() {

    companion object{
        const val PARAM="param"
    }
    private val errorsViewModel: ErrorsViewModel by viewModels { viewModelFactory  }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        return when(val param=try {getArgument<String?>(PARAM)}catch (_: Exception){null}){
            null->FragmentTransparentBinding.inflate(layoutInflater,null,false).root
            else -> TemplateFragmentBinding.inflate(inflater,null,false)
                .apply {

                    iconContainer.addView(
                        TemplateIconBinding.inflate(inflater,iconContainer,false)
                            .apply {

                                src=ResourcesCompat.getDrawable(resources,R.drawable.ic_download,null)

                                image.setOnClickListener {
                                    try {
                                        param.downloadFile(requireContext())
                                    }catch (e:Exception){

                                        errorsViewModel.mainActivityRouter.navigate(
                                            ErrorsFragment::class.java,
                                            Bundle().apply {
                                                putSerializable(
                                                    ErrorsFragment.PARAM,
                                                    NonFatalExceptionShowToaste("Ошибка загрузки ${e.message}"))
                                            }
                                        )

                                    }
                                }
                            }
                            .root
                    )

                    root.setBackgroundColor(Color.BLACK)
                    toolbar.title="Просмотр"
                    toolbar.setNavigationOnClickListener {
                        errorsViewModel.mainActivityRouter.navigate(TransparentFragment::class.java)
                    }

                    root.addView(
                        TemplateFrameBinding.inflate(layoutInflater,root,false)
                            .apply {
                                root.addView(

                                    PhotoView(requireContext())
                                        .apply {
                                                loadImage(param)
                                        }
                                )
                            }
                            .root
                    )
                }
                .root
        }

    }
}