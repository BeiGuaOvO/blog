package com.github.beiguaovo.blog.controller;

import com.github.beiguaovo.blog.common.Result;
import com.github.beiguaovo.blog.service.SiteConfigService;
import com.github.beiguaovo.blog.vo.SiteConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalSiteController {

    private final SiteConfigService siteConfigService;

    @GetMapping("/site-config")
    public Result<SiteConfigVO> get() {
        return Result.ok(siteConfigService.get());
    }
}