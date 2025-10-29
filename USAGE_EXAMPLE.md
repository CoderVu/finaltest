# Report Strategy Pattern - Usage Example

## How to use in Page Objects

### Before (Hardcoded Allure):
```java
public class LoginPage {
    public void enterUsername(String username) {
        Allure.step("Entering username: " + username);
        // ... code ...
    }
}
```

### After (Strategy Pattern):
```java
package org.example.pages;

import org.example.report.ReporterFactory;
import org.example.report.TestReporter;

public class LoginPage {
    private TestReporter reporter = ReporterFactory.getInstance();
    
    public void enterUsername(String username) {
        reporter.logStep("Entering username: " + username);
        // ... code ...
    }
    
    public void clickLogin() {
        reporter.logStep("Clicking login button");
        // ... code ...
    }
    
    public void verifyLoginSuccess() {
        reporter.logInfo("Verifying login was successful");
        // ... code ...
    }
    
    public void handleError(String message, Exception e) {
        reporter.logFail(message, e);
    }
}
```

## Switch between reporters

### 1. Allure (default)
```bash
mvn test
# or explicitly
mvn test -Dreport.strategy=allure
```
Output: `target/allure-results/`

### 2. ExtentReports
```bash
mvn test -Dreport.strategy=extent
```
Output: `target/extent/index.html`

### 3. Jenkins
```bash
mvn test -Dreport.strategy=jenkins
```
Output: `target/surefire-reports/` (Jenkins will parse)

## Programmatic Access (with Enum)

You can also check the current report type programmatically:

```java
import org.example.enums.ReportType;

// Get current report type
ReportType currentType = ReportType.getConfigured();
if (currentType == ReportType.EXTENT) {
    // Do something specific for Extent
}

// Parse from string
ReportType type = ReportType.fromString("extent");

// Get output path
String outputPath = ReportType.EXTENT.getOutputPath();
```

## Benefits

✅ **No code changes** when switching reporters  
✅ **Unified API** - same methods for all frameworks  
✅ **Easy to extend** - add new reporters by implementing TestReporter  
✅ **Page Objects remain framework-agnostic**

## Folder Structure
```
src/main/java/org/example/report/
├── TestReporter.java           # Interface
├── ReporterFactory.java        # Factory
└── impl/
    ├── AllureReporter.java     # Allure implementation
    ├── ExtentReporter.java     # ExtentReports implementation
    └── JenkinsReporter.java    # Jenkins implementation
```
