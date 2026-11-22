<div align="center">
  <img src="docs/assets/mksu-icon.png" width="112" alt="MKSU 图标" />

  # MKSU

  **专注环境隐藏与权限隔离的 Android 内核级 Root 方案**

  [源码仓库](https://github.com/zeroedx/MKSU) · [发行版本](https://github.com/zeroedx/MKSU/releases) · [问题反馈](https://github.com/zeroedx/MKSU/issues)
</div>

> **开发状态：** MKSU 4.0.0 正在规划与开发中，尚未发布。当前代码基于 [5ec1cff/KernelSU](https://github.com/5ec1cff/KernelSU)。以下“开发方向”不代表相关功能已经完成或通过实机验证。

## 项目介绍

MKSU 是基于 KernelSU 的独立下游项目，面向需要精细管理 Root 权限与应用环境的 Android 用户。项目优先关注环境隐藏、权限隔离、稳定性与兼容性，不以不断堆叠功能为目标。

**MKSU 并非 KernelSU 官方版本，也不由官方团队维护。** 4.0.0 计划使用独立管理器身份及专属 APK 签名，形成独立的构建与发行体系；相关改造完成并通过测试前，不应视为已经实现管理器互不兼容。

## 基础能力

当前上游代码已具备以下基础能力，实际表现取决于设备与内核环境：

- **内核级 Root 管理：** 管理应用的超级用户授权与访问策略。
- **应用配置：** 根据应用设置权限及相关隔离行为。
- **模块系统：** 支持基于元模块的系统无损修改机制。
- **系统适配：** 提供 LKM 等构建及安装相关组件。

## 4.0.0 开发方向

| 方向 | 规划内容 |
| --- | --- |
| 环境隐藏 | 改进现有 SELinux、挂载命名空间、进程隔离及可观测痕迹处理的稳健性 |
| 身份独立 | 统一 MKSU 品牌、独立包名、专属签名及管理器认证机制 |
| 模块体验 | 更多模块信息、简洁模式与预测性返回动画 |
| 兼容性 | 优先检查 Android 16 / 17 与不同 GKI 内核的实际运行情况 |
| 构建发行 | 保留日常构建产物；正式发行计划仅提供 `arm64-v8a` 管理器 APK |

**不计划加入 SUSFS、SUSFS2 或相关依赖。** 环境隐藏效果不能保证对所有检测方式有效，也不等同于设备完整性认证通过。

## 兼容性与注意事项

- 主要面向支持的 Android GKI 设备；具体内核版本、设备及构建支持以实际测试结果为准。
- Root 与内核模块修改存在启动失败和数据损坏风险，操作前请备份原始启动镜像及重要数据。
- 日常持续集成产物用于开发和测试，**正式发行包与测试产物不是同一概念**。
- 下载与发行信息请以本仓库的 [Releases](https://github.com/zeroedx/MKSU/releases) 为准，谨防冒用。

## 开发与反馈

- 开发仓库：[zeroedx/MKSU](https://github.com/zeroedx/MKSU)
- 问题与建议：[Issues](https://github.com/zeroedx/MKSU/issues)
- 安全问题：[安全报告说明](SECURITY.md)
- 发布签名与证书配置：[发布签名说明](docs/SIGNING.md)

## 开源许可与致谢

本项目延续原项目的开源许可要求：`kernel/` 相关代码以其适用的 GPL-2.0-only 条款发布，其他部分按仓库对应文件的 GPL-3.0-or-later 等许可执行；具体以各文件的许可证声明及仓库 `LICENSE` 为准。

感谢 [KernelSU](https://github.com/tiann/KernelSU)、[5ec1cff/KernelSU](https://github.com/5ec1cff/KernelSU) 及相关开源项目贡献者。MKSU 是独立下游项目，与上述项目的官方团队无从属关系。
