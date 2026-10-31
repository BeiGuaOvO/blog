-- 博客 + 知识库 建表脚本  MySQL 8.0
-- 执行: mysql -uroot -p < schema.sql     (会删表, 生产请先备份)
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE blog;

DROP TABLE IF EXISTS article_tag;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS doc;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS media;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS site_config;
DROP TABLE IF EXISTS sys_user;

-- ---------- 用户 ----------
CREATE TABLE sys_user (
                          id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                          username   VARCHAR(50)  NOT NULL,
                          password   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
                          nickname   VARCHAR(50)  NOT NULL DEFAULT '',
                          avatar     VARCHAR(255) NOT NULL DEFAULT '',
                          email      VARCHAR(100) NOT NULL DEFAULT '',
                          role       VARCHAR(20)  NOT NULL DEFAULT 'AUTHOR' COMMENT 'ADMIN/AUTHOR',
                          status     TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
                          created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted    TINYINT      NOT NULL DEFAULT 0,
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户';

-- ---------- 分类 ----------
CREATE TABLE category (
                          id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                          name        VARCHAR(50)  NOT NULL,
                          slug        VARCHAR(50)  NOT NULL,
                          description VARCHAR(255) NOT NULL DEFAULT '',
                          sort        INT          NOT NULL DEFAULT 0,
                          created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类';

-- ---------- 标签 ----------
CREATE TABLE tag (
                     id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                     name       VARCHAR(50) NOT NULL,
                     slug       VARCHAR(50) NOT NULL,
                     created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     PRIMARY KEY (id),
                     UNIQUE KEY uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签';

-- ---------- 文章 ----------
CREATE TABLE article (
                         id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                         title         VARCHAR(200)  NOT NULL,
                         slug          VARCHAR(200)  NOT NULL COMMENT 'URL 唯一标识, 发布后不可改',
                         summary       VARCHAR(500)  NOT NULL DEFAULT '',
                         cover         VARCHAR(255)  NOT NULL DEFAULT '',
                         content_md    LONGTEXT      NULL,
                         content_html  LONGTEXT      NULL COMMENT '预留: 服务端渲染/SSR 用',
                         category_id   BIGINT UNSIGNED NULL,
                         status        TINYINT       NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
                         is_top        TINYINT       NOT NULL DEFAULT 0,
                         views         INT           NOT NULL DEFAULT 0,
                         likes         INT           NOT NULL DEFAULT 0,
                         comment_count INT           NOT NULL DEFAULT 0,
                         publish_time  DATETIME      NULL,
                         created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         deleted       TINYINT       NOT NULL DEFAULT 0,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_slug (slug),
                         KEY idx_status_time (status, publish_time),
                         KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章';

-- 数据量大了再启用中文全文索引(ngram 需 MySQL5.7+)
-- ALTER TABLE article ADD FULLTEXT INDEX ft_article (title, summary, content_md) WITH PARSER ngram;

CREATE TABLE article_tag (
                             article_id BIGINT UNSIGNED NOT NULL,
                             tag_id     BIGINT UNSIGNED NOT NULL,
                             PRIMARY KEY (article_id, tag_id),
                             KEY idx_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章-标签';

-- ---------- 知识库文档(无限层级) ----------
CREATE TABLE doc (
                     id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                     parent_id    BIGINT UNSIGNED NULL DEFAULT NULL,
                     title        VARCHAR(200) NOT NULL,
                     slug         VARCHAR(200) NOT NULL,
                     summary      VARCHAR(500) NOT NULL DEFAULT '',
                     content_md   LONGTEXT     NULL,
                     content_html LONGTEXT     NULL,
                     sort         INT          NOT NULL DEFAULT 0,
                     is_public    TINYINT      NOT NULL DEFAULT 1 COMMENT '1公开 0私有',
                     views        INT          NOT NULL DEFAULT 0,
                     created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                     deleted      TINYINT      NOT NULL DEFAULT 0,
                     PRIMARY KEY (id),
                     UNIQUE KEY uk_slug (slug),
                     KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

-- ---------- 评论(下一轮接入) ----------
CREATE TABLE comments (
                          id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                          target_type VARCHAR(20)  NOT NULL DEFAULT 'ARTICLE' COMMENT 'ARTICLE/DOC',
                          target_id   BIGINT UNSIGNED NOT NULL,
                          parent_id   BIGINT UNSIGNED NULL,
                          nickname    VARCHAR(50)  NOT NULL,
                          email       VARCHAR(100) NOT NULL DEFAULT '',
                          website     VARCHAR(255) NOT NULL DEFAULT '',
                          content     TEXT         NOT NULL,
                          status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2拒绝',
                          ip          VARCHAR(64)  NOT NULL DEFAULT '',
                          user_agent  VARCHAR(255) NOT NULL DEFAULT '',
                          created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          KEY idx_target (target_type, target_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论';

-- ---------- 媒体库 ----------
CREATE TABLE media (
                       id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       name        VARCHAR(255) NOT NULL,
                       url         VARCHAR(500) NOT NULL,
                       size        BIGINT       NOT NULL DEFAULT 0,
                       mime        VARCHAR(100) NOT NULL DEFAULT '',
                       uploader_id BIGINT UNSIGNED NULL,
                       created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传文件';

CREATE TABLE site_config (
                             id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                             config_key   VARCHAR(100) NOT NULL,
                             config_value TEXT         NULL,
                             remark       VARCHAR(255) NOT NULL DEFAULT '',
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点配置';

-- ---------- 初始数据 ----------
INSERT INTO category (name, slug, description, sort) VALUES
                                                         ('后端','backend','Java / Spring 相关',1),
                                                         ('前端','frontend','Vue / CSS 相关',2),
                                                         ('运维','ops','Linux / Docker / Nginx',3),
                                                         ('生活','life','随笔',4);

INSERT INTO tag (name, slug) VALUES
                                 ('Java','java'),('Spring Boot','spring-boot'),('Vue','vue'),
                                 ('MySQL','mysql'),('Docker','docker'),('Nginx','nginx');

INSERT INTO article (title, slug, summary, content_md, category_id, status, is_top, publish_time)
VALUES ('Hello Blog','hello-blog','第一篇测试文章','## 你好\n\n这是 **Markdown** 测试。\n\n```java\nSystem.out.println("hi");\n```\n\n- 列表项 1\n- 列表项 2',1,1,1,NOW());

INSERT INTO doc (title, slug, content_md, sort, is_public) VALUES
                                                               ('快速开始','getting-started','# 快速开始\n\n欢迎使用知识库。',1,1),
                                                               ('环境准备','env-setup','# 环境准备\n\n- JDK 17\n- MySQL 8.0\n- Node 18+',2,1);