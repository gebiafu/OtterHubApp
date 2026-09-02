# OtterHub Android 客户端开发计划

## 一、项目概述

基于 OtterHub 前端项目的所有 API 接口，设计一款简洁好看的 Android 网盘客户端。使用 **Kotlin + Jetpack Compose + Material Design 3**，遵循 MVVM 架构，确保代码简洁不臃肿。

## 二、技术选型

| 类别 | 选型 | 说明 |
|------|------|------|
| UI框架 | Jetpack Compose + Material 3 | 声明式UI，代码简洁 |
| 网络请求 | Retrofit 2 + OkHttp | 配合 Hono RPC 后端 |
| 图片加载 | Coil 3 (Compose) | 原生支持 Compose，轻量 |
| 视频/音频 | ExoPlayer (Media3) | 官方媒体播放库 |
| 本地存储 | DataStore Preferences | 存储网盘地址、密码等 |
| 架构 | MVVM + StateFlow | ViewModel + Repository 模式 |
| 导航 | Compose Navigation | 类型安全导航 |
| 文件操作 | SAF + 系统文件选择器 | 系统级文件访问 |

## 三、项目结构

```
OtterHubApp/
├── app/src/main/java/com/example/otterhub/
│   ├── OtterHubApp.kt                    # Application 类
│   ├── MainActivity.kt                    # 单 Activity 入口
│   │
│   ├── data/                              # 数据层
│   │   ├── api/
│   │   │   ├── OtterHubApi.kt            # Retrofit 接口定义
│   │   │   ├── AuthInterceptor.kt        # JWT Token 拦截器
│   │   │   ├── ApiResult.kt              # 统一响应封装
│   │   │   └── RetrofitClient.kt         # Retrofit 单例
│   │   ├── model/
│   │   │   ├── FileItem.kt               # 文件数据模型
│   │   │   ├── FileMetadata.kt           # 文件元数据
│   │   │   ├── ShareInfo.kt              # 分享链接数据
│   │   │   └── Settings.kt              # 设置数据模型
│   │   ├── repository/
│   │   │   ├── AuthRepository.kt         # 认证相关
│   │   │   ├── FileRepository.kt         # 文件操作
│   │   │   ├── UploadRepository.kt       # 上传（含分片）
│   │   │   └── ShareRepository.kt        # 分享链接
│   │   └── local/
│   │       └── PrefsManager.kt           # DataStore 偏好管理
│   │
│   ├── ui/                                # UI层
│   │   ├── theme/                         # M3 主题（Theme.kt, Color.kt, Type.kt）
│   │   ├── navigation/                    # 导航（NavGraph.kt, Screen.kt）
│   │   ├── screen/                        # 各页面
│   │   │   ├── SetupScreen.kt            # 首次配置网盘地址+密码
│   │   │   ├── LoginScreen.kt            # 登录页
│   │   │   ├── HomeScreen.kt             # 主页（文件网格/列表）
│   │   │   ├── FavoritesScreen.kt        # 收藏页
│   │   │   ├── TrashScreen.kt            # 回收站
│   │   │   ├── SettingsScreen.kt         # 设置页
│   │   │   ├── PreviewScreen.kt          # 文件预览
│   │   │   └── ShareScreen.kt            # 分享链接查看
│   │   ├── component/                     # 可复用组件
│   │   │   ├── FileCard.kt, FileGrid.kt, TopBar.kt, SearchBar.kt
│   │   │   ├── FilterChips.kt, UploadProgress.kt, EmptyState.kt
│   │   │   ├── FileDetailDialog.kt, ShareDialog.kt, BatchActionBar.kt
│   │   │   └── MediaPlayer.kt
│   │   └── viewmodel/                     # ViewModel 层
│   │       ├── FileViewModel.kt, UploadViewModel.kt
│   │       ├── PreviewViewModel.kt, SettingsViewModel.kt
│   │
│   └── util/
│       ├── FileUtils.kt                  # 文件类型判断、大小格式化
│       ├── DateUtils.kt                  # 日期格式化
│       └── Extensions.kt                # Kotlin 扩展函数
```

## 四、API 接口映射（对接 OtterHub 后端）

### 认证
- `POST /auth/login` → 密码登录，返回 JWT
- `POST /auth/logout` → 登出

### 文件操作
- `GET /file/list` → 分页文件列表（支持 fileType 筛选）
- `GET /file/:key/download` → 下载文件
- `GET /file/:key/thumb` → 缩略图
- `GET /file/:key` → 原文件（预览用）
- `PATCH /file/:key/meta` → 更新文件名/标签/描述
- `POST /file/:key/toggle-like` → 收藏切换
- `DELETE /file/:key` → 删除

### 上传
- `POST /upload` → 单文件上传
- `POST /upload/chunk/init` → 初始化分片
- `POST /upload/chunk` → 上传分片
- `GET /upload/chunk/progress` → 查询进度

### 分享
- `POST /share/create` → 创建分享
- `GET /share/list` → 分享列表
- `DELETE /share/revoke/:token` → 撤销

### 回收站
- `POST /trash/:key/move` → 移入回收站
- `POST /trash/:key/restore` → 恢复

## 五、核心页面设计

1. **SetupScreen**：首次打开，居中卡片输入网盘地址+密码，保存到 DataStore
2. **LoginScreen**：简洁密码输入，自动跳转主页
3. **HomeScreen**：顶部筛选栏（全部/图片/视频/音频/文档）+ 文件网格 + FAB上传
4. **PreviewScreen**：图片全屏缩放、视频ExoPlayer播放、音频播放控件
5. **FavoritesScreen**：仅显示收藏文件
6. **TrashScreen**：回收站列表 + 恢复/永久删除
7. **SettingsScreen**：网盘地址修改 + 退出登录

## 六、开发顺序

1. **Phase 1**：项目初始化 + 主题 + 导航 + 网络层 + DataStore
2. **Phase 2**：认证流程（SetupScreen → LoginScreen → Token管理）
3. **Phase 3**：文件浏览（HomeScreen + FileCard + 文件列表）
4. **Phase 4**：文件预览（图片 + 视频 + 音频）
5. **Phase 5**：文件操作（上传 + 下载 + 删除 + 收藏）
6. **Phase 6**：高级功能（分享 + 回收站 + 搜索 + 批量操作 + 设置）

## 七、目录位置

项目创建在：`E:\study\demo\OtterHubApp`