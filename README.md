# OtterHub Android 客户端

基于 [OtterHub](https://github.com/DJChanahCJD/OtterHub) 的 Android 网盘客户端应用。

## 功能特性

- ✅ 首次配置网盘地址和密码
- ✅ JWT 认证登录
- ✅ 文件网格/列表浏览
- ✅ 文件类型筛选（图片/视频/音频/文档）
- ✅ 图片预览（支持缩放）
- ✅ 收藏/取消收藏
- ✅ 文件删除/移入回收站
- ✅ 回收站管理（恢复/永久删除）
- ✅ 文件上传（支持分片上传大文件）
- ✅ 分享链接管理
- ✅ 搜索功能
- ✅ 设置页面

## 技术栈

| 类别 | 技术 |
|------|------|
| 开发语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material Design 3 |
| 网络请求 | Retrofit 2 + OkHttp |
| 图片加载 | Coil 3 |
| 视频/音频 | ExoPlayer (Media3) |
| 本地存储 | DataStore Preferences |
| 架构模式 | MVVM + StateFlow |
| 导航 | Compose Navigation |

## 项目结构

```
app/src/main/java/com/example/otterhub/
├── OtterHubApp.kt              # Application
├── MainActivity.kt              # 单 Activity 入口
├── data/                        # 数据层
│   ├── api/                     # Retrofit 接口
│   │   ├── OtterHubApi.kt
│   │   └── RetrofitClient.kt
│   ├── model/                   # 数据模型
│   │   └── Models.kt
│   ├── repository/              # 仓库层
│   │   ├── AuthRepository.kt
│   │   ├── FileRepository.kt
│   │   ├── UploadRepository.kt
│   │   └── ShareRepository.kt
│   └── local/                   # 本地存储
│       └── PrefsManager.kt
├── ui/                          # UI 层
│   ├── theme/                   # Material 3 主题
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── navigation/              # 导航配置
│   │   ├── Screen.kt
│   │   └── NavGraph.kt
│   ├── screen/                  # 页面
│   │   ├── SetupScreen.kt      # 首次配置
│   │   ├── LoginScreen.kt      # 登录
│   │   ├── HomeScreen.kt       # 主页
│   │   ├── FavoritesScreen.kt  # 收藏
│   │   ├── TrashScreen.kt      # 回收站
│   │   ├── PreviewScreen.kt    # 文件预览
│   │   ├── SettingsScreen.kt   # 设置
│   │   └── ShareScreen.kt      # 分享管理
│   ├── component/               # 可复用组件
│   │   ├── FileCard.kt
│   │   ├── FilterChips.kt
│   │   ├── EmptyState.kt
│   │   ├── UploadProgress.kt
│   │   └── ShareDialog.kt
│   └── viewmodel/               # ViewModel
│       ├── FileViewModel.kt
│       ├── UploadViewModel.kt
│       ├── PreviewViewModel.kt
│       ├── TrashViewModel.kt
│       └── SettingsViewModel.kt
└── util/                        # 工具类
    └── FileUtils.kt
```

## 构建指南

### 环境要求

- **Android Studio**: Hedgehog 2023.1.1 或更高版本
- **JDK**: 17 或更高版本
- **Android SDK**: compileSdk 35, minSdk 26, targetSdk 35
- **Gradle**: 8.2+ (项目包含 wrapper)

### 构建步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd OtterHubApp
   ```

2. **用 Android Studio 打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 选择 `OtterHubApp` 目录
   - 等待 Gradle 同步完成

3. **构建 Debug APK**
   ```bash
   # Windows
   gradlew.bat assembleDebug

   # macOS/Linux
   ./gradlew assembleDebug
   ```

4. **APK 输出位置**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### 使用 Android Studio 构建

1. 连接 Android 设备或启动模拟器
2. 点击工具栏 ▶️ 按钮运行
3. 或选择 `Build > Build Bundle(s) / APK(s) > Build APK(s)`

## API 接口

本应用对接 OtterHub 后端 API：

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `POST /auth/login` | 密码登录 |
| 认证 | `POST /auth/logout` | 登出 |
| 文件 | `GET /file/list` | 文件列表 |
| 文件 | `GET /file/{key}` | 获取文件 |
| 文件 | `GET /file/{key}/thumb` | 缩略图 |
| 文件 | `GET /file/{key}/download` | 下载 |
| 文件 | `PATCH /file/{key}/meta` | 更新元数据 |
| 文件 | `POST /file/{key}/toggle-like` | 收藏切换 |
| 文件 | `DELETE /file/{key}` | 删除 |
| 上传 | `POST /upload` | 单文件上传 |
| 上传 | `POST /upload/chunk/init` | 初始化分片 |
| 上传 | `POST /upload/chunk` | 上传分片 |
| 分享 | `POST /share/create` | 创建分享 |
| 分享 | `GET /share/list` | 分享列表 |
| 分享 | `DELETE /share/revoke/{token}` | 撤销分享 |
| 回收站 | `POST /trash/{key}/move` | 移入回收站 |
| 回收站 | `POST /trash/{key}/restore` | 恢复文件 |

## 首次使用

1. 启动应用后进入配置页面
2. 输入网盘服务器地址（如：`https://your-otterhub.pages.dev`）
3. 输入密码
4. 点击"连接并保存"
5. 自动跳转到主页，开始浏览文件

## 开发说明

### 添加新功能

1. 在 `data/api/OtterHubApi.kt` 添加 API 接口
2. 在 `data/repository/` 添加对应 Repository
3. 在 `ui/viewmodel/` 添加 ViewModel
4. 在 `ui/screen/` 添加页面
5. 在 `ui/navigation/NavGraph.kt` 添加路由

### 主题定制

修改 `ui/theme/Color.kt` 中的颜色值即可更换主题色：
- `md_theme_light_primary`: 亮色主题主色
- `md_theme_dark_primary`: 暗色主题主色

## License

MIT
