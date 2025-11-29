# Figma 资源集成与图标处理经验总结

## 1. Figma MCP 工具的使用限制与规避

在使用 `figma-developer-mcp` 工具的 `download_figma_images` 功能时，我们发现该工具对目标下载路径 (`localPath`) 有严格的安全限制：
*   **问题**：尝试使用绝对路径（如 `d:\...`）下载图片时，工具会报错 "Invalid path specified. Directory traversal is not allowed"。
*   **解决方案**：
    1.  **下载到当前工作目录**：将 `localPath` 设置为当前工作空间的相对路径（例如 `downloaded_icons` 或 `.`）。
    2.  **手动移动**：下载完成后，使用 Shell 命令（如 `move` 或 `copy`）将文件移动到目标 Android 项目目录（`app/src/main/res/...`）。

## 2. Android 图标最佳实践 (Vector vs SVG vs PNG)

在 Android UI 开发中，对于图标资源的选择，我们遵循以下优先级：

| 类型 | 格式 | 推荐度 | 优点 | 缺点 | 适用场景 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Vector Drawable** | `.xml` | **⭐⭐⭐⭐⭐** | 原生支持、性能最佳、无限缩放、体积极小 | 复杂图形（如照片）无法转换；需工具转换 | UI 图标、Logo、简单插画 |
| **SVG (Coil)** | `.svg` | ⭐⭐⭐ | 开发便捷、直接使用设计稿 | 需引入解码库、运行时有解码开销、不能直接用于 `android:icon` | 快速原型开发、动态下发图标 |
| **PNG/WebP** | `.png` | ⭐ | 兼容性好 | 体积大、需多套分辨率适配、缩放失真 | 照片、复杂背景图 |

### 本次项目实践
*   **初期方案**：为了快速还原设计，我们暂时使用了 Coil 加载 `assets` 目录下的 SVG 文件。
*   **最终方案**：为了符合 Android 最佳实践（"Option 1"），我们手动将 SVG 路径转换为 Android Vector Drawable XML，并移除了 Coil 的 SVG 解码依赖。这确保了应用的高性能和规范性。

## 3. 命令行环境下的资源处理
在没有 Android Studio GUI 的环境下：
*   **资源导入**：无法使用 IDE 的 "Resource Manager" 自动导入 SVG。需要手动提取 SVG 中的 `path data` (`d="..."`) 和颜色，填入 `vector` XML 模板中。
*   **自适应图标 (Adaptive Icon)**：需手动创建 `mipmap-anydpi-v26` 目录下的 `ic_launcher.xml`，并引用前景 (`foreground`) 和背景 (`background`) 的 Vector Drawable。

---
**记录时间**：2025-11-29
