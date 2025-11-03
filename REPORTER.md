# Reporter Usage Guide

## Structure Overview

```
report/
  ├── ITestReporter.java           # Interface for all reporters
  ├── ReportManager.java           # Selects active reporter and strategy globally
  ├── hook/
  │   └── ReportHook.java         # TestNG listener that delegates to ReportStrategy
  ├── strategy/
  │   ├── ReportStrategy.java     # Strategy interface (extends TestNG listeners)
  │   ├── AllureStrategy.java      # Allure report strategy
  │   ├── ExtentStrategy.java     # Extent report strategy
  │   └── JenkinsStrategy.java    # Jenkins report strategy
  ├── annotations/
  │   └── Step.java               # Custom step annotation
  ├── aop/
  │   └── StepAspect.java         # Logs @Step via active reporter
  └── impl/
      ├── AllureTestReporter.java  # Allure implementation
      ├── ExtentTestReporter.java  # Extent implementation
      └── JenkinsTestReporter.java # Jenkins implementation
```

## Basic Usage

### In Page Objects

```java
package org.example.pages;

import org.example.core.report.ReportManager;
import org.example.core.report.ITestReporter;
import org.example.core.report.annotations.Step;

public class LoginPage extends BasePage {

    // Already available in BasePage
    // protected ITestReporter reporter = ReportManager.getReporter();

    @Step("Login with user: {arg0}")
    public void login(String username, String password) {
        reporter.info("Entering username: " + username);
        enterText(usernameField, username);

        reporter.info("Entering password");
        enterText(passwordField, password);

        reporter.logStep("Click login button");
        clickElement(loginButton);
    }

    @Step("Verify login successful")
    public void verifyLoginSuccess() {
        reporter.info("Expected: Dashboard page");
        // verification code
    }
}
```

## Switching Report Types

No code changes needed! Configure via properties file or system property.

### Configuration Methods

1. **Properties file** (recommended):
```properties
# dev-env.properties
reportType=allure
# or
reportType=extent
# or
reportType=jenkins
```

2. **System property** (runtime override):
```bash
mvn test -DreportType=allure
mvn test -DreportType=extent
mvn test -DreportType=jenkins
```

3. **Environment variable**:
```bash
export REPORT_TYPE=allure
mvn test
```

### Report Types

#### Allure (default)
```bash
mvn test
# or explicitly
mvn test -DreportType=allure
```
- **Output:** `target/allure-results/`
- **View:** `mvn allure:serve` or `mvn allure:report`
- **Auto-generate:** Reports automatically generated after successful runs

#### ExtentReports
```bash
mvn test -DreportType=extent
```
- **Output:** `target/extent/index.html`
- **View:** Open HTML file in browser
- **Features:** Interactive dashboard with charts and statistics

#### Jenkins
```bash
mvn test -DreportType=jenkins
```
- **Output:** `target/surefire-reports/` (XML for Jenkins)
- **Integration:** Ready for Jenkins CI/CD pipeline
- **Format:** Surefire XML format

## Report Strategy Pattern

The framework uses **Strategy Pattern** for pluggable report backends:

- **ReportHook**: TestNG listener that receives all test events
- **ReportStrategy**: Interface that handles test lifecycle events
- **ReportManager**: Factory that selects appropriate strategy based on `ReportType`

Each strategy automatically:
- Captures screenshots on test failures
- Logs test start/success/failure/skip events
- Handles configuration failures
- Manages report lifecycle (start/finish)

## Advanced Usage

### Using Reporter Classes Directly

If you need framework-specific features, you can get the reporter instance:

```java
import org.example.core.report.ReportManager;
import org.example.core.report.ITestReporter;
import org.example.core.report.impl.AllureTestReporter;
import org.example.core.report.impl.ExtentTestReporter;
import org.example.core.report.impl.JenkinsTestReporter;

// Get the reporter
ITestReporter reporter = ReportManager.getReporter();

// Check type and use framework-specific features
if (reporter instanceof AllureTestReporter) {
    AllureTestReporter allure = (AllureTestReporter) reporter;
    // Allure-specific methods available
}

if (reporter instanceof ExtentTestReporter) {
    ExtentTestReporter extent = (ExtentTestReporter) reporter;
    // Extent-specific methods available
}

if (reporter instanceof JenkinsTestReporter) {
    JenkinsTestReporter jenkins = (JenkinsTestReporter) reporter;
    // Jenkins-specific methods available
}
```

## Key Concepts

### logStep() vs info()

- **`logStep()`**: Log main test actions/operations
  - Example: "Click login button", "Navigate to checkout"
  - Creates top-level steps in reports
  
- **`info()`**: Log nested information or details within steps
  - Example: "Username: john@example.com", "Total: $150", "Found 5 items"
  - Creates nested/sub-steps in reports

### Example Flow

```java
reporter.logStep("Login to application");
  reporter.info("Username: admin@example.com");
  reporter.info("Password: ********");
  enterCredentials();
  
reporter.logStep("Verify dashboard loaded");
  reporter.info("Expected URL: /dashboard");
  verifyURL();
```

This creates a hierarchical structure:
```
✓ Login to application
  • Username: admin@example.com
  • Password: ********
✓ Verify dashboard loaded
  • Expected URL: /dashboard
```

### Screenshots

Screenshots are automatically captured on:
- Test failures
- Test skips
- Test timeouts
- Configuration failures

No manual intervention needed - handled by `ReportStrategy` implementations.

## Benefits

✅ **Flexible**: Switch between Allure/Extent/Jenkins without code changes  
✅ **Easy to Use**: Simple API - just `logStep()` and `info()`  
✅ **Extensible**: Add custom reporters by implementing `ITestReporter` and `ReportStrategy`  
✅ **Clean**: No hardcoded framework dependencies in page objects  
✅ **Maintainable**: All reporting logic in one place  
✅ **Automatic**: Screenshots and error logging handled automatically
