# Hướng dẫn: Override/Extend Action Methods

## ✅ Design hiện tại đã hỗ trợ!

Design đã hỗ trợ override action bằng cách **tạo CustomElement class extends WebElementWrapper**.

## Cách làm: Tạo CustomElement class

### Bước 1: Tạo class extends WebElementWrapper

```java
// ClickElementOfVu.java
public class CustomClickElement extends WebElementWrapper {
    public CustomClickElement(String locator) {
        super(locator);
    }
    
    @Override
    public void click() {
        // Custom click logic
        scrollToView();
        waitForElementClickable(Duration.ofSeconds(15));
        super.click(); // Hoặc clickByJs()
    }
}
```

### Bước 2: Sử dụng trong PageObject

```java
// Trong AgodaHomePage.java hoặc PageObject khác
IElement button = new CustomClickElement("//button[@id='special']");
button.click(); // Sẽ dùng custom logic
```

## Ví dụ thực tế

### Ví dụ 1: Button cần scroll và wait lâu hơn
```java
public class StickyButtonElement extends WebElementWrapper {
    public StickyButtonElement(String locator) {
        super(locator);
    }
    
    @Override
    public void click() {
        scrollElementToCenterScreen();
        waitForElementClickable(Duration.ofSeconds(20));
        super.click();
    }
}
```

### Ví dụ 2: Input field luôn dùng JS để set value
```java
public class JSInputElement extends WebElementWrapper {
    public JSInputElement(String locator) {
        super(locator);
    }
    
    @Override
    public void setText(String text) {
        clear();
        setValue(text); // Always use JS
    }
}
```

### Ví dụ 3: Element cần retry nhiều lần
```java
public class RetryClickElement extends WebElementWrapper {
    public RetryClickElement(String locator) {
        super(locator);
    }
    
    @Override
    public void click() {
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            try {
                super.click();
                return;
            } catch (Exception e) {
                if (i == maxRetries - 1) throw e;
                log.warn("Click attempt {} failed, retrying...", i + 1);
                DriverUtils.delay(1);
            }
        }
    }
}
```

### Ví dụ 4: Override nhiều methods
```java
public class CustomElement extends WebElementWrapper {
    public CustomElement(String locator) {
        super(locator);
    }
    
    @Override
    public void click() {
        // Custom click
        scrollToView();
        clickByJs();
    }
    
    @Override
    public void setText(String text) {
        // Custom setText
        clear();
        setValue(text);
    }
}
```

## Lưu ý

1. **Components là `protected`** - Có thể access trong subclass
2. **Có thể gọi `super.method()`** - Để dùng logic mặc định
3. **Có thể override bất kỳ method nào** từ IElement interface
4. **Tái sử dụng được** - Tạo 1 lần, dùng nhiều nơi

## Kết luận

✅ **Design đã hỗ trợ đầy đủ** - Chỉ cần extends WebElementWrapper và override method cần thiết!

