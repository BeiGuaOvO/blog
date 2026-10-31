package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.service.SiteConfigService;
import com.github.beiguaovo.blog.vo.SiteConfigVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/site-config")
@RequiredArgsConstructor
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    @GetMapping
    public Result<SiteConfigVO> get() {
        return Result.ok(siteConfigService.get());
    }

    @PutMapping
    public Result<Void> save(@Valid @RequestBody SiteConfigVO vo) {
        siteConfigService.save(vo);
        return Result.ok();
    }
}