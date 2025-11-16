# Auto-Retry cho UI Assertions

Framework hỗ trợ **auto-retry** cho UI assertions để xử lý các trường hợp flaky tests hoặc timing issues.

## Tổng quan

Khi một UI assertion fail, framework sẽ tự động retry 1 lần trước khi đánh dấu là failed. Điều này giúp:
- Giảm false negatives do timing issues
- Xử lý các trường hợp UI chưa kịp update
- Tăng độ ổn định của test suite

## Cách sử dụng

### 1. Sử dụng AssertionHelper (Khuyến nghị)

`AssertionHelper` cung cấp các method assertion với auto-retry built-in:

```java


import static org.example.core.control.element.ElementFactory.$;

// Assert text equals
Element titleElement = $("//h1[@class='title']");
AssertionHelper.

        assertTextEquals(
                titleElement, 
    "Welcome",
            "Page title should be 'Welcome'"
        );

        // Assert text contains
        Element messageElement = $("//div[@class='message']");
AssertionHelper.

        assertTextContains(
                messageElement, 
    "success",
            "Message should contain 'success'"
        );

        // Assert element visible
        Element buttonElement = $("//button[@id='submit']");
AssertionHelper.

        assertElementVisible(
                buttonElement, 
    "Submit button should be visible"
        );

        // Assert attribute
        Element linkElement = $("//a[@id='home-link']");
AssertionHelper.

        assertAttribute(
                linkElement, 
    "href",
            "https://example.com",
            "Link should point to example.com"
        );

        // Assert has class
        Element tabElement = $("//div[@data-tab='settings']");
AssertionHelper.

        assertHasClass(
                tabElement, 
    "selected",
            "Settings tab should be selected"
        );
```

### 2. Sử dụng SoftAssertImpl với auto-retry

`SoftAssertImpl` có built-in auto-retry (mặc định enabled):

```java
import org.example.core.assertion.SoftAssertImpl;
import static org.example.core.control.element.ElementFactory.$;

SoftAssertImpl softAssert = SoftAssertImpl.get();

Element element = $("//div[@id='status']");
String actualText = element.getText();

// Assertion này sẽ tự động retry 1 lần nếu fail
softAssert.assertEquals(actualText, "Ready", "Status should be 'Ready'");

// Continue with more assertions...
softAssert.assertAll(); // Check all assertions at the end
```

### 3. Cấu hình auto-retry

Bạn có thể enable/disable hoặc thay đổi số lần retry:

```java
// Disable auto-retry cho specific test
SoftAssertImpl.setAutoRetryEnabled(false);

// Set custom retry count (default là 1)
SoftAssertImpl.setRetryCount(2); // Retry 2 lần thay vì 1

// Use assertions...
Element element = $("//div[@class='content']");
AssertionHelper.assertTextEquals(element, "Expected", "Message");

// Reset về defaults
SoftAssertImpl.reset();
```

## Các loại assertions hỗ trợ

### Text Assertions
- `assertTextEquals()` - Kiểm tra text bằng chính xác
- `assertTextContains()` - Kiểm tra text chứa substring

### Element State Assertions
- `assertElementVisible()` - Kiểm tra element visible
- `assertElementEnabled()` - Kiểm tra element enabled
- `assertElementSelected()` - Kiểm tra element selected/checked

### Attribute Assertions
- `assertAttribute()` - Kiểm tra attribute bằng chính xác
- `assertAttributeContains()` - Kiểm tra attribute chứa substring
- `assertHasClass()` - Kiểm tra element có class cụ thể

### Value Assertions
- `assertValueEquals()` - Kiểm tra value của input field

## Cơ chế hoạt động

1. **First Attempt**: Assertion được thực thi lần đầu
2. **Nếu fail**: 
   - Đợi 500ms (để UI có thời gian update)
   - Retry assertion 1 lần
3. **Nếu retry thành công**: 
   - Log success message
   - Không đánh dấu là failed
4. **Nếu retry cũng fail**: 
   - Đánh dấu là failed
   - Capture screenshot
   - Log vào report

## Best Practices

1. **Sử dụng AssertionHelper cho UI checks**: 
   - Dễ đọc, dễ maintain
   - Auto-retry built-in
   - Consistent error messages

2. **Sử dụng SoftAssertImpl cho multiple assertions**:
   - Collect nhiều failures
   - Check tất cả ở cuối với `assertAll()`

3. **Disable retry khi cần**:
   - Khi test logic phức tạp
   - Khi muốn fail ngay lập tức

4. **Custom retry count**:
   - Tăng retry count cho flaky elements
   - Giảm retry count cho performance-critical tests

## Ví dụ thực tế

```java
@Step("Verify login success")
public void verifyLoginSuccess() {
    // Assert page title
    Element titleElement = $("//h1[@class='page-title']");
    AssertionHelper.assertTextEquals(
        titleElement, 
        "Dashboard", 
        "Page title should be 'Dashboard'"
    );
    
    // Assert user menu visible
    Element userMenu = $("//div[@class='user-menu']");
    AssertionHelper.assertElementVisible(
        userMenu, 
        "User menu should be visible after login"
    );
    
    // Assert user menu has active class
    AssertionHelper.assertHasClass(
        userMenu, 
        "active", 
        "User menu should be active"
    );
}
```

## Lưu ý

- Auto-retry chỉ áp dụng cho **soft assertions**
- Retry delay mặc định là **500ms**
- Retry count mặc định là **1 lần**
- Retry chỉ áp dụng cho UI assertions, không áp dụng cho business logic assertions

## So sánh với thực tế

Trong thực tế, nhiều framework test automation cũng implement auto-retry:
- **Selenium**: Retry mechanism trong WebDriverWait
- **Playwright**: Auto-retry cho actions và assertions
- **Cypress**: Built-in retry cho commands
- **TestNG**: RetryAnalyzer cho test methods

Framework này implement auto-retry ở **assertion level**, giúp:
- Flexible hơn (có thể enable/disable per test)
- Consistent với soft assertions
- Dễ debug (log rõ ràng khi retry)

