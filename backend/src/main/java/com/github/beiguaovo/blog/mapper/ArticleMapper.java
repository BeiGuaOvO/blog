package com.github.beiguaovo.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.beiguaovo.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}