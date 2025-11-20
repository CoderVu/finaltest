# Ví dụ: CustomClickElement - Override click() khi click mặc định không hoạt động

## Vấn đề
Khi hàm `click()` mặc định không hoạt động (bị intercept, element không clickable, etc.), cần override với logic custom.

## Giải pháp: CustomClickElement

### File đã tạo: `CustomClickElement.java`

Class này override `click()` với strategy:
1. Scroll element vào view
2. Wait cho element clickable (timeout dài hơn)
3. Thử normal click trước
4. Nếu fail → fallback sang JS click

## Cách sử dụng

### Ví dụ: Chỉ 1 element cụ thể dùng CustomClickElement

```java
// Trong AgodaHomePage.java
// Element bình thường - dùng WebElementWrapper mặc định
protected IElement searchButton = $("//button[@data-selenium='searchButton']");

// Element có vấn đề - dùng ClickElementOfVu
protected IElement stickyButton = new CustomClickElement("//button[@id='sticky']");

// Sử dụng
stickyButton.click(); // Sẽ dùng custom logic (scroll, wait, fallback JS)
searchButton.click(); // Vẫn dùng click mặc định
```

## Logic trong CustomClickElement

```java
@Override
public void click() {
    // 1. Scroll vào view
    scrollElementToCenterScreen();
    
    // 2. Wait clickable với timeout dài
    waitForElementClickable(Duration.ofSeconds(15));
    
    // 3. Thử normal click
    try {
        super.click();
    } catch (Exception e) {
        // 4. Fallback sang JS click
        clickByJs();
    }
}
```

## Khi nào dùng CustomClickElement?

✅ **Chỉ dùng cho element cụ thể có vấn đề:**
- Click mặc định bị intercept
- Element cần scroll trước
- Element cần wait lâu hơn
- Element chỉ click được bằng JS

❌ **Không cần dùng khi:**
- Click mặc định hoạt động tốt
- Element đơn giản, không có vấn đề

**Lưu ý:** Chỉ tạo CustomClickElement cho element có vấn đề, các element khác vẫn dùng `$()` bình thường.

## Tùy chỉnh thêm

Nếu cần logic khác, có thể extend CustomClickElement:

```java
public class MySpecialClickElement extends CustomClickElement {
    public MySpecialClickElement(String locator) {
        super(locator);
    }
    
    @Override
    public void click() {
        // Logic đặc biệt hơn
        scrollToView();
        DriverUtils.delay(2); // Wait longer
        clickByJs(); // Always use JS
    }
}
```

