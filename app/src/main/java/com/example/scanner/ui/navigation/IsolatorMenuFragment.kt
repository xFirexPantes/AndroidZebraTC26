package com.example.scanner.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.databinding.FragmentIsolatorMenuBinding
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.navigation.HomeFragment.HomeViewModel
import com.example.scanner.ui.navigation.ReceiveFragment.Companion.EXTRA_RGM

class IsolatorMenuFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels{ viewModelFactory }
    private var binding: FragmentIsolatorMenuBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIsolatorMenuBinding.inflate(inflater, container, false)
        binding?.toolbar?.apply {
            // Устанавливаем заголовок (можно динамически)
            title = "Меню Изолятор"

            // Обрабатываем нажатие стрелки «назад»
            setNavigationOnClickListener {
                requireActivity().onBackPressed()  // Или: findNavController().popBackStack()
            }
        }


        return binding?.root ?: inflater.inflate(R.layout.fragment_isolator_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.isIso?.isEnabled = false
        binding?.isIso?.isEnabled = false



        homeViewModel.homeFragmentFormState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeFragmentFormState.SetView -> {
                    binding?.isIso?.visibility =
                        if (state.accept) View.VISIBLE else View.GONE
                    binding?.isIso?.isEnabled = state.accept

                    binding?.toWh?.visibility =
                        if (state.accept) View.VISIBLE else View.GONE
                    binding?.toWh?.isEnabled = state.accept

                }
            }
        }
        binding?.isIso?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                IsolatorListFragment::class.java,
                Bundle().apply { putSerializable(IsolatorFragment.PARAM, "iniso") }
            )
        }
        binding?.toWh?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                IsolatorListFragment::class.java,
                Bundle().apply { putSerializable(IsolatorFragment.PARAM, "towh") }
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
