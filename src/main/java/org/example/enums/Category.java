package org.example.enums;

import org.openqa.selenium.By;

public enum Category {
    NHA_SACH_TIKI("Nhà Sách Tiki", "/nha-sach-tiki/c8322"),
    NHA_CUA_DOI_SONG("Nhà Cửa - Đời Sống", "/nha-cua-doi-song/c1883"),
    DIEN_THOAI_MAY_TINH_BANG("Điện Thoại - Máy Tính Bảng", "/dien-thoai-may-tinh-bang/c1789"),
    DO_CHOI_ME_BE("Đồ Chơi - Mẹ & Bé", "/do-choi-me-be/c2549"),
    THIET_BI_SO_PHU_KIEN_SO("Thiết Bị Số - Phụ Kiện Số", "/thiet-bi-kts-phu-kien-so/c1815"),
    DIEN_GIA_DUNG("Điện Gia Dụng", "/dien-gia-dung/c1882"),
    LAM_DEP_SUC_KHOE("Làm Đẹp - Sức Khỏe", "/lam-dep-suc-khoe/c1520"),
    O_TO_XE_MAY_XE_DAP("Ô Tô - Xe Máy - Xe Đạp", "/o-to-xe-may-xe-dap/c8594"),
    THOI_TRANG_NU("Thời trang nữ", "/thoi-trang-nu/c931"),
    BACH_HOA_ONLINE("Bách Hóa Online", "/bach-hoa-online/c4384"),
    THE_THAO_DA_NGOAI("Thể Thao - Dã Ngoại", "/the-thao-da-ngoai/c1975"),
    THOI_TRANG_NAM("Thời trang nam", "/thoi-trang-nam/c915"),
    CROSS_BORDER_HANG_QUOC_TE("Cross Border - Hàng Quốc Tế", "/cross-border-hang-quoc-te/c17166"),
    LAPTOP_MAY_VI_TINH_LINH_KIEN("Laptop - Máy Vi Tính - Linh kiện", "/laptop-may-vi-tinh-linh-kien/c1846"),
    GIAY_DEP_NAM("Giày - Dép nam", "/giay-dep-nam/c1686"),
    DIEN_TU_DIEN_LANH("Điện Tử - Điện Lạnh", "/dien-tu-dien-lanh/c4221"),
    GIAY_DEP_NU("Giày - Dép nữ", "/giay-dep-nu/c1703"),
    MAY_ANH_MAY_QUAY_PHIM("Máy Ảnh - Máy Quay Phim", "/may-anh/c1801"),
    PHU_KIEN_THOI_TRANG("Phụ kiện thời trang", "/phu-kien-thoi-trang/c27498"),
    NGON("NGON", "/ngon/c44792"),
    DONG_HO_VA_TRANG_SUC("Đồng hồ và Trang sức", "/dong-ho-va-trang-suc/c8371"),
    BALO_VA_VALI("Balo và Vali", "/balo-va-vali/c6000"),
    VOUCHER_DICH_VU("Voucher - Dịch vụ", "/voucher-dich-vu/c11312"),
    TUI_THOI_TRANG_NU("Túi thời trang nữ", "/tui-vi-nu/c976"),
    TUI_THOI_TRANG_NAM("Túi thời trang nam", "/tui-thoi-trang-nam/c27616"),
    CHAM_SOC_NHA_CUA("Chăm sóc nhà cửa", "/cham-soc-nha-cua/c15078");

    private final String title;
    private final String href;

    Category(String title, String href) {
        this.title = title;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public String getHref() {
        return href;
    }

    public String anchorByTitleCss() {
        return "a[title='" + title + "']";
    }

    public String anchorByHrefCss() {
        return "a[href='" + href + "']";
    }

    public By byTitle() {
        return By.cssSelector(anchorByTitleCss());
    }

    public By byHref() {
        return By.cssSelector(anchorByHrefCss());
    }

}


