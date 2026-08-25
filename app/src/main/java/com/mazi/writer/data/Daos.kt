package com.mazi.writer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT COUNT(*) FROM books") suspend fun count(): Int
    @Query("SELECT * FROM books WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY updatedAt DESC") fun observeAll(): Flow<List<Book>>
    @Query("SELECT * FROM books WHERE id = :id") fun observe(id: Long): Flow<Book?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(book: Book)
    @Query("UPDATE books SET updatedAt = :timestamp WHERE id = :id") suspend fun touch(id: Long, timestamp: Long)
    @Delete suspend fun delete(book: Book)
}

@Dao
interface VolumeDao {
    @Query("SELECT * FROM volumes WHERE bookId = :bookId ORDER BY position") fun observeForBook(bookId: Long): Flow<List<Volume>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(volume: Volume)
    @Query("SELECT COALESCE(MAX(position), 0) + 1000 FROM volumes WHERE bookId = :bookId") suspend fun nextPosition(bookId: Long): Int
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND deletedAt IS NULL ORDER BY position") fun observeForBook(bookId: Long): Flow<List<Chapter>>
    @Query("SELECT * FROM chapters WHERE id = :id") fun observe(id: Long): Flow<Chapter?>
    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND deletedAt IS NULL AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY position") suspend fun search(bookId: Long, query: String): List<Chapter>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(chapter: Chapter)
    @Query("SELECT COALESCE(MAX(position), 0) + 1000 FROM chapters WHERE bookId = :bookId") suspend fun nextPosition(bookId: Long): Int
    @Query("UPDATE chapters SET content = :content, wordCount = :wordCount, updatedAt = :updatedAt WHERE id = :id") suspend fun updateContent(id: Long, content: String, wordCount: Int, updatedAt: Long)
    @Query("UPDATE chapters SET position = :position WHERE id = :id") suspend fun updatePosition(id: Long, position: Int)
    @Query("UPDATE chapters SET status = :status, updatedAt = :updatedAt WHERE id = :id") suspend fun updateStatus(id: Long, status: ChapterStatus, updatedAt: Long)
    @Delete suspend fun delete(chapter: Chapter)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE bookId = :bookId AND deletedAt IS NULL ORDER BY type, title") fun observeForBook(bookId: Long): Flow<List<Note>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(note: Note)
    @Delete suspend fun delete(note: Note)
}
