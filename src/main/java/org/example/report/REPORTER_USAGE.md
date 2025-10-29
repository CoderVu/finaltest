# Reporter Usage Guide

## Structure Overview

```
report/
  ├── TestReporter.java          # Interface for all reporters
  ├── ReporterFactory.java       # Factory to create appropriate reporter
  └── impl/
      ├── AllureReporter.java      # Allure implementation (full features)
      ├── ExtentReporter.java      # Extent implementation (full features)
      └── JenkinsReporter.java     # Jenkins implementation (full features)
```

## Basic Usage

### In Page Objects

```java
package org.example.pages;

import org.example.report.ReporterFactory;
import org.example.report.TestReporter;

public class LoginPage extends BasePage {
    
    // Already available in BasePage
    // protected TestReporter reporter = ReporterFactory.getInstance();
    
    public void login(String username, String password) {
        // Main step
        reporter.logStep("Login to application");
        
        // Nested info/data
        reporter.info("Entering username: " + username);
        enterText(usernameField, username);
        
        reporter.info("Entering password");
        enterText(passwordField, password);
        
        // Main step
        reporter.logStep("Click login button");
        clickElement(loginButton);
    }
    
    public void verifyLoginSuccess() {
        reporter.logStep("Verify login successful");
        reporter.info("Expected: Dashboard page");
        // verification code
    }
}
```

## Switching Report Types

No code changes needed! Just set the system property:

### Allure (default)
```bash
mvn test
# or explicitly
mvn test -Dreport.strategy=allure
```
Output: `target/allure-results/`  
View: `mvn allure:serve`

### ExtentReports
```bash
mvn test -Dreport.strategy=extent
```
Output: `target/extent/index.html`

### Jenkins
```bash
mvn test -Dreport.strategy=jenkins
```
Output: `target/surefire-reports/` (XML for Jenkins)

## Advanced Usage

### Using Reporter Classes Directly

If you need framework-specific features, you can cast to the specific reporter:

```java
import org.example.report.impl.AllureReporter;
import org.example.report.impl.ExtentReporter;
import org.example.report.impl.JenkinsReporter;

// Get the reporter
TestReporter reporter = ReporterFactory.getInstance();

// Allure-specific features
if (reporter instanceof AllureReporter) {
    AllureReporter allure = (AllureReporter) reporter;
    allure.logPass("Test passed");
    allure.logWarn("Warning occurred");
    allure.addDescription("Test description");
}

// Extent-specific features
if (reporter instanceof ExtentReporter) {
    ExtentReporter extent = (ExtentReporter) reporter;
    extent.logPass("Test passed");
    extent.logWarn("Warning");
    extent.logWithScreenshot("Page loaded", "/path/to/screenshot.png");
}

// Jenkins-specific features
if (reporter instanceof JenkinsReporter) {
    JenkinsReporter jenkins = (JenkinsReporter) reporter;
    jenkins.logPass("Test passed");
    jenkins.logWarn("Warning");
    jenkins.logWithMetadata("Step completed", "{userId: 123}");
}
```

## Key Concepts

### logStep() vs info()

- **`logStep()`**: Log main test actions/operations
  - Example: "Click login button", "Navigate to checkout"
  - Creates top-level steps in reports
  
- **`info()`**: Log nested information or data within steps
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

## Benefits

✅ **Flexible**: Switch between Allure/Extent/Jenkins without code changes  
✅ **Easy to Use**: Simple API - just `logStep()` and `info()`  
✅ **Extensible**: Add custom reporters by implementing `TestReporter`  
✅ **Clean**: No hardcoded framework dependencies in page objects  
✅ **Maintainable**: All reporting logic in one place
