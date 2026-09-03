# OtterHubApp 修复计划与进度

> 目标：修复 Android 客户端（OtterHubApp）相对 Web 端（OtterHub）缺失/损坏的 5 个问题。
> 图例：`[ ]` 待处理 · `[~]` 进行中 · `[x]` 已完成

## 问题 1 — 图片无法预览 & 菜单栏显示回收站文件
- [x] 1.1 `FileViewModel` 列表过滤掉 `trash:` 与未完成分片文件（根因：`/file/list` 不带 fileType 时返回全部 key，含回收站）
- [x] 1.2 `FileCard` 图片缩略图改用原始文件 URL（不再用空的 `thumbUrl`）
- [x] 1.3 `PreviewScreen/ViewModel` 从 key 推导文件类型，按类型前缀 + limit 1000 稳定加载文件信息
- [x] 1.4 配置 Coil ImageLoader 复用 OkHttpClient（携带 auth Cookie），修复受保护图片加载

## 问题 2 — 缺少右键/上下文菜单
- [x] 2.1 `FileCard` 增加长按 + 更多按钮触发
- [x] 2.2 新建 `FileActionsMenu`（查看/分享/复制链接/编辑/下载/详情/删除）
- [x] 2.3 `HomeScreen` / `FavoritesScreen` 接入上下文菜单

## 问题 3 — 无法上传文件
- [x] 3.1 修复 `uploadFile` 响应模型（后端 `data` 是 String 而非 UploadResult）
- [x] 3.2 `HomeScreen` 接入 UploadViewModel + 解析文件名 + 进度展示 + 成功后刷新
- [x] 3.3 `NavGraph` 移除上传 TODO 桩

## 问题 4 — 缺少右下角悬浮菜单设置项
- [x] 4.1 `HomeScreen` FAB 改为可展开菜单（上传 / 回收站 / 系统设置 / 退出登录）
- [x] 4.2 `NavGraph` 提供退出登录回调

## 问题 5 — 回收站恢复后文件仍在列表
- [x] 5.1 `TrashViewModel` 恢复/永久删除成功后重新拉取回收站（并改用 fileType=trash 拉取）
- [x] 5.2 `TrashScreen` 增加错误提示（Snackbar）

## 备注
- 全部改动集中在 `OtterHubApp`（Android 客户端），未改动 Web 端 `OtterHub`。
- 构建验证未完成：本地 `gradlew` 触发 Gradle 发行版下载超时（沙箱网络受限），建议在 Android Studio 中同步并编译确认。
