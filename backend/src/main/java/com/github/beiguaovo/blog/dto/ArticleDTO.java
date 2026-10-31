package com.github.beiguaovo.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题过长")
    private String title;
    /** 留空则自动生成 */
    private String slug;
    @Size(max = 500, message = "摘要过长")
    private String summary;
    private String cover;
    @NotBlank(message = "正文不能为空")
    private String contentMd;
    private String contentHtml;
    private Long categoryId;
    /** 0 草稿 1 发布 */
    private Integer status;
    private Integer isTop;
    private List<Long> tagIds;
    private LocalDateTime publishTime;
}