# Reporter Usage Guide

The framework now ships with a single reporting backend: **ExtentReports**. The goal is to keep configuration simple while still providing structured logs, screenshots, and a shareable HTML report.

## Key Components (`org.example.core.report`)
- `IReporter`: small API for logging steps/info/failures and attaching screenshots.
- `AbstractReporter` + `ExtentReporter`: implementation that writes to Extent.
- `ReportManager`: returns a singleton `IReporter` and caches the active TestNG listener strategy.
- `ReportListener` → `ExtentStrategyI`: TestNG listener registered in `testng.xml` that listens to suite/test events and feeds the Extent lifecycle (create test, flush report, etc.).

## Configuration
- `reportType` property/system property/env var now accepts a single value: `extent`.
- Default is `extent`, so no change is required unless you override it somewhere.
- Reports are written to `target/extent-report/<timestamp>/index_<timestamp>.html`.

## Typical Usage

```java
import org.example.core.report.IReporter;
import org.example.core.report.ReportManager;

public class LoginPage {
    private final IReporter reporter = ReportManager.getReporter();

    public void login(String username, String password) {
        reporter.info("Entering username: " + username);
        userField.setText(username);

        reporter.info("Entering password");
        passwordField.setText(password);

        reporter.logStep("Click login button");
        loginButton.click();
    }
}
```

Because `ReportManager` caches the reporter, you can safely access it from any page/helper without worrying about lifecycle or threading.

## Screenshots
- `ExtentReporter` automatically captures a screenshot when `logFail` is invoked (e.g., during TestNG failures handled by `ExtentStrategyI`).
- You can manually call `reporter.attachScreenshot("optional-name")` at any point to embed evidence inside the report.

## Tips
- Keep log messages action-focused (`logStep`) and detail-oriented (`info`).
- When running parallel tests, each thread receives its own `ExtentTest` node automatically (handled in `ExtentStrategyI`).
- Open the latest HTML file after each run; screenshots are stored under the sibling `screenshots/` folder.
