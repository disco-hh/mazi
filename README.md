# 码字（Mazi）

一个面向小说创作的离线 Android 写作器原型。界面围绕书架、专注写作、资料库三个入口设计，默认不依赖网络。

## 当前实现

- Jetpack Compose 沉浸式界面与暗色「码字模式」
- 书架、每日字数目标与连续写作展示
- 写作编辑器、实时字数与自动保存状态提示
- 人物、地点、设定资料卡界面
- Room 实体模型：作品、章节、资料卡

## 下一步

1. 为 Room 补齐 DAO、Repository 与 ViewModel，并接入真实持久化。
2. 连接章节目录、状态筛选、拖拽排序、搜索替换。
3. 接入 Storage Access Framework，完成用户指定目录的备份及 TXT / Markdown / DOCX 导出。

## 构建

使用 Android Studio 打开本项目，在首次同步时下载 Android Gradle Plugin 和依赖；需要 JDK 17 与 Android SDK 35。
