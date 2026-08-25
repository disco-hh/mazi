package com.mazi.writer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazi.writer.data.*
import android.content.ContentResolver
import android.net.Uri
import com.mazi.writer.export.BookExporter
import com.mazi.writer.export.ExportFormat
import com.mazi.writer.export.NovelBackup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WriterViewModel(private val repository: WriterRepository) : ViewModel() {
    val books = repository.observeBooks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val selectedBookId = MutableStateFlow<Long?>(null)
    val activeBookId: StateFlow<Long?> = selectedBookId.asStateFlow()
    val activeBook = combine(books, selectedBookId) { list, id -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val chapters = selectedBookId.filterNotNull().flatMapLatest(repository::observeChapters)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val volumes = selectedBookId.filterNotNull().flatMapLatest(repository::observeVolumes)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val notes = selectedBookId.filterNotNull().flatMapLatest(repository::observeNotes)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val selectedChapterId = MutableStateFlow<Long?>(null)
    val selectedChapter = combine(chapters, selectedChapterId) { list, id -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private var saveJob: Job? = null
    private val _searchResults = MutableStateFlow<List<Chapter>>(emptyList())
    val searchResults: StateFlow<List<Chapter>> = _searchResults.asStateFlow()
    private var replaceUndo: WriterRepository.ReplaceUndo? = null

    init {
        viewModelScope.launch {
            repository.ensureStarterBook()
            repository.observeBooks().filter { it.isNotEmpty() }.first().let { selectBook(it.first().id) }
        }
    }

    fun selectBook(id: Long) { selectedBookId.value = id; selectedChapterId.value = null }
    fun selectChapter(id: Long) { selectedChapterId.value = id }
    fun createBook(title: String) = viewModelScope.launch { selectBook(repository.createBook(title)) }
    fun createChapter(title: String) = viewModelScope.launch {
        selectedBookId.value?.let { selectedChapterId.value = repository.createChapter(it, title, selectedChapter.value?.volumeId) }
    }
    fun createVolume(title: String) = viewModelScope.launch {
        selectedBookId.value?.let { repository.createVolume(it, title) }
    }
    fun createNote(title: String, detail: String, type: NoteType) = viewModelScope.launch {
        selectedBookId.value?.let { repository.createNote(it, title, detail, type) }
    }
    fun updateContent(content: String) {
        val chapter = selectedChapter.value ?: return
        saveJob?.cancel()
        saveJob = viewModelScope.launch { delay(800); repository.updateChapterContent(chapter, content) }
    }
    fun updateStatus(status: ChapterStatus) = viewModelScope.launch {
        selectedChapter.value?.let { repository.updateChapterStatus(it.id, status) }
    }
    fun search(query: String) = viewModelScope.launch {
        _searchResults.value = selectedBookId.value?.takeIf { query.isNotBlank() }?.let { repository.searchChapters(it, query) }.orEmpty()
    }
    fun replaceAll(find: String, replacement: String, ignoreCase: Boolean) = viewModelScope.launch { selectedBookId.value?.let { replaceUndo = repository.replaceAll(it, find, replacement, ignoreCase) } }
    fun undoReplace() = viewModelScope.launch { replaceUndo?.let { repository.undoReplace(it); replaceUndo = null } }
    fun export(resolver: ContentResolver, uri: Uri, format: ExportFormat) = viewModelScope.launch {
        selectedBookId.value?.let { id -> repository.exportPayload(id)?.let { (book, chapters) -> BookExporter.write(resolver, uri, BookExporter.render(book, chapters, format)) } }
    }
    fun backup(resolver: ContentResolver, uri: Uri) = viewModelScope.launch {
        selectedBookId.value?.let { id -> repository.backupPayload(id)?.let { NovelBackup.write(resolver, uri, it) } }
    }
    fun restore(resolver: ContentResolver, uri: Uri) = viewModelScope.launch {
        selectBook(repository.restore(NovelBackup.read(resolver, uri)))
    }
}
