package com.mazi.writer.data

import androidx.room.withTransaction
import com.mazi.writer.export.NovelBackup
import kotlinx.coroutines.flow.Flow

class WriterRepository(private val database: AppDatabase) {
    data class ReplaceUndo(val originals: List<Chapter>, val replacements: Int)
    private val books = database.bookDao()
    private val volumes = database.volumeDao()
    private val chapters = database.chapterDao()
    private val notes = database.noteDao()

    fun observeBooks(): Flow<List<Book>> = books.observeAll()
    data class BackupPayload(val book: Book, val volumes: List<Volume>, val chapters: List<Chapter>, val notes: List<Note>)
    suspend fun exportPayload(bookId: Long): Pair<Book, List<Chapter>>? = books.get(bookId)?.let { it to chapters.getForBook(bookId) }
    suspend fun backupPayload(bookId: Long): BackupPayload? = books.get(bookId)?.let { book -> BackupPayload(book, volumes.getForBook(bookId), chapters.getForBook(bookId), notes.getForBook(bookId)) }
    suspend fun restore(project: NovelBackup.RestoredProject): Long {
        val now = System.currentTimeMillis()
        val newBookId = now
        database.withTransaction {
            books.save(Book(id = newBookId, title = "${project.title}（恢复副本）", author = project.author, summary = project.summary, genre = project.genre, targetWords = project.targetWords, dailyGoal = project.dailyGoal))
            val volumeIds = project.volumes.associate { source -> source.sourceId to (now + source.position + 10_000) }
            project.volumes.forEach { source -> volumes.save(Volume(volumeIds.getValue(source.sourceId), newBookId, source.title, source.position)) }
            project.chapters.forEachIndexed { index, source -> chapters.save(Chapter(id = now + 100_000 + index, bookId = newBookId, volumeId = source.sourceVolumeId?.let(volumeIds::get), title = source.title, content = source.content, position = source.position, status = source.status, outline = source.outline, wordCount = countWords(source.content))) }
            project.notes.forEachIndexed { index, source -> notes.save(Note(now + 200_000 + index, newBookId, source.title, source.detail, source.type, source.tags)) }
        }
        return newBookId
    }
    fun observeVolumes(bookId: Long): Flow<List<Volume>> = volumes.observeForBook(bookId)
    fun observeChapters(bookId: Long): Flow<List<Chapter>> = chapters.observeForBook(bookId)
    suspend fun searchChapters(bookId: Long, query: String): List<Chapter> = chapters.search(bookId, query)
    fun observeNotes(bookId: Long): Flow<List<Note>> = notes.observeForBook(bookId)

    suspend fun createBook(title: String): Long {
        val id = System.currentTimeMillis()
        database.withTransaction {
            books.save(Book(id = id, title = title))
            chapters.save(Chapter(id = id + 1, bookId = id, title = "第 1 章", content = "", position = 1_000))
        }
        return id
    }

    suspend fun ensureStarterBook() {
        if (books.count() == 0) createBook("未命名作品")
    }

    suspend fun createChapter(bookId: Long, title: String, volumeId: Long? = null): Long {
        val id = System.currentTimeMillis()
        chapters.save(Chapter(id = id, bookId = bookId, volumeId = volumeId, title = title.ifBlank { "新章节" }, content = "", position = chapters.nextPosition(bookId)))
        books.touch(bookId, System.currentTimeMillis())
        return id
    }

    suspend fun createVolume(bookId: Long, title: String): Long {
        val id = System.currentTimeMillis()
        volumes.save(Volume(id, bookId, title.ifBlank { "新卷" }, volumes.nextPosition(bookId)))
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
            chapters.updateContent(chapter.id, content, countWords(content), System.currentTimeMillis())
            books.touch(chapter.bookId, System.currentTimeMillis())
        }
    }

    suspend fun reorder(bookId: Long, ordered: List<Chapter>) = database.withTransaction {
        ordered.forEachIndexed { index, chapter -> chapters.updatePosition(chapter.id, index) }
    }

    suspend fun saveNote(note: Note) = notes.save(note)

    suspend fun updateChapterStatus(chapterId: Long, status: ChapterStatus) {
        chapters.updateStatus(chapterId, status, System.currentTimeMillis())
    }

    suspend fun replaceAll(bookId: Long, find: String, replacement: String, ignoreCase: Boolean): ReplaceUndo {
        require(find.isNotBlank())
        val originals = chapters.getForBook(bookId).filter { it.content.contains(find, ignoreCase) || it.title.contains(find, ignoreCase) }
        var replacements = 0
        database.withTransaction {
            originals.forEach { chapter ->
                val newTitle = chapter.title.replace(find, replacement, ignoreCase = ignoreCase)
                val newContent = chapter.content.replace(find, replacement, ignoreCase = ignoreCase)
                replacements += occurrences(chapter.title, find, ignoreCase) + occurrences(chapter.content, find, ignoreCase)
                chapters.save(chapter.copy(title = newTitle, content = newContent, wordCount = countWords(newContent), updatedAt = System.currentTimeMillis()))
            }
        }
        return ReplaceUndo(originals, replacements)
    }

    suspend fun undoReplace(undo: ReplaceUndo) = database.withTransaction { undo.originals.forEach(chapters::save) }

    private fun countWords(text: String): Int {
        var count = 0
        var inLatinWord = false
        text.forEach { char ->
            when {
                char.isLetterOrDigit() && char.code > 0x7F -> { count++; inLatinWord = false }
                char.isLetterOrDigit() -> if (!inLatinWord) { count++; inLatinWord = true }
                else -> inLatinWord = false
            }
        }
        return count
    }
    private fun occurrences(text: String, target: String, ignoreCase: Boolean): Int { var index = 0; var count = 0; while (true) { index = text.indexOf(target, index, ignoreCase); if (index < 0) return count; count++; index += target.length } }
}
