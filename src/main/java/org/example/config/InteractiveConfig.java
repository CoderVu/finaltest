package org.example.config;

import java.util.Scanner;

public class InteractiveConfig {
    private static final Scanner scanner = new Scanner(System.in);

    public static void configure() {
        System.out.println("\n=== Interactive Test Configuration ===");
        
        // Environment file selection
        configureEnvironmentFile();
        
        // Execution mode selection
        configureExecutionMode();
        
        // Browser selection
        configureBrowser();
        
        // Clean reports option
        configureCleanReports();
        
        System.out.println("\n=== Configuration Complete ===\n");
    }

    private static void configureEnvironmentFile() {
        System.out.println("\n1. Select Environment File:");
        System.out.println("   1) dev-env.yaml");
        System.out.println("   2) prod-env.yaml");
        System.out.println("   3) Custom file path");
        System.out.print("Enter your choice (1-3): ");
        
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
                System.out.print("Enter custom file path: ");
                String customPath = scanner.nextLine().trim();
                if (!customPath.isEmpty()) {
                    envFile = customPath;
                }
                break;
            default:
                System.out.println("Invalid choice, using default: dev-env.yaml");
                break;
        }
        
        System.setProperty("env.file", envFile);
        System.out.println("Selected environment file: " + envFile);
    }

    private static void configureExecutionMode() {
        System.out.println("\n2. Select Execution Mode:");
        System.out.println("   1) Local execution");
        System.out.println("   2) Remote Grid execution");
        System.out.print("Enter your choice (1-2): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.setProperty("remote.enabled", "false");
                System.setProperty("grid.enabled", "false");
                System.out.println("Selected: Local execution");
                break;
            case "2":
                System.setProperty("remote.enabled", "true");
                System.setProperty("grid.enabled", "true");
                configureRemoteUrl();
                System.out.println("Selected: Remote Grid execution");
                break;
            default:
                System.out.println("Invalid choice, using default: Local execution");
                System.setProperty("remote.enabled", "false");
                System.setProperty("grid.enabled", "false");
                break;
        }
    }

    private static void configureRemoteUrl() {
        System.out.println("\n   Remote Grid Configuration:");
        System.out.println("   1) localhost:4444 (default)");
        System.out.println("   2) Custom URL");
        System.out.print("   Enter your choice (1-2): ");
        
        String choice = scanner.nextLine().trim();
        String remoteUrl = "http://localhost:4444/wd/hub";
        
        switch (choice) {
            case "1":
                remoteUrl = "http://localhost:4444/wd/hub";
                break;
            case "2":
                System.out.print("   Enter remote URL: ");
                String customUrl = scanner.nextLine().trim();
                if (!customUrl.isEmpty()) {
                    remoteUrl = customUrl;
                }
                break;
            default:
                System.out.println("   Using default: http://localhost:4444/wd/hub");
                break;
        }
        
        System.setProperty("remote.url", remoteUrl);
        System.out.println("   Remote URL: " + remoteUrl);
    }

    private static void configureBrowser() {
        System.out.println("\n3. Select Browser Execution:");
        System.out.println("   1) Single browser (Chrome)");
        System.out.println("   2) Single browser (Firefox)");
        System.out.println("   3) Single browser (Edge)");
        System.out.println("   4) All browsers (Parallel)");
        System.out.print("Enter your choice (1-4): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.setProperty("browser", "chrome");
                System.setProperty("single.browser", "true");
                System.out.println("Selected: Chrome only");
                break;
            case "2":
                System.setProperty("browser", "firefox");
                System.setProperty("single.browser", "true");
                System.out.println("Selected: Firefox only");
                break;
            case "3":
                System.setProperty("browser", "edge");
                System.setProperty("single.browser", "true");
                System.out.println("Selected: Edge only");
                break;
            case "4":
                System.setProperty("single.browser", "false");
                System.out.println("Selected: All browsers (Parallel execution)");
                break;
            default:
                System.out.println("Invalid choice, using default: Chrome only");
                System.setProperty("browser", "chrome");
                System.setProperty("single.browser", "true");
                break;
        }
        
        // Additional options
        configureHeadlessMode();
    }

    private static void configureHeadlessMode() {
        System.out.println("\n4. Headless Mode:");
        System.out.println("   1) Normal mode (with GUI)");
        System.out.println("   2) Headless mode (no GUI)");
        System.out.print("Enter your choice (1-2): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.setProperty("headless", "false");
                System.out.println("Selected: Normal mode (with GUI)");
                break;
            case "2":
                System.setProperty("headless", "true");
                System.out.println("Selected: Headless mode (no GUI)");
                break;
            default:
                System.out.println("Using default: Normal mode");
                System.setProperty("headless", "false");
                break;
        }
    }

    private static void configureCleanReports() {
        System.out.println("\n5. Clean previous reports?");
        System.out.println("   1. Yes - Clean previous test results (mvn clean test)");
        System.out.println("   2. No - Keep previous test results (mvn test)");
        System.out.print("Select clean option (1-2): ");
        
        String cleanChoice = scanner.nextLine().trim();
        switch (cleanChoice) {
            case "1":
                System.setProperty("clean.reports", "true");
                System.out.println("✓ Clean reports enabled");
                break;
            case "2":
                System.setProperty("clean.reports", "false");
                System.out.println("✓ Clean reports disabled");
                break;
            default:
                System.setProperty("clean.reports", "true");
                System.out.println("✓ Default: Clean reports enabled");
                break;
        }
    }
}