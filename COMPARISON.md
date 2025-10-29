# So Sánh AllureReporter vs AllureReportStrategy

## Sự Khác Biệt

| Feature | AllureReporter.java | AllureReportStrategy.java |
|---------|---------------------|---------------------------|
| **Mục đích** | Dùng trong Page Objects/Test | TestNG Listener tự động |
| **Cách gọi** | Active (bạn gọi) | Passive (TestNG gọi) |
| **Ví dụ** | `reporter.logStep("Login")` | `onTestFailure()` khi test fail |
| **Interface** | `TestReporter` | `ReportStrategy` (ITestListener) |

## Trùng Lặp Code

### Cả 2 đều có:
```java
// AllureReporter.java
public void attachScreenshot(String name) {
    WebDriver driver = WebDriverRunner.getWebDriver();
    byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
}

// AllureReportStrategy.java (giống y hệt!)
public static void attachScreenshot(String name) {
    WebDriver driver = WebDriverRunner.getWebDriver();
    byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
}
```

## Khi Nào Dùng Cái Nào?

### AllureReporter
```java
// Trong Page Objects
public class LoginPage extends BasePage {
    public void login() {
        reporter.logStep("Login to application");  // ← AllureReporter
        reporter.info("Username: admin");          // ← AllureReporter
    }
}
```

### AllureReportStrategy
```java
// Tự động chạy bởi TestNG khi test fail
@Override
public void onTestFailure(ITestResult result) {
    // Tự động chụp screenshot khi test fail
    captureScreenshotOnFailure(result, "Failed");
}
```

## Vấn Đề

❌ **Có trùng lặp code** - Screenshot attachment code lặp lại

❌ **Allure không cần Strategy** - Allure tự động tích hợp TestNG, không cần listener

❌ **Phức tạp không cần thiết** - 2 file cho 1 mục đích

## Đề Xuất

### Option 1: XÓA AllureReportStrategy (Khuyến nghị)
Vì:
- Allure tự động hook với TestNG
- Không cần listener để chụp screenshot (có thể dùng @AfterMethod)
- Giảm complexity

### Option 2: MERGE screenshot logic
Nếu muốn auto screenshot khi fail:
```java
// Trong AllureReporter
public void captureFailureScreenshot(Throwable error) {
    attachScreenshot("Failure Screenshot");
    logFail("Test failed", error);
}
```

### Option 3: GIỮ CẢ 2
Nếu muốn:
- Auto screenshot khi fail (Strategy)
- Manual screenshot trong code (Reporter)

## Kết Luận

**Có cần cả 2 không?**

Với **Allure**: KHÔNG CẦN Strategy
- Allure tự động tích hợp với TestNG
- Chỉ cần AllureReporter cho manual logging
- Auto screenshot có thể làm qua @AfterMethod

Với **Extent**: CẦN Strategy
- Phải tạo ExtentTest instance cho mỗi test
- Không làm được nếu không có listener

**Khuyến nghị**: Xóa AllureReportStrategy, chỉ giữ AllureReporter
