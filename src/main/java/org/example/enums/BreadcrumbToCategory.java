package org.example.enums;

public enum BreadcrumbToCategory {
    HOME_TO_NHA_SACH_TIKI("Trang chủ", "Nhà Sách Tiki"),
    HOME_TO_DIEN_THOAI("Trang chủ", "Điện Thoại - Máy Tính Bảng"),
    HOME_TO_DO_CHOI("Trang chủ", "Đồ Chơi - Mẹ & Bé"),
    HOME_TO_THOI_TRANG_NU("Trang chủ", "Thời trang nữ"),
    HOME_TO_THOI_TRANG_NAM("Trang chủ", "Thời trang nam"),
    HOME_TO_THE_THAO("Trang chủ", "Thể Thao - Dã Ngoại"),
    HOME_TO_BACH_HOA("Trang chủ", "Bách Hóa Online"),
    HOME_TO_MAY_ANH("Trang chủ", "Máy Ảnh - Máy Quay Phim");

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