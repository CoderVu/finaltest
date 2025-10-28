package org.example.configure;

import java.util.Scanner;

public class RunConfig {
    private static final Scanner scanner = new Scanner(System.in);

    private static void print(String message, boolean newline) {
        if (newline) {
            System.out.println(message);
        } else {
            System.out.print(message);
        }
    }

    public static void configure() {

        print("\n=== VuNguyenCoder ===", true);

        configureEnvironmentFile();

        configureExecutionMode();

        configureBrowser();

        configureCleanReports();

        print("\n=== Configuration Complete ===", true);
    }

    private static void configureEnvironmentFile() {

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
                print("Enter custom environment file path: ", false);
                String customPath = scanner.nextLine().trim();
                if (!customPath.isEmpty()) {
                    envFile = customPath;
                }
                break;
            default:
                print("Invalid choice, using default: dev-env.yaml", true);
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

        // Additional options
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