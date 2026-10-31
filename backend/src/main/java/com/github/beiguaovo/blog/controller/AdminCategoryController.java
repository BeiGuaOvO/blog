package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.entity.Category;
import com.github.beiguaovo.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list() {
        return Result.ok(categoryService.list());
    }

    @PostMapping
    public Result<Map<String, Long>> create(@RequestBody Category category) {
        return Result.ok(Map.of("id", categoryService.create(category)));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Category category) {
        categoryService.update(id, category);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}