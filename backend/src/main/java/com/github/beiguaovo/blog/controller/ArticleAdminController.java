package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.PageResult;
import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.dto.ArticleDTO;
import com.github.beiguaovo.blog.entity.Article;
import com.github.beiguaovo.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class ArticleAdminController {

    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<Article>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Long categoryId) {
        return Result.ok(articleService.adminPage(page, size, keyword, status, categoryId));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.ok(articleService.adminDetail(id));
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody ArticleDTO dto) {
        return Result.ok(Map.of("id", articleService.create(dto)));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        articleService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        articleService.changeStatus(id, status);
        return Result.ok();
    }
}