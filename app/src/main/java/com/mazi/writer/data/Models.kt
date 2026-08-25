package com.mazi.writer.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded

enum class ChapterStatus { DRAFT, REVISING, DONE }
enum class NoteType { OUTLINE, CHARACTER, PLACE, SETTING, TIMELINE }

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: Long,
    val title: String,
    /** Kept for v1 database compatibility; use summary for new UI. */
    val subtitle: String = "",
    /** Kept for v1 database compatibility; use dailyGoal for new UI. */
    val goal: Int = 1000,
    val author: String = "",
    val summary: String = "",
    val genre: String = "",
    val targetWords: Int = 0,
    val coverStyle: Int = 0,
    val dailyGoal: Int = 1000,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(tableName = "volumes")
data class Volume(
    @PrimaryKey val id: Long,
    val bookId: Long,
    val title: String,
    val position: Int
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey val id: Long,
    val bookId: Long,
    val volumeId: Long? = null,
    val title: String,
    val content: String,
    val position: Int,
    val status: ChapterStatus = ChapterStatus.DRAFT,
    val outline: String = "",
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: Long,
    val bookId: Long,
    val title: String,
    val detail: String,
    val type: NoteType,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

data class BookWithStats(@Embedded val book: Book, val totalWords: Int)
