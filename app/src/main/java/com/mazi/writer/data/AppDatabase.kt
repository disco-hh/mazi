package com.mazi.writer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun chapterStatus(value: String) = ChapterStatus.valueOf(value)
    @TypeConverter fun chapterStatus(status: ChapterStatus) = status.name
    @TypeConverter fun noteType(value: String) = NoteType.valueOf(value)
    @TypeConverter fun noteType(type: NoteType) = type.name
}

@androidx.room.TypeConverters(Converters::class)
@Database(entities = [Book::class, Chapter::class, Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun noteDao(): NoteDao
}
