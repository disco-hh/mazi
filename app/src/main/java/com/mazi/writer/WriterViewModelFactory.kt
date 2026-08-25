package com.mazi.writer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mazi.writer.data.WriterRepository

class WriterViewModelFactory(private val repository: WriterRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = WriterViewModel(repository) as T
}
