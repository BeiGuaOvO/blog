# Changelog

本项目的所有重要变更都记录在此文件。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Planned
- 评论模块：嵌套回复、审核后台、敏感词与限流
- 部署方案：Dockerfile、docker-compose、Nginx 反向代理
- 全文检索：MySQL 全文索引或 Elasticsearch
- 文章历史版本与 diff 对比
- RSS / Atom 订阅

## [1.0.0] - 2026-10-31

首个可用版本，覆盖博客与知识库的核心读写链路。

### Added

**后端**
- Spring Boot 3.2 + MyBatis-Plus 基础工程，统一响应体 `Result` 与全局异常处理
- JWT 认证：登录、获取当前用户、修改密码；`JwtAuthFilter` 无状态校验
- 文章模块：分页、关键词搜索、分类与标签筛选、置顶、草稿/发布、按年月归档、浏览量统计
- 知识库模块：无限层级文档树、公开/私有控制、按 slug 访问、子文档删除保护
- 分类与标签 CRUD，slug 唯一性校验，删除前引用检查
- 文件上传：`yyyy/MM` 分目录、UUID 命名、扩展名白名单、路径穿越防护
- 站点配置：以 JSON 单行存储于 `site_config`，字段缺省自动补默认值
- SpringDoc 接口文档，逻辑删除与分页拦截器

**前端**
- Vue 3 + Vite + Pinia + Element Plus 工程骨架，`/api` 开发代理
- 前台：首页列表、文章详情、归档、知识库文档树与详情、404
- Markdown 渲染管线：代码高亮、外链新窗口、标题锚点与 TOC 滚动高亮
- KaTeX 数学公式渲染，支持行内 `$...$` 与块级 `$$...$$`
- 暗黑模式、响应式布局、图片灯箱式展示
- 后台：登录、文章列表与编辑器（图床上传）、文档树管理、分类/标签管理、站点设置（带实时预览）

**工程**
- `sql/schema.sql`：9 张表与种子数据
- `start.bat` / `stop.bat`：Windows 本地一键启停
- 首次启动自动创建管理员账号

### Fixed
- 文档移动到根目录时 `parentId` 无法置空（`updateById` 跳过 null 字段）
- 文章取消分类时 `categoryId` 无法置空（同上）
- 各 Mapper 泛型与实体类型不匹配导致编译失败
- 静态资源映射缺少结尾斜杠，导致上传成功但图片 404
- 知识库正文中的 LaTeX 公式被当作纯文本输出

### Security
- Markdown 渲染禁用原始 HTML，并经 DOMPurify 过滤
- 上传文件名不落库到磁盘，统一 UUID 重命名
- 生产环境的密钥与数据库密码通过环境变量注入，不写入仓库

[Unreleased]: https://github.com/beiguaovo/blog/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/beiguaovo/blog/releases/tag/v1.0.0