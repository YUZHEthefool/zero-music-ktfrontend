# Zero Music Android 客户端

基于 Kotlin + Jetpack Compose 的音乐播放器 Android 应用。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **架构**: MVVM
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp
- **音频播放**: ExoPlayer (Media3)
- **异步**: Coroutines + Flow

## 开发进度

### ✅ Phase 1: 基础框架搭建
- Gradle 配置
- Application 类和依赖注入
- UI 主题系统
- 数据模型和网络层
- Repository 模式

### ✅ Phase 2: 播放列表功能
- PlaylistViewModel (状态管理)
- PlaylistScreen UI
- 加载状态、错误处理
- 下拉刷新功能

### ✅ Phase 3: 音频播放功能
- ExoPlayer 完整集成
- PlayerState 状态管理
- MusicPlayer 接口和实现
- PlayerViewModel
- 播放控制 (播放/暂停/上一曲/下一曲)
- 进度跟踪和跳转
- 循环模式 (不循环/列表循环/单曲循环)
- 随机播放

### ✅ Phase 4: 完整播放器 UI
- MiniPlayerBar (迷你播放器)
- FullPlayerScreen (全屏播放器)
- 可拖动进度条
- 专辑封面展示
- 播放控制按钮
- 循环和随机切换
- 导航集成

### ✅ Phase 5: 后台播放与通知
- MusicPlaybackService (前台服务)
- MediaSession 集成
- 播放通知系统
- 通知控制按钮
- 锁屏控制
- 服务生命周期管理

### 🔄 Phase 6: 优化与测试 (进行中)
- 文档更新
- 错误处理优化
- 性能优化
- 代码质量提升
- 测试覆盖

## 运行项目

### 前置条件

1. **启动后端服务器**
   ```bash
   cd ../backend
   go run main.go
   ```
   后端将在 `http://localhost:8080` 运行

2. **配置服务器地址**
   
   在 `app/src/main/java/com/zeromusic/util/Constants.kt` 中:
   ```kotlin
   const val BASE_URL = "http://10.0.2.2:8080"  // 模拟器
   // 或
   const val BASE_URL = "http://192.168.x.x:8080"  // 真机 (替换为实际 IP)
   ```

### 构建和运行

1. **使用 Android Studio**
   - 打开 `android` 目录
   - 等待 Gradle 同步完成
   - 点击 Run 按钮

2. **使用命令行**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 项目结构

```
app/src/main/java/com/zeromusic/
├── data/
│   ├── model/          # 数据模型 (Song, PlayerState, PlaybackInfo等)
│   ├── remote/         # API 服务接口
│   └── repository/     # 数据仓库
├── di/                 # 依赖注入模块 (NetworkModule, PlayerModule)
├── player/             # 音频播放器
│   ├── MusicPlayer.kt         # 播放器接口
│   └── ExoPlayerMusicPlayer.kt # ExoPlayer实现
├── service/            # 后台服务
│   └── MusicPlaybackService.kt # 前台播放服务
├── ui/
│   ├── playlist/       # 播放列表界面
│   ├── player/         # 播放器界面
│   │   ├── MiniPlayerBar.kt      # 迷你播放器
│   │   ├── FullPlayerScreen.kt   # 全屏播放器
│   │   └── PlayerViewModel.kt    # 播放器ViewModel
│   └── theme/          # UI 主题
├── util/               # 工具类
├── MainActivity.kt     # 主 Activity
└── ZeroMusicApplication.kt  # Application 类
```

## API 端点

后端提供以下 API:

- `GET /api/songs` - 获取所有歌曲
- `GET /api/song/:id` - 获取单首歌曲详情
- `GET /api/stream/:id` - 流式播放音频

## 当前功能

### ✅ 已实现

**播放列表功能**:
- 显示音乐列表
- 下拉刷新
- 加载状态显示
- 错误处理和重试
- 歌曲信息展示 (标题、艺术家、专辑、时长)

**音频播放功能**:
- ExoPlayer 音频播放
- 播放/暂停/停止控制
- 上一曲/下一曲
- 进度跟踪和跳转
- 3种循环模式 (不循环/列表循环/单曲循环)
- 随机播放

**播放器 UI**:
- 迷你播放器 (底部栏)
- 全屏播放器界面
- 可拖动进度条
- 专辑封面展示
- 完整播放控制

**后台播放**:
- 前台服务支持
- 媒体通知显示
- 通知控制按钮
- 锁屏控制
- MediaSession 集成

### 🔄 Phase 6 进行中
- 文档更新和完善
- 代码优化
- 性能提升
- 测试覆盖

## 注意事项

1. **网络权限**: 已在 AndroidManifest.xml 中声明
2. **最低 Android 版本**: Android 8.0 (API 26)
3. **目标 Android 版本**: Android 14 (API 34)
4. **模拟器网络**: 使用 `10.0.2.2` 访问本机 localhost
5. **真机测试**: 确保设备和开发机在同一网络,使用实际 IP 地址

## 故障排除

### 无法加载歌曲列表
1. 确认后端服务器正在运行
2. 检查 `Constants.BASE_URL` 配置是否正确
3. 查看 Logcat 中的网络请求日志

### 编译错误
1. 清理并重建项目: `./gradlew clean build`
2. 删除 `.gradle` 目录并重新同步

## 许可证

MIT License