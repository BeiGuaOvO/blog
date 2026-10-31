package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.entity.Category;
import com.github.beiguaovo.blog.entity.Tag;
import com.github.beiguaovo.blog.service.CategoryService;
import com.github.beiguaovo.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalTaxonomyController {

    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(categoryService.list());
    }

    @GetMapping("/tags")
    public Result<List<Tag>> tags() {
        return Result.ok(tagService.list());
    }
}