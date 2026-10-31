package com.github.beiguaovo.blog.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SiteConfigVO {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 40, message = "站点名称不超过 40 个字符")
    private String siteName;

    @Size(max = 120, message = "副标题不超过 120 个字符")
    private String siteSubtitle;

    @Size(max = 300, message = "站点描述不超过 300 个字符")
    private String siteDescription;

    /** 单字符 logo，如 G / 北 */
    @Size(max = 4, message = "Logo 文字不超过 4 个字符")
    private String logoText;

    private String logoImage;

    @Size(max = 40)
    private String authorName;

    @Size(max = 120)
    private String authorBio;

    private String authorAvatar;

    @Size(max = 120)
    private String footerText;

    @Size(max = 60)
    private String icp;

    @Size(max = 200)
    private String seoKeywords;

    @Size(max = 300)
    private String seoDescription;

    private Integer startYear;

    private String github;

    private String email;
}