# 液态玻璃水印工具 · Liquid Glass Watermark Studio

> ## ⚠️ 验证状态：未验证 / Unverified
>
> 本项目**尚未在完整构建环境（Android Studio / 装有 SDK 与可用 Node 的桌面）中完成端到端验证**。
> 仅 **Desktop (JVM) 目标**在本地（Termux）**编译通过**并产出 jar；**Web (Wasm) 与 Android 目标**
> 因本机环境限制（Termux 无法运行 Kotlin 自动下载的 glibc Node；无 Android SDK）**未在本机实际构建/运行验证**。
> 请在正常开发机上自行 `./gradlew ...` 验证后再使用。

一个基于 [AndroidLiquidGlass / Backdrop](https://github.com/Kyant0/AndroidLiquidGlass)（`io.github.kyant0:backdrop:2.0.0`）的 **Compose Multiplatform** 工具：
自动在图片下方添加“液态玻璃”样式的水印，并通过 GUI **调节该库支持的全部参数**。

- **Modifier 方式调用**：核心组件是 `Modifier.liquidGlassWatermark(backdrop, config)`，内部使用库的 `Modifier.drawBackdrop(...)`。
- **跨平台**：Android / Desktop (JVM) / Web (Wasm)。
- **自带演示组件**：`LiquidButton`、`LiquidSlider`、`LiquidToggle`、`LiquidTabs`（全部用 `drawBackdrop` 实现玻璃质感，可复用）。

## 运行

```bash
# Desktop (JVM)
./gradlew :composeApp:desktopRun

# Web (Wasm) —— 本地开发服务器，浏览器打开自动弹出的地址
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# 打包
./gradlew :composeApp:desktopPackageDistribution  # 桌面安装包
./gradlew :composeApp:wasmJsBrowserDistribution    # Web 静态产物 (build/dist/wasmJs/productionExecutable)
```

> Android 版需要在 Android Studio / 装有 Android SDK 的环境打开后直接运行（本仓库在无 SDK 的
> 环境下主要验证 Desktop / Web 目标）。

## 项目结构

```
composeApp/
└── src/
    ├── commonMain/kotlin/com/liquidglass/watermark/
    │   ├── App.kt / MainScreen.kt        # 主 GUI：预览 + 参数面板
    │   ├── Scene.kt                      # 示例风景“图片”（Canvas 绘制）
    │   ├── model/WatermarkConfig.kt      # 全部可调参数的数据模型
    │   ├── watermark/LiquidGlassWatermark.kt  # 核心水印 Modifier + BackdropHost
    │   └── components/LiquidComponents.kt     # LiquidButton / Slider / Toggle / Tabs
    ├── desktopMain/kotlin/.../Main.kt
    ├── wasmJsMain/kotlin/.../Main.kt
    └── androidMain/kotlin/.../MainActivity.kt
```

## 可调参数（映射到库的完整 API）

| 分类 | 参数 | 库对应 API |
|------|------|-----------|
| 布局/文字 | 水印文字、字号、字距、条高、圆角比例、胶囊圆角、底边距、放置位置(叠于图下/图下方) | `shape` / `RoundedRectangle` / `Capsule` |
| 背景滤镜 | 模糊半径、折射高度、折射强度、深度、色差、鲜艳度、亮度、对比度、饱和度、玻璃不透明度 | `blur` / `lens` / `colorControls` / `opacity` / `vibrancy` |
| 高光 | 启用、样式(Default/Ambient/Plain)、宽度、透明度、角度、衰减、环境光强度 | `highlight` / `HighlightStyle` |
| 外阴影 | 启用、半径、偏移 X/Y、不透明度 | `shadow` / `Shadow` |
| 内阴影 | 启用、半径、偏移 Y、不透明度 | `innerShadow` / `InnerShadow` |
| 表面 | 着色层、色调混合、顶部高光分隔线、细粒度噪点 | `onDrawSurface` |

## 核心用法示例

```kotlin
// 1) 把图片（或任意区域）录制为 backdrop
BackdropHost(Modifier.fillMaxSize()) {
    ImageScene(Modifier.fillMaxSize())

    // 2) 用 Modifier 在图片下方加液态玻璃水印条
    LiquidGlassWatermarkBar(
        backdrop = LocalBackdrop.current,          // 采样它身后的图片
        config = config,                            // 所有参数集中在这里
        modifier = Modifier.align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
            .height(config.watermarkHeightDp.dp),
    ) { Icon(Icons.Filled.WaterDrop, null); Text(config.watermarkText) }
}
```

底层就是一行：

```kotlin
Modifier.liquidGlassWatermark(backdrop, config)
    // 等价于 Modifier.drawBackdrop(backdrop, shape, effects, highlight, shadow, innerShadow, onDrawSurface)
```

## 依赖版本

- Kotlin `2.3.21`、Compose Multiplatform `1.11.0`（与 `backdrop:2.0.0` 发布的 POM 一致）
- `io.github.kyant0:backdrop:2.0.0`、`io.github.kyant0:shapes:1.2.0`
- Gradle `8.14`、AGP `8.9.1`（Android）
