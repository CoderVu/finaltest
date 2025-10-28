package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import java.util.Scanner;

@Slf4j
public class ExecutionConfig {

    private static final boolean DEFAULT_HEADLESS = false;

    private static final String BROWSER_PROPERTY = "browser";
    private static final String BASE_URL_PROPERTY = "base.url";
    private static final String HEADLESS_PROPERTY = "headless";
    private static final String TIMEOUT_PROPERTY = "timeout";
    private static final String PAGE_LOAD_TIMEOUT_PROPERTY = "page.load.timeout";
    private static final String ENV_FILE_PROPERTY = "env.file";
    private static final String REMOTE_ENABLED_PROPERTY = "remote.enabled";
    private static final String REMOTE_URL_PROPERTY = "remote.url";
    private static final String W3C_ENABLED_PROPERTY = "w3c.enabled";
    private static final String GRID_ENABLED_PROPERTY = "grid.enabled";

    private static final Scanner scanner = new Scanner(System.in);

    public static String getBrowser() {
        try {
            if (org.testng.Reporter.getCurrentTestResult() != null
                    && org.testng.Reporter.getCurrentTestResult().getTestContext() != null
                    && org.testng.Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest() != null) {
                String param = org.testng.Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("browser");
                if (param != null && !param.trim().isEmpty()) {
                    log.info("ExecutionConfig: Using TestNG parameter browser: {}", param);
                    return param.trim().toLowerCase();
                }
            }
        } catch (Throwable ignored) {}

        String singleBrowser = System.getProperty("single.browser");
        if (singleBrowser != null && !singleBrowser.trim().isEmpty()) {
            String selected = System.getProperty(BROWSER_PROPERTY);
            if (selected != null && !selected.trim().isEmpty()) {
                log.info("ExecutionConfig: Using single browser mode: {}", selected);
                return selected.trim().toLowerCase();
            }
        }

        String browserFromCmd = System.getProperty(BROWSER_PROPERTY);
        if (browserFromCmd != null && !browserFromCmd.trim().isEmpty()) {
            log.info("ExecutionConfig: Using system property browser: {}", browserFromCmd);
            return browserFromCmd.trim().toLowerCase();
        }

        String defaultBrowser = Constants.getDefaultBrowser();
        if (defaultBrowser != null && !defaultBrowser.trim().isEmpty()) {
            log.info("ExecutionConfig: Using default browser: {}", defaultBrowser);
            return defaultBrowser.trim().toLowerCase();
        }

        log.info("ExecutionConfig: Using fallback browser: chrome");
        return "chrome";
    }

    public static String getBaseUrl() {
        String baseUrl = System.getProperty(BASE_URL_PROPERTY);
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            return baseUrl.trim();
        }
        return Constants.getBaseUrl();
    }

    public static boolean isHeadless() {
        String headless = System.getProperty(HEADLESS_PROPERTY, String.valueOf(DEFAULT_HEADLESS));
        return Boolean.parseBoolean(headless);
    }

    public static long getTimeout() {
        String timeout = System.getProperty(TIMEOUT_PROPERTY, String.valueOf(Constants.DEFAULT_TIMEOUT));
        return Long.parseLong(timeout);
    }

    public static long getPageLoadTimeout() {
        String timeout = System.getProperty(PAGE_LOAD_TIMEOUT_PROPERTY, String.valueOf(Constants.DEFAULT_PAGE_LOAD_TIMEOUT));
        return Long.parseLong(timeout);
    }

    public static String getEnvFile() {
        String file = System.getProperty(ENV_FILE_PROPERTY);
        if (file != null && !file.trim().isEmpty()) {
            return file.trim();
        }
        return Constants.CONFIG_PROPERTIES_FILE;
    }

    public static boolean isRemoteEnabled() {
        String enabled = System.getProperty(REMOTE_ENABLED_PROPERTY, "false");
        return Boolean.parseBoolean(enabled);
    }

    public static String getRemoteUrl() {
        return System.getProperty(REMOTE_URL_PROPERTY, "");
    }

    public static boolean isW3CEnabled() {
        String enabled = System.getProperty(W3C_ENABLED_PROPERTY, "true");
        return Boolean.parseBoolean(enabled);
    }

    public static boolean isGridEnabled() {
        String enabled = System.getProperty(GRID_ENABLED_PROPERTY, "false");
        return Boolean.parseBoolean(enabled);
    }

    public static boolean shouldSkipBrowser() {
        String singleBrowserMode = System.getProperty("single.browser");
        if (singleBrowserMode == null || !Boolean.parseBoolean(singleBrowserMode)) {
            return false;
        }

        String selectedBrowser = System.getProperty("browser");
        String currentBrowser = getBrowser();
        if (selectedBrowser != null && !selectedBrowser.trim().isEmpty()) {
            boolean shouldSkip = !selectedBrowser.trim().toLowerCase().equals(currentBrowser.toLowerCase());
            if (shouldSkip) {
                log.info("Skipping browser: {} (Selected: {})", currentBrowser, selectedBrowser);
            }
            return shouldSkip;
        }
        return false;
    }

    public static String resolveBaseUrlForSelenide() {
        String url = getBaseUrl();
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Base URL is not configured. Please set it in environment file or system property.");
        }
        url = url.trim();
        if (isRemoteEnabled()) {
            if (url.startsWith("http://localhost")) {
                url = url.replaceFirst("http://localhost", "http://host.docker.internal");
            } else if (url.startsWith("http://127.0.0.1")) {
                url = url.replaceFirst("http://127.0.0.1", "http://host.docker.internal");
            }
        }
        return url;
    }

    // ---------------- Interactive CLI config (moved from RunConfig) ----------------
    public static void configureInteractive() {
        print("\n=== VuNguyenCoder ===", true);
        configureEnvironmentFile();
        configureExecutionMode();
        configureBrowser();
        configureCleanReports();
        print("\n=== Configuration Complete ===", true);
    }

    private static void print(String message, boolean newline) {
        if (newline) {
            System.out.println(message);
        } else {
            System.out.print(message);
        }
    }

    private static void configureEnvironmentFile() {
        print("\n1. Select Environment File:", true);
        print("   1) dev-env.yaml", true);
        print("   2) prod-env.yaml", true);
        print("   3) staging-env.yaml", true);
        print("   4) Custom file path", true);
        print("Enter your choice (1-4): ", false);
        String choice = scanner.nextLine().trim();
        String envFile = "dev-env.yaml";

        switch (choice) {
            case "1":
                envFile = "dev-env.yaml";
                break;
            case "2":
                envFile = "prod-env.yaml";
                break;
            case "3":
                envFile = "staging-env.yaml";
                break;
            case "4":
                print("Enter custom environment file path: ", false);
                String customPath = scanner.nextLine().trim();
                if (!customPath.isEmpty()) {
                    envFile = customPath;
                }
                break;
            default:
                print("Invalid choice, using default: dev-env.yaml", true);
                envFile = "dev-env.yaml";
                break;
        }

        System.setProperty("env.file", envFile);
        print("Selected environment file: " + envFile, true);
    }

    private static void configureExecutionMode() {
        print("\n2. Select Execution Mode:", true);
        print("   1) Local execution", true);
        print("   2) Remote Grid execution", true);
        print("Enter your choice (1-2): ", false);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.setProperty("remote.enabled", "false");
                System.setProperty("grid.enabled", "false");
                print("Selected: Local execution", true);
                break;
            case "2":
                System.setProperty("remote.enabled", "true");
                System.setProperty("grid.enabled", "true");
                configureRemoteUrl();
                print("Selected: Remote Grid execution", true);
                break;
            default:
                print("Invalid choice, using default: Local execution", true);
                System.setProperty("remote.enabled", "false");
                System.setProperty("grid.enabled", "false");
                break;
        }
    }

    private static void configureRemoteUrl() {
        print("\n   Remote Grid Configuration:", true);
        print("   1) localhost:4444 (default)", true);
        print("   2) Custom URL", true);
        print("   Enter your choice (1-2): ", false);

        String choice = scanner.nextLine().trim();
        String remoteUrl = "http://localhost:4444/wd/hub";

        switch (choice) {
            case "1":
                remoteUrl = "http://localhost:4444/wd/hub";
                break;
            case "2":
                print("   Enter remote URL: ", false);
                String customUrl = scanner.nextLine().trim();
                if (!customUrl.isEmpty()) {
                    remoteUrl = customUrl;
                }
                break;
            default:
                print("   Using default: http://localhost:4444/wd/hub", true);
                break;
        }

        System.setProperty("remote.url", remoteUrl);
        print("   Remote URL: " + remoteUrl, true);
    }

    private static void configureBrowser() {
        print("\n3. Select Browser Execution:", true);
        print("   1) Single browser (Chrome)", true);
        print("   2) Single browser (Firefox)", true);
        print("   3) Single browser (Edge)", true);
        print("   4) All browsers (Parallel)", true);
        print("Enter your choice (1-4): ", false);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.setProperty("browser", "chrome");
                System.setProperty("single.browser", "true");
                print("Selected: Chrome only", true);
                break;
            case "2":
                System.setProperty("browser", "firefox");
                System.setProperty("single.browser", "true");
                print("Selected: Firefox only", true);
                break;
            case "3":
                System.setProperty("browser", "edge");
                System.setProperty("single.browser", "true");
                print("Selected: Edge only", true);
                break;
            case "4":
                System.setProperty("single.browser", "false");
                print("Selected: All browsers (Parallel execution)", true);
                break;
            default:
                print("Invalid choice, using default: Chrome only", true);
                System.setProperty("browser", "chrome");
                System.setProperty("single.browser", "true");
                break;
        }

        configureHeadlessMode();
    }

    private static void configureHeadlessMode() {
        print("\n4. Headless Mode:", true);
        print("   1) Normal mode (with GUI)", true);
        print("   2) Headless mode (no GUI)", true);
        print("Enter your choice (1-2): ", false);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.setProperty("headless", "false");
                print("Selected: Normal mode (with GUI)", true);
                break;
            case "2":
                System.setProperty("headless", "true");
                print("Selected: Headless mode (no GUI)", true);
                break;
            default:
                print("Using default: Normal mode", true);
                System.setProperty("headless", "false");
                break;
        }
    }

    private static void configureCleanReports() {
        print("\n5. Clean previous reports?", true);
        print("   1. Yes - Clean previous test results (mvn clean test)", true);
        print("   2. No - Keep previous test results (mvn test)", true);
        print("Select clean option (1-2): ", false);

        String cleanChoice = scanner.nextLine().trim();
        switch (cleanChoice) {
            case "1":
                System.setProperty("clean.reports", "true");
                print("✓ Clean reports enabled", true);
                break;
            case "2":
                System.setProperty("clean.reports", "false");
                print("✓ Clean reports disabled", true);
                break;
            default:
                System.setProperty("clean.reports", "true");
                print("✓ Default: Clean reports enabled", true);
                break;
        }
    }
}


