package com.github.beiguaovo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.entity.ArticleTag;
import com.github.beiguaovo.blog.entity.Tag;
import com.github.beiguaovo.blog.mapper.ArticleTagMapper;
import com.github.beiguaovo.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    public List<Tag> list() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId));
    }

    public Long create(Tag tag) {
        tag.setId(null);
        checkSlug(tag.getSlug(), null);
        tagMapper.insert(tag);
        return tag.getId();
    }

    public void update(Long id, Tag tag) {
        if (tagMapper.selectById(id) == null) {
            throw new BizException(404, "标签不存在");
        }
        checkSlug(tag.getSlug(), id);
        tag.setId(id);
        tagMapper.updateById(tag);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        tagMapper.deleteById(id);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
    }

    private void checkSlug(String slug, Long excludeId) {
        if (slug == null || slug.isBlank()) {
            throw new BizException("slug 不能为空");
        }
        LambdaQueryWrapper<Tag> w = new LambdaQueryWrapper<Tag>().eq(Tag::getSlug, slug);
        if (excludeId != null) {
            w.ne(Tag::getId, excludeId);
        }
        if (tagMapper.selectCount(w) > 0) {
            throw new BizException("slug 已存在");
        }
    }
}