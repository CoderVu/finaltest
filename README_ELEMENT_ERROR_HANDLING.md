# Tự Động Chụp Màn Hình Khi Element Không Tìm Thấy

## Tổng Quan

Config này tự động chụp màn hình và gắn vào Allure report khi element không tìm thấy hoặc khi test fail, **KHÔNG CẦN THAY ĐỔI CODE** trong page objects.

## Cách Hoạt Động

### 1. Tự Động Chụp Màn Hình

Khi sử dụng Selenium/Selenide actions bình thường:

```java
// Trong ShopPage.java - KHÔNG CẦN THAY ĐỔI
public void clickAllFiltersButton() {
    allProductsSection.shouldBe(Condition.visible);
    filterAllButton.shouldBe(Condition.visible).click(); // Nếu element không tìm thấy, tự động chụp màn hình
}
```

### 2. Các Trường Hợp Tự Động Chụp Màn Hình

- **NoSuchElementException**: Element không tìm thấy
- **TimeoutException**: Element không xuất hiện trong thời gian chờ
- **ElementNotInteractableException**: Element không thể tương tác
- **StaleElementReferenceException**: Element đã bị stale
- **Test Failure**: Bất kỳ lỗi nào trong test

### 3. Kết Quả Trong Allure Report

- Screenshot được gắn tự động với tên mô tả rõ ràng
- Step FAILED được tạo trong report
- Timestamp chính xác của lỗi
- Tên test và class name

## Cấu Hình

### 1. TestNG Configuration (testng.xml)

```xml
<listeners>
    <listener class-name="org.example.config.AllureConfig"/>
</listeners>
```

### 2. Files Đã Tạo/Chỉnh Sửa

- `AllureConfig.java` - **File chính** xử lý tất cả Allure events và element errors
- `SoftAssertConfig.java` - Giữ nguyên functionality hiện tại
- `testng.xml` - Chỉ cần 1 listener duy nhất

### 3. Cấu Trúc AllureConfig.java

File `AllureConfig.java` bao gồm:

- **Test Events Handling**: onTestFailure, onTestSkipped, onTestFailedWithTimeout
- **Configuration Events**: onConfigurationFailure, onConfigurationSuccess, onConfigurationSkip
- **Screenshot Capture**: Tự động chụp màn hình khi test fail
- **Element Error Detection**: Phát hiện element-related errors
- **Legacy Methods**: Giữ nguyên các method cũ để tương thích ngược

## Ví Dụ Sử Dụng

### Trước (Không Có Config)
```java
// Nếu element không tìm thấy, chỉ có exception text
public void clickButton() {
    button.shouldBe(Condition.visible).click(); // NoSuchElementException
}
```

### Sau (Với Config)
```java
// VẪN GIỮ NGUYÊN CODE NHƯ CŨ
public void clickButton() {
    button.shouldBe(Condition.visible).click(); // Tự động chụp màn hình + gắn vào Allure
}
```

## Kết Quả

1. **Không cần thay đổi code** trong page objects
2. **Tự động chụp màn hình** khi element errors xảy ra
3. **Screenshot được gắn vào Allure report** với thông tin chi tiết
4. **Dễ debug** khi element không tìm thấy
5. **Không ảnh hưởng** đến performance của test
6. **Quản lý tập trung** - tất cả config trong 1 file duy nhất

## Lưu Ý

- Config này hoạt động ở level TestNG listener
- Screenshot được chụp ngay khi test fail
- Không cần import hay sử dụng class mới nào
- Tương thích với tất cả Selenium/Selenide actions hiện tại
- **Tất cả functionality đã được gộp vào `AllureConfig.java`** - dễ quản lý hơn
