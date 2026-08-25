package com.mazi.writer.export

import android.content.ContentResolver
import android.net.Uri
import com.mazi.writer.data.Book
import com.mazi.writer.data.Chapter

enum class ExportFormat(val extension: String, val mimeType: String) { TXT("txt", "text/plain"), MARKDOWN("md", "text/markdown") }

object BookExporter {
    fun fileName(book: Book, format: ExportFormat) = "${book.title.ifBlank { "未命名作品" }}.${format.extension}"

    fun render(book: Book, chapters: List<Chapter>, format: ExportFormat): String = buildString {
        when (format) {
            ExportFormat.TXT -> {
                appendLine(book.title)
                book.author.takeIf { it.isNotBlank() }?.let { appendLine(it) }
                appendLine()
                chapters.forEach { chapter -> appendLine(chapter.title); appendLine(); appendLine(chapter.content.trim()); appendLine(); appendLine() }
            }
            ExportFormat.MARKDOWN -> {
                appendLine("# ${book.title}")
                book.author.takeIf { it.isNotBlank() }?.let { appendLine(); appendLine("作者：$it") }
                chapters.forEach { chapter -> appendLine(); appendLine("## ${chapter.title}"); appendLine(); appendLine(chapter.content.trim()) }
            }
        }
    }

    fun write(resolver: ContentResolver, uri: Uri, content: String) {
        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8).use { writer ->
            requireNotNull(writer) { "无法打开导出文件" }
            writer.write(content)
        }
    }
}
