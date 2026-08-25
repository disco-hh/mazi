package com.mazi.writer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter fun chapterStatus(value: String) = ChapterStatus.valueOf(value)
    @TypeConverter fun chapterStatus(status: ChapterStatus) = status.name
    @TypeConverter fun noteType(value: String) = NoteType.valueOf(value)
    @TypeConverter fun noteType(type: NoteType) = type.name
}

@androidx.room.TypeConverters(Converters::class)
@Database(entities = [Book::class, Volume::class, Chapter::class, Note::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun volumeDao(): VolumeDao
    abstract fun chapterDao(): ChapterDao
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN author TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE books ADD COLUMN summary TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE books ADD COLUMN genre TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE books ADD COLUMN targetWords INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN coverStyle INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN dailyGoal INTEGER NOT NULL DEFAULT 1000")
                db.execSQL("ALTER TABLE books ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN lastOpenedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN deletedAt INTEGER")
                db.execSQL("UPDATE books SET createdAt = updatedAt, lastOpenedAt = updatedAt, dailyGoal = goal")
                db.execSQL("CREATE TABLE IF NOT EXISTS volumes (id INTEGER NOT NULL, bookId INTEGER NOT NULL, title TEXT NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("ALTER TABLE chapters ADD COLUMN volumeId INTEGER")
                db.execSQL("ALTER TABLE chapters ADD COLUMN outline TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chapters ADD COLUMN wordCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE chapters ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE chapters ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE chapters ADD COLUMN deletedAt INTEGER")
                db.execSQL("UPDATE chapters SET wordCount = length(content), createdAt = 0, updatedAt = 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER")
            }
        }
    }
}
