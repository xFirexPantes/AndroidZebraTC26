package com.example.scanner.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.databinding.FragmentInvoiceMenuBinding
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.navigation.HomeFragment.HomeViewModel

class InvoiceMenuFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels{ viewModelFactory }
    private var binding: FragmentInvoiceMenuBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvoiceMenuBinding.inflate(inflater, container, false)
        binding?.toolbar?.apply {
            // Устанавливаем заголовок (можно динамически)
            title = "Меню Набор"

            // Обрабатываем нажатие стрелки «назад»
            setNavigationOnClickListener {
                requireActivity().onBackPressed()  // Или: findNavController().popBackStack()
            }
        }


        return binding?.root ?: inflater.inflate(R.layout.fragment_invoice_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.toControl?.isEnabled = false
        binding?.toReturn?.isEnabled = false



        homeViewModel.homeFragmentFormState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeFragmentFormState.SetView -> {
                    binding?.toControl?.visibility =
                        if (state.accept) View.VISIBLE else View.GONE
                    binding?.toControl?.isEnabled = state.accept

                    binding?.toReturn?.alpha = 0.5f
                       // if (state.accept) View.VISIBLE else View.GONE
                    binding?.toReturn?.isEnabled = false

                }
            }
        }
        binding?.toControl?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                InvoiceFragment::class.java,
                Bundle().apply { putSerializable(InvoiceFragment.PARAM, "") }
            )
        }
        binding?.toReturn?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                ReturnFragment::class.java,
                Bundle().apply { putSerializable(ReturnFragment.PARAM_STEP_1_VALUE, "") }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
    companion object{
        const val PARAM_STEP_1_VALUE="param"

    }
}
