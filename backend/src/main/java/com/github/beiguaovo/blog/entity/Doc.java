package com.github.beiguaovo.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("doc")
public class Doc {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String title;
    private String slug;
    private String summary;
    private String contentMd;
    private String contentHtml;
    private Integer sort;
    private Integer isPublic;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<Doc> children = new ArrayList<>();
}