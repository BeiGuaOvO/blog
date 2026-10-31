package com.github.beiguaovo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.entity.Article;
import com.github.beiguaovo.blog.entity.Category;
import com.github.beiguaovo.blog.mapper.ArticleMapper;
import com.github.beiguaovo.blog.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public List<Category> list() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort).orderByAsc(Category::getId));
    }

    public Long create(Category category) {
        category.setId(null);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        checkSlug(category.getSlug(), null);
        categoryMapper.insert(category);
        return category.getId();
    }

    public void update(Long id, Category category) {
        if (categoryMapper.selectById(id) == null) {
            throw new BizException(404, "分类不存在");
        }
        checkSlug(category.getSlug(), id);
        category.setId(id);
        categoryMapper.updateById(category);
    }

    public void delete(Long id) {
        long used = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, id));
        if (used > 0) {
            throw new BizException("该分类下还有 " + used + " 篇文章，请先移除");
        }
        categoryMapper.deleteById(id);
    }

    private void checkSlug(String slug, Long excludeId) {
        if (slug == null || slug.isBlank()) {
            throw new BizException("slug 不能为空");
        }
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<Category>()
                .eq(Category::getSlug, slug);
        if (excludeId != null) {
            w.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectCount(w) > 0) {
            throw new BizException("slug 已存在");
        }
    }
}