package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.PageResult;
import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.entity.Article;
import com.github.beiguaovo.blog.service.ArticleService;
import com.github.beiguaovo.blog.vo.ArchiveVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal")
@RequiredArgsConstructor
public class ArticlePortalController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public Result<PageResult<Article>> list(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Long tagId) {
        return Result.ok(articleService.portalPage(page, size, keyword, categoryId, tagId));
    }

    @GetMapping("/articles/hot")
    public Result<List<Article>> hot(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(articleService.hotArticles(limit));
    }

    @GetMapping("/articles/{slug}")
    public Result<Article> detail(@PathVariable String slug) {
        return Result.ok(articleService.detailBySlug(slug));
    }

    @GetMapping("/archive")
    public Result<List<ArchiveVO>> archive() {
        return Result.ok(articleService.archive());
    }
}