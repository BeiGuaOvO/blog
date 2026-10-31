package com.github.beiguaovo.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ArchiveVO {
    private int year;
    private int month;
    private long count;
    private List<Item> articles = new ArrayList<>();

    public ArchiveVO(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public void add(Item item) {
        articles.add(item);
        count = articles.size();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String title;
        private String slug;
        private LocalDateTime publishTime;
    }
}