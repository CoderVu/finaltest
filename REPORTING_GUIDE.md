# Reporting Framework Guide

## 📋 Table of Contents

1. [Using Current Reporting](#using-current-reporting)
2. [How to Add New Report Engine (e.g., Allure)](#how-to-add-new-report-engine)
3. [Retry Mechanism and Reporting](#retry-mechanism-and-reporting)

---

## Using Current Reporting

### 1. Using in PageObject (Recommended)

Framework tự động tạo step khi methods trong PageObject được gọi. Sử dụng đơn giản như `Allure.step()`.

#### Method 1: Wrap Method Body in `step()`

```java
public class LoginPage extends BasePage {
    
    public void login(String username, String password) {
        step(() -> {
            enterUsername(username);
            enterPassword(password);
            clickLogin(); // Tự động trở thành child step của login()
        });
    }
    
    private void enterUsername(String username) {
        step(() -> {
            usernameField.setText(username);
            log("Username entered: " + username);
        });
    }
    
    private void enterPassword(String password) {
        step(() -> {
            passwordField.setText(password);
        });
    }
    
    private void clickLogin() {
        step(() -> {
            loginButton.click();
            waitForPageLoad();
        });
    }
}
```

**Result in report:**
```
Login
  ├── Enter Username
  ├── Enter Password
  └── Click Login
```

#### Method 2: Step with Custom Name

```java
public void searchHotel(String destination) {
    step("Search for hotel in " + destination, () -> {
        destinationField.setText(destination);
        searchButton.click();
    });
}
```

#### Method 3: Step with Return Value

```java
public String getHotelName() {
    return step("Get hotel name", () -> {
        return hotelNameElement.getText();
    });
}
```

### 2. Using Reporter Directly

```java
import org.example.core.reporting.ReportClient;
import org.example.core.reporting.ReportingManager;

public class MyPage extends BasePage {
    
    private final ReportClient reporter = ReportingManager.getReportClient();
    
    public void doSomething() {
        // Log step chính
        reporter.logStep("Doing something");
        
        // Log thông tin chi tiết
        reporter.info("Additional information");
        
        // Sử dụng childStep cho các bước phức tạp
        reporter.childStep("Complex operation", () -> {
            // ... code ...
        });
        
        // Attach screenshot
        reporter.attachScreenshot("screenshot_name");
        
        // Log failure
        reporter.logFail("Something failed", new Exception());
    }
}
```

### 3. Configure Report Type

In properties file (e.g., `dev.properties`):

```properties
report.type=extent
```

Hoặc qua system property:

```bash
mvn test -Dreport.type=extent
```

### 4. View Report

After running tests, report is generated at:

```
target/extent-report/<timestamp>/index_<timestamp>.html
```

Open this HTML file in browser to view the report.

---

## How to Add New Report Engine

The framework is designed with plugin architecture, making it easy to add new report engines (e.g., Allure) without modifying common code.

### Architecture

```
ReportingManager (Singleton)
    ↓
ReportPluginRegistry (ServiceLoader)
    ↓
ReportPlugin (Interface)
    ├── createReporter() → ReportClient
    └── createLifecycleListener() → ReportingLifecycleListener
```

### Step 1: Add ReportType to Enum

```java
// src/main/java/org/example/enums/ReportType.java

public enum ReportType {
    EXTENT("extent", "com.aventstack.extentreports.ExtentTest", "target/extent"),
    ALLURE("allure", "io.qameta.allure.Allure", "target/allure-results"); // ← Thêm mới
    
    // ... existing code ...
}
```

### Step 2: Create Package and Classes

Create new package with corresponding prefix:

```
src/main/java/org/example/core/reporting/
└── allure/                              # Package mới với prefix "allure"
    ├── AllureReportClient.java          # Implement ReportClient
    ├── AllureReportLifecycle.java       # Implement ReportingLifecycleListener
    └── AllureReportPlugin.java          # Implement ReportPlugin
```

### Step 3: Implement AllureReportClient

```java
package org.example.core.reporting.allure;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.BaseReportClient;
import org.example.enums.ReportType;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.ByteArrayInputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class AllureReportClient extends BaseReportClient {
    
    private static final String STEP_STACK_ATTRIBUTE = "reporting.allure.stepStack";
    
    public AllureReportClient() {
        super(ReportType.ALLURE);
    }
    
    @Override
    public void logStep(String message) {
        Allure.step("STEP: " + message);
    }
    
    @Override
    public void info(String message) {
        Allure.step("INFO: " + message);
    }
    
    @Override
    public void logFail(String message, Throwable error) {
        Allure.step("FAIL: " + message, () -> {
            if (error != null) {
                throw error;
            }
            throw new AssertionError(message);
        });
    }
    
    @Override
    public void attachScreenshot(String name) {
        try {
            TakesScreenshot driver = (TakesScreenshot) getWebDriver();
            if (driver != null) {
                byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
                String filename = (name == null || name.isEmpty()) 
                    ? "screenshot_" + System.currentTimeMillis() + ".png" 
                    : name;
                Allure.addAttachment(filename, "image/png", 
                    new ByteArrayInputStream(screenshot));
            }
        } catch (Exception e) {
            log.warn("Failed to attach screenshot: {}", e.getMessage());
        }
    }
    
    @Override
    public void childStep(String name, Runnable runnable) {
        Allure.step(name, runnable);
    }
    
    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        return Allure.step(name, supplier::get);
    }
    
    @Override
    public boolean isInStep() {
        // Allure doesn't have explicit step stack tracking
        return false;
    }
}
```

### Step 4: Implement AllureReportLifecycle

```java
package org.example.core.reporting.allure;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;
import org.testng.ITestContext;
import org.testng.ITestResult;

@Slf4j
public class AllureReportLifecycle implements ReportingLifecycleListener {
    
    @Override
    public void onStart(ITestContext context) {
        log.info("Allure reporting initialized");
    }
    
    @Override
    public void onFinish(ITestContext context) {
        log.info("Allure reporting finished");
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null 
            ? result.getTestClass().getName() 
            : "<unknown>";
        String fullTestName = testClass + "." + testName;
        
        Allure.getLifecycle().startTestCase(fullTestName);
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(fullTestName);
        });
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setStatus(io.qameta.allure.model.Status.PASSED);
        });
        Allure.getLifecycle().stopTestCase();
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setStatus(io.qameta.allure.model.Status.FAILED);
            if (error != null) {
                testResult.setStatusDetails(new io.qameta.allure.model.StatusDetails()
                    .setMessage(error.getMessage())
                    .setTrace(error.getStackTrace().toString()));
            }
        });
        Allure.getLifecycle().stopTestCase();
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setStatus(io.qameta.allure.model.Status.SKIPPED);
        });
        Allure.getLifecycle().stopTestCase();
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestFailure(result);
    }
    
    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        onTestFailure(result);
    }
    
    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        // Allure doesn't need special handling for configuration methods
    }
    
    @Override
    public void onConfigurationFailure(ITestResult itr) {
        Throwable t = itr.getThrowable();
        Allure.step("Configuration failed: " + (t != null ? t.getMessage() : "Unknown"), () -> {
            if (t != null) {
                throw t;
            }
        });
    }
    
    @Override
    public void onConfigurationSkip(ITestResult itr) {
        // Allure doesn't need special handling for skipped configuration
    }
    
    @Override
    public void failStep(String stepName) {
        Allure.step("FAIL: " + stepName, () -> {
            throw new AssertionError(stepName);
        });
    }
}
```

### Step 5: Implement AllureReportPlugin

```java
package org.example.core.reporting.allure;

import org.example.core.reporting.BaseReportClient;
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;
import org.example.core.reporting.plugin.ReportPlugin;
import org.example.enums.ReportType;

public class AllureReportPlugin implements ReportPlugin {
    
    @Override
    public ReportType getType() {
        return ReportType.ALLURE;
    }
    
    @Override
    public BaseReportClient createReporter() {
        return new AllureReportClient();
    }
    
    @Override
    public ReportingLifecycleListener createLifecycleListener() {
        return new AllureReportLifecycle();
    }
}
```

### Step 6: Register Plugin

**File:** `src/main/resources/META-INF/services/org.example.core.reporting.plugin.ReportPlugin`

Add new line:

```
org.example.core.reporting.extent.ExtentReportPlugin
org.example.core.reporting.allure.AllureReportPlugin
```

### Step 7: Configure

Set property to choose report engine:

```properties
report.type=allure
```

Or via system property:

```bash
mvn test -Dreport.type=allure
```

### Step 8: Add Dependency (if needed)

Add Allure dependency to `pom.xml`:

```xml
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-testng</artifactId>
    <version>2.24.0</version>
</dependency>
```

### Done!

Now the framework will automatically:
1. Load `AllureReportPlugin` from ServiceLoader
2. Create `AllureReportClient` and `AllureReportLifecycle`
3. Use Allure to generate report

**Note:**
- No need to modify common code (`ReportingListener`, `ReportingManager`, etc.)
- Just implement 3 interfaces and register plugin
- Can have multiple report engines at the same time, choose via config

---

## Retry Mechanism and Reporting

### How Retry Works with Reporting

The `MyRetryAnalyzer` is integrated with the reporting framework:

1. **MyRetryAnalyzer** tracks retry attempts and sets `retry.attempt` attribute on `ITestResult`
2. **ExtentReportLifecycle** reads `retry.attempt` to create attempt nodes in the report
3. Each retry attempt gets its own node in the report (e.g., "Attempt 1", "Attempt 2", "Attempt 3")

### Configuration

Set maximum retry attempts in properties file:

```properties
max_num_of_attempts=3
```

### How It Works

```
Test fails
  ↓
MyRetryAnalyzer.retry() is called
  ↓
Sets retry.attempt = 1, 2, 3, etc.
  ↓
ExtentReportLifecycle.onTestStart() reads retry.attempt
  ↓
Creates "Attempt 1", "Attempt 2", etc. nodes in report
  ↓
All steps and screenshots are logged under the attempt node
```

### Report Structure

```
Test Name
├── Attempt 1
│   ├── Step 1
│   ├── Step 2
│   └── FAIL (with screenshot)
├── Attempt 2
│   ├── Step 1
│   ├── Step 2
│   └── FAIL (with screenshot)
└── Attempt 3
    ├── Step 1
    ├── Step 2
    └── PASS
```

**Note:** `MyRetryAnalyzer` is not part of the reporting framework, but it provides retry attempt information that the reporting framework uses to organize the report.

---

## Framework Structure

### Common Parts (No prefix)

- `ReportClient` - Common interface
- `BaseReportClient` - Common abstract class
- `ReportingLifecycleListener` - Lifecycle interface
- `ReportPlugin` - Plugin interface
- `ReportingManager` - Singleton manager
- `ReportPluginRegistry` - Plugin discovery
- `ReportClientFactory` - Factory
- `ReportingListener` - Common TestNG listener

### Implementation-Specific Parts (With prefix)

- `extent/` - ExtentReports implementation
  - `ExtentReportClient`
  - `ExtentReportLifecycle`
  - `ExtentReportPlugin`
- `allure/` - Allure implementation (when added)
  - `AllureReportClient`
  - `AllureReportLifecycle`
  - `AllureReportPlugin`

### Service Files

1. **`META-INF/services/org.testng.ITestNGListener`**
   ```
   org.example.core.reporting.listeners.ReportingListener
   ```
   - Common TestNG listener, no need to modify when adding new report engine

2. **`META-INF/services/org.example.core.reporting.plugin.ReportPlugin`**
   ```
   org.example.core.reporting.extent.ExtentReportPlugin
   org.example.core.reporting.allure.AllureReportPlugin
   ```
   - Register plugins, add new line when adding report engine

---

## How It Works

```
1. TestNG starts
   ↓
2. ServiceLoader loads ReportPlugin from META-INF/services
   ↓
3. ReportPluginRegistry registers plugins
   ↓
4. ReportingListener (TestNG listener) is called
   ↓
5. ReportingListener calls ReportingManager.getLifecycleListener()
   ↓
6. ReportingManager gets plugin by ReportType from config
   ↓
7. Plugin creates ReportingLifecycleListener (ExtentReportLifecycle or AllureReportLifecycle)
   ↓
8. ReportingListener delegates events to lifecycle listener
   ↓
9. Framework code calls ReportingManager.getReportClient()
   ↓
10. ReportingManager creates ReportClient from plugin (ExtentReportClient or AllureReportClient)
```

---

## Tips

1. **Use `step()` in PageObject**: Simple and automatically creates hierarchy
2. **Take screenshot when needed**: `reporter.attachScreenshot("name")`
3. **Log detailed information**: `reporter.info("message")`
4. **Use childStep for complex logic**: Automatically creates nested steps
5. **Switch report engine via config**: No need to modify code

