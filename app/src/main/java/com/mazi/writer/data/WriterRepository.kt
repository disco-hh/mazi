package com.mazi.writer.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class WriterRepository(private val database: AppDatabase) {
    private val books = database.bookDao()
    private val chapters = database.chapterDao()
    private val notes = database.noteDao()

    fun observeBooks(): Flow<List<Book>> = books.observeAll()
    fun observeChapters(bookId: Long): Flow<List<Chapter>> = chapters.observeForBook(bookId)
    fun observeNotes(bookId: Long): Flow<List<Note>> = notes.observeForBook(bookId)

    suspend fun createBook(title: String): Long {
        val id = System.currentTimeMillis()
        database.withTransaction {
            books.save(Book(id = id, title = title))
            chapters.save(Chapter(id = id + 1, bookId = id, title = "第一章", content = "", position = 0))
        }
        return id
    }

    suspend fun ensureStarterBook() {
        if (books.count() == 0) createBook("未命名作品")
    }

    suspend fun createChapter(bookId: Long, title: String): Long {
        val id = System.currentTimeMillis()
        chapters.save(Chapter(id, bookId, title.ifBlank { "新章节" }, "", chapters.nextPosition(bookId)))
        books.touch(bookId, System.currentTimeMillis())
        return id
    }

    suspend fun createNote(bookId: Long, title: String, detail: String, type: NoteType) {
        notes.save(Note(System.currentTimeMillis(), bookId, title.ifBlank { "未命名资料" }, detail, type))
        books.touch(bookId, System.currentTimeMillis())
    }

    suspend fun saveChapter(chapter: Chapter) {
        chapters.save(chapter)
        books.touch(chapter.bookId, System.currentTimeMillis())
    }

    suspend fun updateChapterContent(chapter: Chapter, content: String) {
        database.withTransaction {
            chapters.updateContent(chapter.id, content)
            books.touch(chapter.bookId, System.currentTimeMillis())
        }
    }

    suspend fun reorder(bookId: Long, ordered: List<Chapter>) = database.withTransaction {
        ordered.forEachIndexed { index, chapter -> chapters.updatePosition(chapter.id, index) }
    }

    suspend fun saveNote(note: Note) = notes.save(note)
}
