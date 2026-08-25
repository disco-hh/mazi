package com.mazi.writer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazi.writer.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WriterViewModel(private val repository: WriterRepository) : ViewModel() {
    val books = repository.observeBooks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val selectedBookId = MutableStateFlow<Long?>(null)
    val chapters = selectedBookId.filterNotNull().flatMapLatest(repository::observeChapters)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val selectedChapterId = MutableStateFlow<Long?>(null)
    val selectedChapter = combine(chapters, selectedChapterId) { list, id -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private var saveJob: Job? = null

    fun selectBook(id: Long) { selectedBookId.value = id }
    fun selectChapter(id: Long) { selectedChapterId.value = id }
    fun createBook(title: String) = viewModelScope.launch { selectBook(repository.createBook(title)) }
    fun updateContent(content: String) {
        val chapter = selectedChapter.value ?: return
        saveJob?.cancel()
        saveJob = viewModelScope.launch { delay(450); repository.updateChapterContent(chapter, content) }
    }
}
