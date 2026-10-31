package com.github.beiguaovo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.common.PageResult;
import com.github.beiguaovo.blog.dto.ArticleDTO;
import com.github.beiguaovo.blog.entity.Article;
import com.github.beiguaovo.blog.entity.ArticleTag;
import com.github.beiguaovo.blog.entity.Category;
import com.github.beiguaovo.blog.entity.Tag;
import com.github.beiguaovo.blog.mapper.ArticleMapper;
import com.github.beiguaovo.blog.mapper.ArticleTagMapper;
import com.github.beiguaovo.blog.mapper.CategoryMapper;
import com.github.beiguaovo.blog.mapper.TagMapper;
import com.github.beiguaovo.blog.vo.ArchiveVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;

    /* ==================== 前台 ==================== */

    public PageResult<Article> portalPage(long page, long size, String keyword,
                                          Long categoryId, Long tagId) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getSlug, Article::getSummary,
                        Article::getCover, Article::getCategoryId, Article::getViews,
                        Article::getIsTop, Article::getCommentCount,
                        Article::getPublishTime, Article::getCreatedAt)
                .eq(Article::getStatus, 1)
                .le(Article::getPublishTime, LocalDateTime.now());
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        if (categoryId != null) {
            w.eq(Article::getCategoryId, categoryId);
        }
        if (tagId != null) {
            // tagId 已由 Spring 转换为 Long，拼接安全
            w.inSql(Article::getId, "SELECT article_id FROM article_tag WHERE tag_id = " + tagId);
        }
        w.orderByDesc(Article::getIsTop).orderByDesc(Article::getPublishTime);

        Page<Article> result = articleMapper.selectPage(new Page<>(page, size), w);
        fillCategory(result.getRecords());
        fillTags(result.getRecords());
        return PageResult.of(result);
    }

    public Article detailBySlug(String slug) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getSlug, slug)
                .eq(Article::getStatus, 1)
                .and(q -> q.isNull(Article::getPublishTime)
                        .or().le(Article::getPublishTime, LocalDateTime.now())));
        if (article == null) {
            throw new BizException(404, "文章不存在或未发布");
        }
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, article.getId())
                .setSql("views = views + 1"));
        article.setViews((article.getViews() == null ? 0 : article.getViews()) + 1);
        fillCategory(List.of(article));
        fillTags(List.of(article));
        return article;
    }

    public List<Article> hotArticles(int limit) {
        Page<Article> result = articleMapper.selectPage(new Page<>(1, limit),
                new LambdaQueryWrapper<Article>()
                        .select(Article::getId, Article::getTitle, Article::getSlug, Article::getViews)
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViews));
        return result.getRecords();
    }

    public List<ArchiveVO> archive() {
        List<Article> list = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getSlug, Article::getPublishTime)
                .eq(Article::getStatus, 1)
                .isNotNull(Article::getPublishTime)
                .orderByDesc(Article::getPublishTime));

        Map<String, ArchiveVO> grouped = new LinkedHashMap<>();
        for (Article a : list) {
            LocalDateTime t = a.getPublishTime();
            String key = t.getYear() + "-" + t.getMonthValue();
            ArchiveVO vo = grouped.computeIfAbsent(key, k -> new ArchiveVO(t.getYear(), t.getMonthValue()));
            vo.add(new ArchiveVO.Item(a.getId(), a.getTitle(), a.getSlug(), t));
        }
        return new ArrayList<>(grouped.values());
    }

    /* ==================== 后台 ==================== */

    public PageResult<Article> adminPage(long page, long size, String keyword,
                                         Integer status, Long categoryId) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getSlug, Article::getCategoryId,
                        Article::getStatus, Article::getIsTop, Article::getViews,
                        Article::getCommentCount, Article::getPublishTime,
                        Article::getCreatedAt, Article::getUpdatedAt);
        if (StringUtils.hasText(keyword)) {
            w.like(Article::getTitle, keyword);
        }
        if (status != null) {
            w.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            w.eq(Article::getCategoryId, categoryId);
        }
        w.orderByDesc(Article::getIsTop).orderByDesc(Article::getCreatedAt);

        Page<Article> result = articleMapper.selectPage(new Page<>(page, size), w);
        fillCategory(result.getRecords());
        return PageResult.of(result);
    }

    public Article adminDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException(404, "文章不存在");
        }
        fillTags(List.of(article));
        return article;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleDTO dto) {
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setId(null);
        article.setSlug(resolveSlug(dto.getSlug(), dto.getTitle(), null));
        article.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        article.setIsTop(dto.getIsTop() == null ? 0 : dto.getIsTop());
        article.setViews(0);
        article.setLikes(0);
        article.setCommentCount(0);
        if (article.getStatus() == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.insert(article);
        saveTags(article.getId(), dto.getTagIds());
        return article.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ArticleDTO dto) {
        Article old = articleMapper.selectById(id);
        if (old == null) {
            throw new BizException(404, "文章不存在");
        }

        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setId(id);
        article.setSlug(resolveSlug(dto.getSlug(), dto.getTitle(), id));
        article.setStatus(dto.getStatus() == null ? old.getStatus() : dto.getStatus());
        article.setIsTop(dto.getIsTop() == null ? old.getIsTop() : dto.getIsTop());
        article.setPublishTime(resolvePublishTime(old, article.getStatus(), dto.getPublishTime()));
        articleMapper.updateById(article);

        // categoryId 允许置空：updateById 会跳过 null 字段，这里显式写一次
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getCategoryId, dto.getCategoryId()));

        saveTags(id, dto.getTagIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw new BizException(404, "文章不存在");
        }
        articleMapper.deleteById(id);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
    }

    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态只能是 0（草稿）或 1（发布）");
        }
        Article old = articleMapper.selectById(id);
        if (old == null) {
            throw new BizException(404, "文章不存在");
        }
        LambdaUpdateWrapper<Article> w = new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getStatus, status);
        if (status == 1 && old.getPublishTime() == null) {
            w.set(Article::getPublishTime, LocalDateTime.now());
        }
        articleMapper.update(null, w);
    }

    /* ==================== 内部方法 ==================== */

    /**
     * 发布时间规则：
     * 已发布过的文章发布时间保持不变；首次发布时用传入值，未传则取当前时间。
     */
    private LocalDateTime resolvePublishTime(Article old, Integer status, LocalDateTime given) {
        if (status != null && status == 1) {
            if (old.getPublishTime() != null) {
                return old.getPublishTime();
            }
            return given == null ? LocalDateTime.now() : given;
        }
        return old.getPublishTime();
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        tagIds.stream().filter(Objects::nonNull).distinct()
                .forEach(tagId -> articleTagMapper.insert(new ArticleTag(articleId, tagId)));
    }

    private void fillCategory(List<Article> list) {
        List<Long> ids = list.stream().map(Article::getCategoryId)
                .filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> names = categoryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        list.forEach(a -> a.setCategoryName(names.get(a.getCategoryId())));
    }

    private void fillTags(List<Article> list) {
        if (list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(Article::getId).toList();
        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, ids));
        if (relations.isEmpty()) {
            return;
        }
        List<Long> tagIds = relations.stream().map(ArticleTag::getTagId).distinct().toList();
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));
        Map<Long, List<Tag>> grouped = new HashMap<>();
        for (ArticleTag relation : relations) {
            Tag tag = tagMap.get(relation.getTagId());
            if (tag != null) {
                grouped.computeIfAbsent(relation.getArticleId(), k -> new ArrayList<>()).add(tag);
            }
        }
        list.forEach(a -> a.setTags(grouped.getOrDefault(a.getId(), List.of())));
    }

    private String resolveSlug(String slug, String title, Long excludeId) {
        String base = StringUtils.hasText(slug) ? slug.trim().toLowerCase() : slugify(title);
        if (!StringUtils.hasText(base)) {
            base = "a-" + System.currentTimeMillis();
        }
        String candidate = base;
        int suffix = 2;
        while (slugExists(candidate, excludeId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean slugExists(String slug, Long excludeId) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<Article>().eq(Article::getSlug, slug);
        if (excludeId != null) {
            w.ne(Article::getId, excludeId);
        }
        return articleMapper.selectCount(w) > 0;
    }

    /** 只保留字母数字和连字符；中文标题会得到空串，建议手填英文 slug */
    private String slugify(String text) {
        if (text == null) {
            return "";
        }
        String s = text.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}