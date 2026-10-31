package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.entity.Doc;
import com.github.beiguaovo.blog.service.DocService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/docs")
@RequiredArgsConstructor
public class DocPortalController {

    private final DocService docService;

    @GetMapping("/tree")
    public Result<List<Doc>> tree() {
        return Result.ok(docService.tree(true));
    }

    @GetMapping("/{slug}")
    public Result<Doc> detail(@PathVariable String slug) {
        return Result.ok(docService.detailBySlug(slug));
    }
}