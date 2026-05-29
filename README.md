# AutoVolume - 自动音量调节

根据环境噪音自动调节媒体音量的 Android 应用。

## 功能特性

- 🎤 **实时噪音检测** — AudioRecord 采集麦克风数据，计算 dB 分贝值
- 🔊 **自动音量调节** — 根据噪音-音量映射曲线自动调节 STREAM_MUSIC
- 💤 **智能节能** — 屏幕关闭自动降低检测频率

## 技术栈

| 项目 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM |
| 异步 | Coroutines + Flow/StateFlow |
| 存储 | DataStore Preferences |
| minSdk | 29 (Android 10) |
| targetSdk | 35 (Android 15) |

## 项目结构

```
app/src/main/java/com/autovolume/
├── audio/          # 音频采集与分析
│   └── AudioAnalyzer.kt
├── volume/         # 音量控制
│   └── VolumeController.kt
├── headset/        # 耳机检测
│   └── HeadsetDetector.kt
├── service/        # 前台服务
│   └── AutoVolumeService.kt
├── receiver/       # 开机广播
│   └── BootReceiver.kt
├── datastore/      # 数据持久化
│   └── SettingsDataStore.kt
├── model/          # 数据模型
│   ├── AppSettings.kt
│   └── DetectionResult.kt
├── ui/             # UI 层
│   ├── MainViewModel.kt
│   ├── theme/Theme.kt
│   ├── components/DbMeter.kt
│   └── screens/ (Home/Settings/Advanced)
├── util/           # 工具类
│   └── PermissionHelper.kt
├── AutoVolumeApp.kt
└── MainActivity.kt
```

## 编译运行

1. 用 Android Studio 打开 `AutoVolume` 目录
2. 等待 Gradle Sync 完成
3. 连接手机或启动模拟器
4. 点击 Run ▶️

## 使用说明

1. 首次启动会申请录音和通知权限
2. 在主页开启"自动音量调节"开关
3. 连接耳机（蓝牙或有线）
4. 选择运行模式（推荐"平衡模式"）
5. 在设置中调整音量映射曲线和各项参数

## 权限说明

| 权限 | 用途 |
|------|------|
| RECORD_AUDIO | 麦克风检测环境噪音 |
| POST_NOTIFICATIONS | 前台服务通知 (Android 13+) |
| FOREGROUND_SERVICE_MICROPHONE | 前台服务使用麦克风 (Android 14+) |
| RECEIVE_BOOT_COMPLETED | 开机自启动 |
| REQUEST_IGNORE_BATTERY_OPTIMIZATIONS | 电池优化白名单 |

## 注意事项

- 部分国产手机需要手动允许自启动和后台运行权限
- 建议关闭电池优化以确保后台稳定运行
- 首次使用请在安静环境下测试映射曲线是否合适
