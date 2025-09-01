package com.example.scanner.ui.navigation_over

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.scanner.R
import com.example.scanner.ui.base.BaseFragment
import com.example.scanner.ui.base.BaseFragmentDialog
import com.example.scanner.ui.dialogs.ProgressDialog

class ProgressFragment : BaseFragment() {

    private var progressDialog:ProgressDialog?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }
    fun recursiveGetFragments(parents: List<Fragment>): List<Fragment> {
        val result = parents.toMutableList()
        for(root in parents) {
            if (root.isVisible) {
                result.addAll(recursiveGetFragments(root.childFragmentManager.fragments))
            }
        }
        return result
    }
    override fun onResume() {
        super.onResume()
        val isShowDialog=
            recursiveGetFragments(requireActivity().supportFragmentManager.fragments)
            .any { fragment -> fragment is BaseFragmentDialog }
        if (isShowDialog)
            progressDialog=ProgressDialog()

        progressDialog?.show(childFragmentManager,BaseFragmentDialog::class.java.toString())
    }

    override fun onPause() {
        progressDialog?.dismiss()
        super.onPause()
    }

}