package com.example.domain.skill2.model;

/**
 * 查询过滤条件 —— 纯数据对象，不依赖任何持久化框架。
 * AppService 把 Query DTO 转成这个传给领域层。
 */
public class Skill2Filter {

    private final String keyword;
    private final String category;
    private final String level;
    private final String status;

    private Skill2Filter(Builder b) {
        this.keyword = b.keyword;
        this.category = b.category;
        this.level = b.level;
        this.status = b.status;
    }

    /* ===== 自描述工厂方法 ===== */

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String keyword;
        private String category;
        private String level;
        private String status;

        public Builder keyword(String v) { this.keyword = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder level(String v) { this.level = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Skill2Filter build() { return new Skill2Filter(this); }
    }

    /* ===== 是否有条件（用于判断是否跳过查询） ===== */

    public boolean isEmpty() {
        return isBlank(keyword) && isBlank(category) && isBlank(level) && isBlank(status);
    }

    public String keyword() { return keyword; }
    public String category() { return category; }
    public String level() { return level; }
    public String status() { return status; }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
