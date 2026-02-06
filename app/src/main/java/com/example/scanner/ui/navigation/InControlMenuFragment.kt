package com.example.scanner.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
        binding?.toIncontrol?.isEnabled = false
        binding?.toBox?.isEnabled = false
        binding?.toWarehouse?.isEnabled = false
        binding?.WHtoIncontrol?.isEnabled = false
        binding?.toDry?.isEnabled = false
        binding?.fromDry?.isEnabled = false
        binding?.listDry?.isEnabled = false


        homeViewModel.homeFragmentFormState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeFragmentFormState.SetView -> {
                    // Кнопка "Отправить на ВК" (toIncontrol)
                    binding?.WHtoIncontrol?.visibility =
                        if (state.accept) View.VISIBLE else View.GONE
                    binding?.WHtoIncontrol?.isEnabled = state.accept
                    // Кнопка "Принять на ВК" (toIncontrol)
                    binding?.toIncontrol?.visibility =
                        if (state.incontrol) View.VISIBLE else View.GONE
                    binding?.toIncontrol?.isEnabled = state.incontrol

                    // Кнопка "Положить в коробку" (toBox)
                    binding?.toBox?.visibility =
                        if (state.incontrol) View.VISIBLE else View.GONE
                    binding?.toBox?.isEnabled = state.incontrol

                    // Кнопка "Принять на склад" (toWarehouse)
                    binding?.toWarehouse?.visibility =
                        if (state.accept) View.VISIBLE else View.GONE
                    binding?.toWarehouse?.isEnabled = state.accept

                    binding?.toDry?.visibility =
                        if (state.incontrol) View.VISIBLE else View.GONE
                    binding?.toDry?.isEnabled = state.incontrol

                    binding?.fromDry?.visibility =
                        if (state.accept) View.VISIBLE else View.GONE
                    binding?.fromDry?.isEnabled = state.accept

                    binding?.listDry?.visibility =
                        if (state.accept || state.incontrol) View.VISIBLE else View.GONE
                    binding?.listDry?.isEnabled = (state.accept || state.incontrol)
                }
            }
        }
        binding?.WHtoIncontrol?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                InControlFragment::class.java,
                Bundle().apply { putSerializable(InControlFragment.PARAM, "WHtoIncontrol") }
            )
        }
        binding?.toIncontrol?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                InControlFragment::class.java,
                Bundle().apply { putSerializable(InControlFragment.PARAM, "toIncontrol") }
            )
        }

        binding?.toBox?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                InControlFragment::class.java,
                Bundle().apply { putSerializable(InControlFragment.PARAM, "toBox") }
            )
        }
        binding?.toWarehouse?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                InControlFragment::class.java,
                Bundle().apply { putSerializable(InControlFragment.PARAM, "toWH") }
            )
        }
        binding?.toDry?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                DryFragment::class.java,
                Bundle().apply { putSerializable(DryFragment.PARAM, "toDry") }
            )
        }
        binding?.fromDry?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                DryFragment::class.java,
                Bundle().apply { putSerializable(DryFragment.PARAM, "fromDry") }
            )
        }
        binding?.listDry?.setOnClickListener {
            homeViewModel.mainActivityRouter.navigate(
                DryFragment::class.java,
                Bundle().apply { putSerializable(DryFragment.PARAM, "listDry") }
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
