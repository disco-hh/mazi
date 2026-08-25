package com.mazi.writer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazi.writer.data.Book
import com.mazi.writer.data.ChapterStatus
import com.mazi.writer.data.NoteType
import com.mazi.writer.WriterViewModel
import com.mazi.writer.export.BookExporter
import com.mazi.writer.export.ExportFormat

private val Ink = Color(0xFF171714)
private val Paper = Color(0xFFF5F1E9)
private val Muted = Color(0xFF756F66)
private val Moss = Color(0xFF577262)
private val Amber = Color(0xFFC58A45)

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
                    0 -> Bookshelf(viewModel, onOpenWriting = { tab = 1 })
                    1 -> Writing(onFocus = { focusMode = !focusMode }, focused = focusMode, viewModel = viewModel)
                    else -> Library(viewModel)
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

@Composable private fun Bookshelf(viewModel: WriterViewModel, onOpenWriting: () -> Unit) {
    val books by viewModel.booksWithStats.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> restoreUri = uri }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("书架", fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("所有故事都安静地留在这里", color = Muted) }; IconButton(onClick = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }) { Icon(Icons.Outlined.Restore, "恢复备份") } } }
        item { TodayCard() }
        item { Text("我的作品", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
        items(books, key = { it.book.id }) { item -> BookCard(item) { viewModel.selectBook(item.book.id); onOpenWriting() } }
        item { OutlinedButton(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("新建作品") } }
    }
    if (showCreate) CreateBookDialog(onDismiss = { showCreate = false }, onCreate = { viewModel.createBook(it); showCreate = false; onOpenWriting() })
    restoreUri?.let { uri -> AlertDialog(onDismissRequest = { restoreUri = null }, title = { Text("恢复备份？") }, text = { Text("将以“恢复副本”新建作品，不会覆盖当前书架中的内容。") }, confirmButton = { TextButton(onClick = { viewModel.restore(context.contentResolver, uri); restoreUri = null }) { Text("恢复副本") } }, dismissButton = { TextButton(onClick = { restoreUri = null }) { Text("取消") } }) }
}

@Composable private fun TodayCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(22.dp)) { Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("今日码字", color = Color(0xFFFFF9F0), fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(10.dp)); Text("682", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold); Text("/ 1,000 字", color = Color(0xFFBDB8AF)); Spacer(Modifier.height(12.dp)); LinearProgressIndicator(progress = { .682f }, color = Amber, trackColor = Color(0xFF393732), modifier = Modifier.fillMaxWidth().height(6.dp)) }; Spacer(Modifier.width(24.dp)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("7", color = Amber, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("连续天数", color = Color(0xFFBDB8AF), fontSize = 12.sp) } } }
}

@Composable private fun BookCard(item: com.mazi.writer.data.BookWithStats, onClick: () -> Unit) {
    val book = item.book
    Card(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)) { Row(Modifier.padding(14.dp).fillMaxWidth()) { Box(Modifier.size(width = 65.dp, height = 86.dp).background(Moss, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.AutoStories, null, tint = Color.White.copy(.85f)) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(book.title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Spacer(Modifier.height(5.dp)); Text("本地离线作品", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(14.dp)); Row(verticalAlignment = Alignment.CenterVertically) { LinearProgressIndicator(progress = { 0f }, color = Moss, trackColor = Paper, modifier = Modifier.weight(1f).height(5.dp)); Spacer(Modifier.width(10.dp)); Text("离线保存", fontSize = 12.sp, color = Muted) } } } }
}

@Composable private fun CreateBookDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("新建作品") }, text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("作品名称") }, singleLine = true) }, confirmButton = { TextButton(onClick = { onCreate(title.ifBlank { "未命名作品" }) }) { Text("创建") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

@Composable private fun Writing(onFocus: () -> Unit, focused: Boolean, viewModel: WriterViewModel) {
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val volumes by viewModel.volumes.collectAsStateWithLifecycle()
    val selected by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val activeBook by viewModel.activeBook.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingExport by remember { mutableStateOf<ExportFormat?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri -> pendingExport?.let { format -> uri?.let { viewModel.export(context.contentResolver, it, format) } }; pendingExport = null }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri -> uri?.let { viewModel.backup(context.contentResolver, it) } }
    val chapter = selected ?: chapters.firstOrNull()
    LaunchedEffect(chapter?.id) { chapter?.let { viewModel.selectChapter(it.id) } }
    var draft by remember(chapter?.id, chapter?.content) { mutableStateOf(chapter?.content.orEmpty()) }
    var showSearch by remember { mutableStateOf(false) }
    val isDark = focused
    val bg = if (isDark) Ink else Paper
    val fg = if (isDark) Color(0xFFF0ECE3) else Ink
    Column(Modifier.fillMaxSize().background(bg)) {
        if (!focused) EditorHeader(onFocus, { showSearch = true }, { format -> pendingExport = format; exportLauncher.launch(BookExporter.fileName(activeBook ?: Book(0, "未命名作品"), format)) }, { backupLauncher.launch(com.mazi.writer.export.NovelBackup.fileName(activeBook ?: Book(0, "未命名作品"))) }, chapter?.title.orEmpty(), chapter?.status ?: ChapterStatus.DRAFT, volumes, chapters, viewModel::selectChapter, viewModel::createChapter, viewModel::createVolume, viewModel::updateStatus)
        Text("章节写作", color = if (isDark) Color(0xFFA6B6AC) else Moss, fontSize = 13.sp, modifier = Modifier.padding(start = 28.dp, top = if (focused) 42.dp else 12.dp))
        Text(chapter?.title ?: "正在载入…", color = fg, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 28.dp, vertical = 7.dp))
        HorizontalDivider(color = fg.copy(.1f), modifier = Modifier.padding(horizontal = 28.dp))
        TextField(value = draft, onValueChange = { draft = it; viewModel.updateContent(it) }, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = fg, unfocusedTextColor = fg, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 35.sp), placeholder = { Text("开始写作…", color = fg.copy(.35f)) })
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onFocus) { Icon(if (focused) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen, "码字模式", tint = fg.copy(.7f)) }; Spacer(Modifier.width(4.dp)); Text("${chapter?.status?.label() ?: "草稿"} · ${draft.filterNot { it.isWhitespace() }.length} 字", color = fg.copy(.65f), fontSize = 13.sp); Spacer(Modifier.weight(1f)); Text("已自动保存", color = if (isDark) Color(0xFFA6B6AC) else Moss, fontSize = 12.sp) }
    }
    if (showSearch) SearchDialog(viewModel, onDismiss = { showSearch = false })
}

@Composable private fun EditorHeader(onFocus: () -> Unit, onSearch: () -> Unit, onExport: (ExportFormat) -> Unit, onBackup: () -> Unit, title: String, status: ChapterStatus, volumes: List<com.mazi.writer.data.Volume>, chapters: List<com.mazi.writer.data.Chapter>, onSelect: (Long) -> Unit, onCreate: (String) -> Unit, onCreateVolume: (String) -> Unit, onStatus: (ChapterStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var newChapter by remember { mutableStateOf(false) }
    var newVolume by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var exportExpanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box { IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.Menu, "章节目录") }; DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { val grouped = chapters.groupBy { it.volumeId }; volumes.forEach { volume -> Text(volume.title, color = Moss, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)); grouped[volume.id].orEmpty().forEach { chapter -> DropdownMenuItem(text = { Text("  ${chapter.title}") }, onClick = { onSelect(chapter.id); expanded = false }) } }; grouped[null].orEmpty().takeIf { it.isNotEmpty() }?.let { loose -> Text("未分卷章节", color = Moss, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)); loose.forEach { chapter -> DropdownMenuItem(text = { Text(chapter.title) }, onClick = { onSelect(chapter.id); expanded = false }) } }; HorizontalDivider(); DropdownMenuItem(text = { Text("新建卷") }, leadingIcon = { Icon(Icons.Outlined.CreateNewFolder, null) }, onClick = { expanded = false; newVolume = true }); DropdownMenuItem(text = { Text("新建章节") }, leadingIcon = { Icon(Icons.Outlined.Add, null) }, onClick = { expanded = false; newChapter = true }) } }
        Text(title.ifBlank { "码字" }, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, "搜索当前作品") }
        Box { AssistChip(onClick = { statusExpanded = true }, label = { Text(status.label()) }); DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) { ChapterStatus.entries.forEach { item -> DropdownMenuItem(text = { Text(item.label()) }, onClick = { onStatus(item); statusExpanded = false }) } } }
        Box { IconButton(onClick = { exportExpanded = true }) { Icon(Icons.Outlined.FileDownload, "导出") }; DropdownMenu(expanded = exportExpanded, onDismissRequest = { exportExpanded = false }) { ExportFormat.entries.forEach { format -> DropdownMenuItem(text = { Text("导出 ${if (format == ExportFormat.TXT) "TXT" else "Markdown"}") }, onClick = { onExport(format); exportExpanded = false }) } } }
        IconButton(onClick = onBackup) { Icon(Icons.Outlined.Backup, "完整备份") }
        IconButton(onClick = onFocus) { Icon(Icons.Outlined.Fullscreen, "码字模式") }
    }
    if (newChapter) CreateBookDialog(onDismiss = { newChapter = false }, onCreate = { onCreate(it); newChapter = false })
    if (newVolume) CreateBookDialog(onDismiss = { newVolume = false }, onCreate = { onCreateVolume(it); newVolume = false })
}

@Composable private fun SearchDialog(viewModel: WriterViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var ignoreCase by remember { mutableStateOf(true) }
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    AlertDialog(onDismissRequest = onDismiss, title = { Text("搜索与替换") }, text = { Column { OutlinedTextField(value = query, onValueChange = { query = it; viewModel.search(it) }, label = { Text("查找") }, singleLine = true); OutlinedTextField(value = replacement, onValueChange = { replacement = it }, label = { Text("替换为") }, singleLine = true); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = ignoreCase, onCheckedChange = { ignoreCase = it }); Text("不区分大小写") }; if (query.isNotBlank()) Text("找到 ${results.size} 个章节", color = Muted, fontSize = 13.sp); results.take(4).forEach { chapter -> TextButton(onClick = { viewModel.selectChapter(chapter.id); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text(chapter.title) } } } }, confirmButton = { Row { TextButton(onClick = { viewModel.replaceAll(query, replacement, ignoreCase); onDismiss() }, enabled = query.isNotBlank()) { Text("全部替换") }; TextButton(onClick = onDismiss) { Text("关闭") } } }, dismissButton = { TextButton(onClick = { viewModel.undoReplace(); onDismiss() }) { Text("撤销上次替换") } })
}

private fun ChapterStatus.label() = when (this) { ChapterStatus.DRAFT -> "草稿"; ChapterStatus.REVISING -> "修改中"; ChapterStatus.DONE -> "完成" }

@Composable private fun Library(viewModel: WriterViewModel) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("资料库", fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("让故事里的每件事都有来处", color = Muted, modifier = Modifier.padding(top = 3.dp, bottom = 18.dp)) }; item { Text("当前作品 · ${notes.size} 张资料卡", color = Moss, fontSize = 13.sp) }; items(notes, key = { it.id }) { NoteCard(NoteUi(it.title, it.detail, it.type)) }; item { FilledTonalButton(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("新建资料卡") } } }
    if (showCreate) CreateNoteDialog(onDismiss = { showCreate = false }, onCreate = { title, detail, type -> viewModel.createNote(title, detail, type); showCreate = false })
}

@Composable private fun CreateNoteDialog(onDismiss: () -> Unit, onCreate: (String, String, NoteType) -> Unit) {
    var title by remember { mutableStateOf("") }; var detail by remember { mutableStateOf("") }; var type by remember { mutableStateOf(NoteType.CHARACTER) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("新建资料卡") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("名称") }, singleLine = true); OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("描述") }); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { NoteType.entries.forEach { item -> FilterChip(selected = type == item, onClick = { type = item }, label = { Text(item.label()) }) } } } }, confirmButton = { TextButton(onClick = { onCreate(title, detail, type) }) { Text("保存") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

private fun NoteType.label() = when (this) { NoteType.OUTLINE -> "大纲"; NoteType.CHARACTER -> "人物"; NoteType.PLACE -> "地点"; NoteType.SETTING -> "设定"; NoteType.TIMELINE -> "时间线" }

@Composable private fun NoteCard(note: NoteUi) { val (icon, color, type) = when(note.type) { NoteType.OUTLINE -> Triple(Icons.Outlined.FormatListBulleted, Moss, "大纲"); NoteType.CHARACTER -> Triple(Icons.Outlined.Person, Moss, "人物"); NoteType.PLACE -> Triple(Icons.Outlined.Place, Amber, "地点"); NoteType.SETTING -> Triple(Icons.Outlined.AutoAwesome, Color(0xFF75728B), "设定"); NoteType.TIMELINE -> Triple(Icons.Outlined.Schedule, Color(0xFF64748B), "时间线") }; Card(shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(color = color.copy(.13f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(44.dp)) { Icon(icon, null, tint = color, modifier = Modifier.padding(11.dp)) }; Spacer(Modifier.width(14.dp)); Column { Text(note.title, fontWeight = FontWeight.SemiBold); Text(note.detail, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(type, color = color, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) } } } }
