# 北瓜笔记 · Beigua Blog

一个前后端分离的个人博客 + 知识库系统。博客走时间流，知识库走层级目录，两者共用一套 Markdown 渲染管线。

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.4-42b883)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1)
![License](https://img.shields.io/badge/license-MIT-blue)

> 站点名称、Logo、作者信息、备案号均可在后台「站点设置」中修改，存库即时生效，无需重新构建。

## 功能

### 博客
- Markdown 写作，代码高亮、KaTeX 数学公式、Mermaid 预留
- 分类 / 标签 / 置顶 / 按年月归档
- 标题与摘要关键词搜索
- 热门阅读排行
- 右侧目录（TOC）随滚动高亮
- 响应式布局 + 暗黑模式

### 知识库
- 无限层级文档树，支持拖拽排序与父节点调整
- 面包屑导航、文档内锚点跳转
- 公开 / 私有文档控制
- 与博客共用 Markdown 渲染，公式、代码块表现一致

### 后台
- JWT 登录、修改密码
- Markdown 编辑器（md-editor-v3），支持粘贴 / 拖拽上传图片
- 文章草稿、发布、撤回、置顶
- 文档树管理（新建子文档、移动层级、删除保护）
- 分类、标签 CRUD
- 站点设置：站点名、副标题、Logo、作者信息、备案号、SEO、社交链接
- 接口文档：SpringDoc / Swagger UI

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.2.5、Spring Security + JWT、MyBatis-Plus 3.5.5、MySQL 8 |
| 前端 | Vue 3.4、Vite 5、Pinia、Vue Router 4、Element Plus |
| 渲染 | markdown-it、highlight.js、KaTeX、DOMPurify |
| 构建 | Maven、npm |

## 目录结构

```
blog/
├─ backend/
│  ├─ pom.xml
│  ├─ sql/schema.sql                 建表 + 种子数据
│  └─ src/main/
│     ├─ java/com/github/beiguaovo/blog/
│     │  ├─ common/                  统一响应、异常处理、分页
│     │  ├─ config/                  Security / MyBatis-Plus / WebMvc / 初始化
│     │  ├─ security/                JWT 生成与校验
│     │  ├─ entity/ mapper/          实体与 Mapper
│     │  ├─ dto/ vo/                 请求参数与响应视图
│     │  ├─ service/                 业务逻辑
│     │  ├─ storage/                 上传目录管理
│     │  └─ controller/              REST 接口
│     └─ resources/application.yml
├─ frontend/
│  ├─ package.json
│  ├─ vite.config.js
│  └─ src/
│     ├─ api/                       接口封装
│     ├─ stores/                    Pinia（user / theme / site / doc）
│     ├─ router/
│     ├─ utils/                     markdown / KaTeX / 锚点
│     ├─ components/                MarkdownView / TocNav / DocTree
│     ├─ layouts/                   Portal / Docs / Admin
│     └─ views/                     前台与后台页面
├─ start.bat                         Windows 一键启动
├─ stop.bat                          清理 8080 / 5173 占用
├─ CHANGELOG.md
└─ README.md
```

## 快速开始

### 环境要求

- JDK 17 或以上（推荐 21）
- Maven 3.9+
- MySQL 8.0
- Node.js 18+

### 1. 初始化数据库

```bash
mysql -u root -p --default-character-set=utf8mb4 < backend/sql/schema.sql
```

脚本会创建 `blog` 库、9 张表，以及若干分类 / 标签 / 示例文章。

> 脚本开头有 `DROP TABLE IF EXISTS`，重复执行会清空数据。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动创建管理员账号并在日志中提示：

```
已创建初始管理员 admin / admin123，请立即修改密码！
```

- 接口地址：`http://localhost:8080/api`
- 接口文档：`http://localhost:8080/api/swagger-ui.html`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173`，右上角「登录」进入后台。

### 4. 一键启动（Windows）

双击根目录 `start.bat`，会依次检查 JDK、Maven、MySQL，启动前后端并用健康检查等待后端就绪后自动打开浏览器。

## 配置

所有配置均可通过环境变量覆盖，`application.yml` 中只保留开发默认值。

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/blog?...` | JDBC 连接串 |
| `DB_USER` | `root` | 数据库账号 |
| `DB_PASSWORD` | `root` | 数据库密码 |
| `JWT_SECRET` | 示例值 | **生产必须替换**，长度 ≥ 32 字节 |
| `JWT_EXPIRE` | `604800` | token 有效期（秒） |
| `UPLOAD_PATH` | `./uploads` | 上传文件根目录，生产建议绝对路径 |
| `CORS_ORIGINS` | `http://localhost:5173` | 允许的前端来源，逗号分隔 |

生成随机密钥：

```bash
openssl rand -hex 32
```

## 主要接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录，返回 JWT |
| PUT | `/api/auth/password` | 修改密码 |
| GET | `/api/portal/articles` | 文章列表（分页 / 搜索 / 分类 / 标签） |
| GET | `/api/portal/articles/{slug}` | 文章详情 |
| GET | `/api/portal/archive` | 归档 |
| GET | `/api/portal/docs/tree` | 知识库文档树 |
| GET | `/api/portal/docs/{slug}` | 文档详情 |
| GET | `/api/portal/site-config` | 站点配置 |
| GET/POST/PUT/DELETE | `/api/admin/articles/**` | 文章管理 |
| GET/POST/PUT/DELETE | `/api/admin/docs/**` | 文档管理 |
| PUT | `/api/admin/site-config` | 站点设置保存 |
| POST | `/api/admin/upload` | 图片 / 附件上传 |

完整列表见 Swagger UI。

## 已知限制 / Roadmap

- [ ] 评论模块（表结构已就绪，接口与页面待实现）
- [ ] 部署方案：Dockerfile、docker-compose、Nginx 配置
- [ ] 全文检索（当前为 `LIKE` 查询，量大后可切 MySQL 全文索引或 Elasticsearch）
- [ ] 文章历史版本与 diff
- [ ] RSS / Atom 订阅
- [ ] 多作者与细粒度权限（当前仅 ADMIN / AUTHOR 两种角色）

## 安全说明

- Markdown 渲染默认禁用原始 HTML，并经 DOMPurify 二次过滤
- 上传文件按 `yyyy/MM` 分目录存储，使用 UUID 命名，带扩展名白名单
- 上传目录与静态资源映射统一由 `UploadStorage` 管理，防止路径穿越
- JWT 无状态认证，登出由前端清除 token；修改密码不会让已签发的 token 立即失效

> 部署到公网前请务必：修改默认管理员密码、替换 `JWT_SECRET`、配置 HTTPS。

## License

[MIT](LICENSE)