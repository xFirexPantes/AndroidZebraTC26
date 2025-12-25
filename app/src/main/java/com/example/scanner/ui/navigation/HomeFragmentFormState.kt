package com.example.scanner.ui.navigation


sealed class HomeFragmentFormState<out T : Any> {
    data class SetView(
        val username: String? = null,
        val isolator: Boolean,
        val issuance: Boolean,
        val accept: Boolean,
        val search: Boolean,
        val incontrol: Boolean,
    ) : HomeFragmentFormState<Nothing>()
}