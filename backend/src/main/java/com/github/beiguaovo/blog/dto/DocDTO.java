package com.github.beiguaovo.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String slug;
    private Long parentId;
    private String summary;
    @NotBlank(message = "正文不能为空")
    private String contentMd;
    private String contentHtml;
    private Integer sort;
    /** 1 公开 0 私有 */
    private Integer isPublic;
}