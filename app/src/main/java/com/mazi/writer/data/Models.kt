package com.mazi.writer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChapterStatus { DRAFT, REVISING, DONE }
enum class NoteType { CHARACTER, PLACE, SETTING }

@Entity(tableName = "books")
data class Book(@PrimaryKey val id: Long, val title: String, val subtitle: String = "", val updatedAt: Long = System.currentTimeMillis(), val goal: Int = 1000)

@Entity(tableName = "chapters")
data class Chapter(@PrimaryKey val id: Long, val bookId: Long, val title: String, val content: String, val position: Int, val status: ChapterStatus = ChapterStatus.DRAFT)

@Entity(tableName = "notes")
data class Note(@PrimaryKey val id: Long, val bookId: Long, val title: String, val detail: String, val type: NoteType)
