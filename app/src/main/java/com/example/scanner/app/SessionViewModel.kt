package com.example.scanner.app

import android.content.Context

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SessionViewModel(private val context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("scanned_items", Context.MODE_PRIVATE)

    // LiveData для наблюдаемых изменений
    private val _scannedItems = MutableLiveData<Set<Int>>()
    val scannedItems: LiveData<Set<Int>> = _scannedItems

    init {
        loadScannedItems() // Загружаем при создании ViewModel
    }

    fun addItem(id: Int) {
        val updated = (_scannedItems.value ?: emptySet()) + id
        _scannedItems.postValue(updated)
        saveScannedItems(updated) // Сохраняем сразу
    }

    fun removeItem(id: Int) {
        val updated = (_scannedItems.value ?: emptySet()) - id
        _scannedItems.postValue(updated)
        saveScannedItems(updated)
    }

    fun clearScannedItems() {
        _scannedItems.postValue(emptySet())
        saveScannedItems(emptySet())
    }

    private fun saveScannedItems(items: Set<Int>) {
        prefs.edit()
            .putStringSet("items", items.map { it.toString() }.toSet())
            .apply()
    }

    private fun loadScannedItems() {
        val saved = prefs.getStringSet("items", emptySet())
        if (saved != null) {
            _scannedItems.postValue(saved.map { it.toInt() }.toSet())
        }
    }
}