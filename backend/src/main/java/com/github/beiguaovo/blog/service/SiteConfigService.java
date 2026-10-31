package com.github.beiguaovo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.entity.SiteConfig;
import com.github.beiguaovo.blog.mapper.SiteConfigMapper;
import com.github.beiguaovo.blog.vo.SiteConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteConfigService {

    private static final String KEY = "site";

    private final SiteConfigMapper siteConfigMapper;
    private final ObjectMapper objectMapper;

    /** 公开读取：库里没有就用默认值，前端永远拿得到一份完整配置 */
    public SiteConfigVO get() {
        SiteConfig row = siteConfigMapper.selectOne(
                new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, KEY));
        if (row == null || !StringUtils.hasText(row.getConfigValue())) {
            return defaults();
        }
        try {
            return mergeWithDefaults(objectMapper.readValue(row.getConfigValue(), SiteConfigVO.class));
        } catch (Exception e) {
            log.warn("site_config 解析失败，返回默认配置: {}", e.getMessage());
            return defaults();
        }
    }

    /** 后台保存：整份覆盖 */
    public void save(SiteConfigVO vo) {
        if (vo == null) {
            throw new BizException("配置不能为空");
        }
        SiteConfigVO merged = mergeWithDefaults(vo);
        if (!StringUtils.hasText(merged.getSiteName())) {
            throw new BizException("站点名称不能为空");
        }
        merged.setSiteName(merged.getSiteName().trim());

        String json;
        try {
            json = objectMapper.writeValueAsString(merged);
        } catch (Exception e) {
            throw new BizException(500, "配置序列化失败");
        }

        SiteConfig row = siteConfigMapper.selectOne(
                new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, KEY));
        if (row == null) {
            row = new SiteConfig();
            row.setConfigKey(KEY);
            row.setConfigValue(json);
            row.setRemark("站点基础信息(JSON)");
            siteConfigMapper.insert(row);
        } else {
            row.setConfigValue(json);
            siteConfigMapper.updateById(row);
        }
    }

    /** 想恢复出厂设置：DELETE FROM site_config WHERE config_key='site'; */
    private SiteConfigVO defaults() {
        SiteConfigVO v = new SiteConfigVO();
        v.setSiteName("极客笔记");
        v.setSiteSubtitle("记录 Java / 前端 / 运维的实践与思考。");
        v.setSiteDescription("记录 Java / 前端 / 运维的实践与思考。");
        v.setLogoText("G");
        v.setLogoImage("");
        v.setAuthorName("极客笔记");
        v.setAuthorBio("记录 Java / 前端 / 运维的实践与思考。");
        v.setAuthorAvatar("");
        v.setFooterText("");
        v.setIcp("");
        v.setSeoKeywords("Java,Spring Boot,Vue,知识库");
        v.setSeoDescription("记录 Java / 前端 / 运维的实践与思考。");
        v.setStartYear(Year.now().getValue());
        v.setGithub("");
        v.setEmail("");
        return v;
    }

    /** null 补默认值；空串保留（表示用户主动清空） */
    private SiteConfigVO mergeWithDefaults(SiteConfigVO v) {
        SiteConfigVO d = defaults();
        if (!StringUtils.hasText(v.getSiteName())) v.setSiteName(d.getSiteName());
        if (v.getSiteSubtitle() == null) v.setSiteSubtitle(d.getSiteSubtitle());
        if (v.getSiteDescription() == null) v.setSiteDescription(d.getSiteDescription());
        if (!StringUtils.hasText(v.getLogoText())) v.setLogoText(d.getLogoText());
        if (v.getLogoImage() == null) v.setLogoImage("");
        if (v.getAuthorName() == null) v.setAuthorName(d.getAuthorName());
        if (v.getAuthorBio() == null) v.setAuthorBio(d.getAuthorBio());
        if (v.getAuthorAvatar() == null) v.setAuthorAvatar("");
        if (v.getFooterText() == null) v.setFooterText("");
        if (v.getIcp() == null) v.setIcp("");
        if (v.getSeoKeywords() == null) v.setSeoKeywords(d.getSeoKeywords());
        if (v.getSeoDescription() == null) v.setSeoDescription(d.getSeoDescription());
        if (v.getStartYear() == null) v.setStartYear(d.getStartYear());
        if (v.getGithub() == null) v.setGithub("");
        if (v.getEmail() == null) v.setEmail("");
        return v;
    }
}