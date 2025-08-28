package org.example.enums;

public enum BreadcrumbToCategory {
    HOME_TO_NHA_SACH_TIKI("Trang chủ", "Nhà Sách Tiki"),
    HOME_TO_DIEN_GIA_DUNG("Trang chủ", "Điện Gia Dụng");

    private final String homeText;
    private final String categoryText;

    BreadcrumbToCategory(String homeText, String categoryText) {
        this.homeText = homeText;
        this.categoryText = categoryText;
    }

    public String getHomeText() {
        return homeText;
    }

    public String getCategoryText() {
        return categoryText;
    }

    public String getFullBreadcrumb() {
        return homeText + " > " + categoryText;
    }
}