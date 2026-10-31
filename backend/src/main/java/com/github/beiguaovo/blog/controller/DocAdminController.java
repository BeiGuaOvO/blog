package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.dto.DocDTO;
import com.github.beiguaovo.blog.entity.Doc;
import com.github.beiguaovo.blog.service.DocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/docs")
@RequiredArgsConstructor
public class DocAdminController {

    private final DocService docService;

    @GetMapping("/tree")
    public Result<List<Doc>> tree() {
        return Result.ok(docService.tree(false));
    }

    @GetMapping("/{id}")
    public Result<Doc> detail(@PathVariable Long id) {
        return Result.ok(docService.detailById(id));
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody DocDTO dto) {
        return Result.ok(Map.of("id", docService.create(dto)));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DocDTO dto) {
        docService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        docService.delete(id);
        return Result.ok();
    }
}