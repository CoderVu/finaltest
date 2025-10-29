# Migration Guide - From @Step to TestReporter

## Current State
Page Objects use Allure's `@Step` annotation which couples code to Allure framework.

## Migration Steps

### Step 1: Use TestReporter in Page Objects

**Option A: Add to each page class**
```java
public class LoginPage {
    private TestReporter reporter = ReporterFactory.getInstance();
    
    public void login(String username, String password) {
        reporter.logStep("Logging in with username: " + username);
        // ... existing code ...
    }
}
```

**Option B: Inherit from BasePage (already done)**
```java
public class LoginPage extends BasePage {
    public void login(String username, String password) {
        reporter.logStep("Logging in with username: " + username);
        // ... existing code ...
    }
}
```

### Step 2: Replace @Step annotations

**Before:**
```java
@Step("Enter username: {username}")
public void enterUsername(String username) {
    usernameField.setText(username);
}
```

**After:**
```java
public void enterUsername(String username) {
    reporter.logStep("Enter username: " + username);
    usernameField.setText(username);
}
```

### Step 3: Run tests to verify

```bash
# Test with Allure
mvn test

# Test with ExtentReports
mvn test -Dreport.strategy=extent

# Test with Jenkins
mvn test -Dreport.strategy=jenkins
```

## Benefits After Migration

✅ Page Objects work with ANY reporting framework  
✅ No need to recompile when switching reporters  
✅ Cleaner code - no annotations needed  
✅ Better testability

## API Reference

### TestReporter Methods

```java
// Log a step
reporter.logStep("User clicks login button");

// Log information
reporter.logInfo("Expected 5 items in cart");

// Log failure
reporter.logFail("Login failed", exception);

// Attach screenshot
reporter.attachScreenshot("login-page");
```
