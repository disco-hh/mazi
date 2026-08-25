package com.mazi.writer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazi.writer.data.ChapterStatus
import com.mazi.writer.data.NoteType
import com.mazi.writer.WriterViewModel

private val Ink = Color(0xFF171714)
private val Paper = Color(0xFFF5F1E9)
private val Muted = Color(0xFF756F66)
private val Moss = Color(0xFF577262)
private val Amber = Color(0xFFC58A45)

private data class ChapterUi(val title: String, val preview: String, val words: Int, val status: ChapterStatus)
private data class BookUi(val title: String, val detail: String, val words: String, val progress: Float, val tint: Color)
private data class NoteUi(val title: String, val detail: String, val type: NoteType)

@Composable
fun MaziApp(viewModel: WriterViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var focusMode by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = lightColorScheme(primary = Moss, background = Paper, surface = Color.White, onSurface = Ink)) {
        Scaffold(
            containerColor = if (focusMode) Ink else Paper,
            bottomBar = { AnimatedVisibility(!focusMode) { Navigation(tab, onSelect = { tab = it }) } }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(if (focusMode) PaddingValues(0.dp) else pad)) {
                when (tab) {
                    0 -> Bookshelf(onOpenWriting = { tab = 1 })
                    1 -> Writing(onFocus = { focusMode = !focusMode }, focused = focusMode, viewModel = viewModel)
                    else -> Library()
                }
            }
        }
    }
}

@Composable private fun Navigation(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = Color(0xFFFFFCF7), tonalElevation = 0.dp) {
        listOf(Icons.Outlined.MenuBook to "书架", Icons.Outlined.EditNote to "写作", Icons.Outlined.CollectionsBookmark to "资料库").forEachIndexed { i, (icon, name) ->
            NavigationBarItem(selected = selected == i, onClick = { onSelect(i) }, icon = { Icon(icon, name) }, label = { Text(name) })
        }
    }
}

@Composable private fun Bookshelf(onOpenWriting: () -> Unit) {
    val books = listOf(
        BookUi("长夜与微光", "上次编辑于 今天 22:18", "34,286 字", .68f, Color(0xFF5D756B)),
        BookUi("冬日来信", "上次编辑于 昨天", "12,420 字", .35f, Color(0xFF997252)),
        BookUi("未命名故事", "创建于 8 月 21 日", "0 字", 0f, Color(0xFF7A788D))
    )
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("书架", fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("所有故事都安静地留在这里", color = Muted) }; IconButton(onClick = {}) { Icon(Icons.Outlined.MoreHoriz, "更多") } } }
        item { TodayCard() }
        item { Text("我的作品", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
        items(books) { BookCard(it) }
        item { OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("新建作品") } }
    }
}

@Composable private fun TodayCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(22.dp)) { Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("今日码字", color = Color(0xFFFFF9F0), fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(10.dp)); Text("682", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold); Text("/ 1,000 字", color = Color(0xFFBDB8AF)); Spacer(Modifier.height(12.dp)); LinearProgressIndicator(progress = { .682f }, color = Amber, trackColor = Color(0xFF393732), modifier = Modifier.fillMaxWidth().height(6.dp)) }; Spacer(Modifier.width(24.dp)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("7", color = Amber, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("连续天数", color = Color(0xFFBDB8AF), fontSize = 12.sp) } } }
}

@Composable private fun BookCard(book: BookUi) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)) { Row(Modifier.padding(14.dp).fillMaxWidth()) { Box(Modifier.size(width = 65.dp, height = 86.dp).background(book.tint, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.AutoStories, null, tint = Color.White.copy(.85f)) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(book.title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Spacer(Modifier.height(5.dp)); Text(book.detail, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(14.dp)); Row(verticalAlignment = Alignment.CenterVertically) { LinearProgressIndicator(progress = { book.progress }, color = book.tint, trackColor = Paper, modifier = Modifier.weight(1f).height(5.dp)); Spacer(Modifier.width(10.dp)); Text(book.words, fontSize = 12.sp, color = Muted) } } } }
}

@Composable private fun Writing(onFocus: () -> Unit, focused: Boolean, viewModel: WriterViewModel) {
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val selected by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val chapter = selected ?: chapters.firstOrNull()
    LaunchedEffect(chapter?.id) { chapter?.let { viewModel.selectChapter(it.id) } }
    var draft by remember(chapter?.id, chapter?.content) { mutableStateOf(chapter?.content.orEmpty()) }
    val isDark = focused
    val bg = if (isDark) Ink else Paper
    val fg = if (isDark) Color(0xFFF0ECE3) else Ink
    Column(Modifier.fillMaxSize().background(bg)) {
        if (!focused) EditorHeader(onFocus)
        Text("第一卷 · 归途", color = if (isDark) Color(0xFFA6B6AC) else Moss, fontSize = 13.sp, modifier = Modifier.padding(start = 28.dp, top = if (focused) 42.dp else 12.dp))
        Text("第一章 雨落之前", color = fg, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 28.dp, vertical = 7.dp))
        HorizontalDivider(color = fg.copy(.1f), modifier = Modifier.padding(horizontal = 28.dp))
        TextField(value = draft, onValueChange = { draft = it; viewModel.updateContent(it) }, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = fg, unfocusedTextColor = fg, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 35.sp), placeholder = { Text("开始写作…", color = fg.copy(.35f)) })
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onFocus) { Icon(if (focused) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen, "码字模式", tint = fg.copy(.7f)) }; Spacer(Modifier.width(4.dp)); Text("${draft.filterNot { it.isWhitespace() }.length} 字", color = fg.copy(.65f), fontSize = 13.sp); Spacer(Modifier.weight(1f)); Text("已自动保存", color = if (isDark) Color(0xFFA6B6AC) else Moss, fontSize = 12.sp) }
    }
}

@Composable private fun EditorHeader(onFocus: () -> Unit) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = {}) { Icon(Icons.Outlined.Menu, "章节目录") }; Text("长夜与微光", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); IconButton(onClick = {}) { Icon(Icons.Outlined.Search, "搜索") }; IconButton(onClick = onFocus) { Icon(Icons.Outlined.Fullscreen, "码字模式") } } }

@Composable private fun Library() {
    val notes = listOf(NoteUi("林晚", "二十七岁，旧书店店主。", NoteType.CHARACTER), NoteUi("雾港", "终年多雨的海边小城。", NoteType.PLACE), NoteUi("归信", "每逢雨夜出现的无名来信。", NoteType.SETTING))
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("资料库", fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("让故事里的每件事都有来处", color = Muted, modifier = Modifier.padding(top = 3.dp, bottom = 18.dp)) }; item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = true, onClick = {}, label = { Text("全部  3") }); FilterChip(selected = false, onClick = {}, label = { Text("人物") }); FilterChip(selected = false, onClick = {}, label = { Text("地点") }) } }; items(notes) { NoteCard(it) }; item { FilledTonalButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("新建资料卡") } } }
}

@Composable private fun NoteCard(note: NoteUi) { val (icon, color, type) = when(note.type) { NoteType.CHARACTER -> Triple(Icons.Outlined.Person, Moss, "人物"); NoteType.PLACE -> Triple(Icons.Outlined.Place, Amber, "地点"); NoteType.SETTING -> Triple(Icons.Outlined.AutoAwesome, Color(0xFF75728B), "设定") }; Card(shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(color = color.copy(.13f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(44.dp)) { Icon(icon, null, tint = color, modifier = Modifier.padding(11.dp)) }; Spacer(Modifier.width(14.dp)); Column { Text(note.title, fontWeight = FontWeight.SemiBold); Text(note.detail, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(type, color = color, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) } } } }
