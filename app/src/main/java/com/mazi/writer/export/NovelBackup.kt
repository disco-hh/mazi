package com.mazi.writer.export

import android.content.ContentResolver
import android.net.Uri
import com.mazi.writer.data.*
import com.mazi.writer.data.WriterRepository.BackupPayload
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object NovelBackup {
    fun fileName(book: Book) = "${book.title.ifBlank { "未命名作品" }}-${LocalDate.now()}.novelzip"

    fun write(resolver: ContentResolver, uri: Uri, payload: BackupPayload) {
        val stream = requireNotNull(resolver.openOutputStream(uri)) { "无法创建备份文件" }
        ZipOutputStream(stream.buffered()).use { zip ->
            put(zip, "manifest.json", JSONObject().put("format", "mazi.novelzip").put("version", 1).put("createdAt", System.currentTimeMillis()).toString())
            put(zip, "book.json", payload.book.toJson().toString())
            put(zip, "volumes.json", JSONArray(payload.volumes.map { it.toJson() }).toString())
            put(zip, "research.json", JSONArray(payload.notes.map { it.toJson() }).toString())
            payload.chapters.forEachIndexed { index, chapter ->
                val slug = chapter.title.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(60)
                put(zip, "chapters/${(index + 1).toString().padStart(4, '0')}-$slug.md", chapter.toJson().toString())
            }
        }
    }

    private fun put(zip: ZipOutputStream, name: String, value: String) { zip.putNextEntry(ZipEntry(name)); zip.write(value.toByteArray(Charsets.UTF_8)); zip.closeEntry() }
    private fun Book.toJson() = JSONObject().put("id", id).put("title", title).put("author", author).put("summary", summary).put("genre", genre).put("targetWords", targetWords).put("dailyGoal", dailyGoal)
    private fun Volume.toJson() = JSONObject().put("id", id).put("title", title).put("position", position)
    private fun Chapter.toJson() = JSONObject().put("id", id).put("volumeId", volumeId).put("title", title).put("content", content).put("position", position).put("status", status.name).put("outline", outline).put("wordCount", wordCount)
    private fun Note.toJson() = JSONObject().put("id", id).put("title", title).put("detail", detail).put("type", type.name).put("tags", tags)
}
