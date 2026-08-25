package com.mazi.writer.export

import android.content.ContentResolver
import android.net.Uri
import com.mazi.writer.data.*
import com.mazi.writer.data.WriterRepository.BackupPayload
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object NovelBackup {
    data class RestoredProject(val title: String, val author: String, val summary: String, val genre: String, val targetWords: Int, val dailyGoal: Int, val volumes: List<RestoredVolume>, val chapters: List<RestoredChapter>, val notes: List<RestoredNote>)
    data class RestoredVolume(val sourceId: Long, val title: String, val position: Int)
    data class RestoredChapter(val sourceVolumeId: Long?, val title: String, val content: String, val position: Int, val status: ChapterStatus, val outline: String)
    data class RestoredNote(val title: String, val detail: String, val type: NoteType, val tags: String)
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

    fun read(resolver: ContentResolver, uri: Uri): RestoredProject {
        val files = linkedMapOf<String, String>()
        requireNotNull(resolver.openInputStream(uri)) { "无法读取备份文件" }.use { input -> ZipInputStream(input.buffered()).use { zip ->
            while (true) { val entry = zip.nextEntry ?: break; if (!entry.isDirectory) files[entry.name] = zip.readBytes().toString(Charsets.UTF_8); zip.closeEntry() }
        } }
        val manifest = JSONObject(requireNotNull(files["manifest.json"]) { "不是有效的墨栖备份" })
        require(manifest.optString("format") == "mazi.novelzip") { "不支持的备份格式" }
        val book = JSONObject(requireNotNull(files["book.json"]) { "备份缺少作品信息" })
        val volumes = JSONArray(files["volumes.json"] ?: "[]").let { list -> List(list.length()) { index -> list.getJSONObject(index).let { RestoredVolume(it.optLong("id"), it.optString("title"), it.optInt("position")) } } }
        val notes = JSONArray(files["research.json"] ?: "[]").let { list -> List(list.length()) { index -> list.getJSONObject(index).let { RestoredNote(it.optString("title"), it.optString("detail"), NoteType.valueOf(it.optString("type", NoteType.SETTING.name)), it.optString("tags")) } } }
        val chapters = files.filterKeys { it.startsWith("chapters/") }.toSortedMap().values.map { raw -> JSONObject(raw).let { json -> RestoredChapter(json.takeIf { !it.isNull("volumeId") }?.optLong("volumeId"), json.optString("title"), json.optString("content"), json.optInt("position"), ChapterStatus.valueOf(json.optString("status", ChapterStatus.DRAFT.name)), json.optString("outline")) } }
        return RestoredProject(book.optString("title"), book.optString("author"), book.optString("summary"), book.optString("genre"), book.optInt("targetWords"), book.optInt("dailyGoal", 1000), volumes, chapters, notes)
    }

    private fun put(zip: ZipOutputStream, name: String, value: String) { zip.putNextEntry(ZipEntry(name)); zip.write(value.toByteArray(Charsets.UTF_8)); zip.closeEntry() }
    private fun Book.toJson() = JSONObject().put("id", id).put("title", title).put("author", author).put("summary", summary).put("genre", genre).put("targetWords", targetWords).put("dailyGoal", dailyGoal)
    private fun Volume.toJson() = JSONObject().put("id", id).put("title", title).put("position", position)
    private fun Chapter.toJson() = JSONObject().put("id", id).put("volumeId", volumeId).put("title", title).put("content", content).put("position", position).put("status", status.name).put("outline", outline).put("wordCount", wordCount)
    private fun Note.toJson() = JSONObject().put("id", id).put("title", title).put("detail", detail).put("type", type.name).put("tags", tags)
}
