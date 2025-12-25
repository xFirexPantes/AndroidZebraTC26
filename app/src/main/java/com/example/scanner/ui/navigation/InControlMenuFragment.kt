package com.example.scanner.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.scanner.R
import com.example.scanner.databinding.FragmentInControlMenuBinding
import com.example.scanner.modules.viewModelFactory
import com.example.scanner.ui.navigation.HomeFragment.HomeViewModel

class InControlMenuFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels{ viewModelFactory }
    private var binding: FragmentInControlMenuBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInControlMenuBinding.inflate(inflater, container, false)
        binding?.toolbar?.apply {
            // Устанавливаем заголовок (можно динамически)
            title = "Меню Входной Контроль"

            // Обрабатываем нажатие стрелки «назад»
            setNavigationOnClickListener {
                requireActivity().onBackPressed()  // Или: findNavController().popBackStack()
            }
        }


        return binding?.root ?: inflater.inflate(R.layout.fragment_in_control_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.homeFragmentFormState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeFragmentFormState.SetView -> {
                    // Активируем/деактивируем кнопки на основе прав пользователя
                    binding?.toIncontrol?.isEnabled = state.incontrol  //
                    binding?.toBox?.isEnabled = state.incontrol  //
                    binding?.toWarehouse?.isEnabled = state.accept

                }
                else -> {
                    // Если состояние не SetView — отключаем всё
                    binding?.toIncontrol?.isEnabled = false
                    binding?.toBox?.isEnabled = false
                    binding?.toWarehouse?.isEnabled = false
                }
            }
        }
        binding?.toIncontrol?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                InControlFragment::class.java,
                Bundle().apply { putSerializable(InControlFragment.PARAM, "toIncontrol") }
            )
        }

        binding?.toBox?.setOnClickListener {
            Toast.makeText(requireContext(), "Функция пока не реализована", Toast.LENGTH_SHORT).show()
        }
        binding?.toWarehouse?.setOnClickListener {
            Toast.makeText(requireContext(), "Функция пока не реализована", Toast.LENGTH_SHORT).show()
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
