package org.example.launcher;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.ExecutionConfig;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestLauncher {
    
    public static void main(String[] args) {
        log.info("=== Selenium Test Suite Cross-Platform Launcher ===");
        
        try {
            // Check if Maven is available
            if (!isMavenAvailable()) {
                log.error("Error: Maven is not installed or not in PATH");
                log.error("Please install Maven and ensure 'mvn' command is available");
                System.exit(1);
            }
            
            // Check Docker and Selenium Grid
            checkAndStartSeleniumGrid();
            
            log.info("Starting interactive test configuration...");
            log.info("This will prompt you to select:");
            log.info("1. Environment file (dev-env.yaml or prod-env.yaml)");
            log.info("2. Execution mode (local or remote grid)");
            log.info("3. Browser selection (single or all browsers)");
            log.info("4. Display mode (normal or headless)");
            
            // Configure interactively
            ExecutionConfig.configureInteractive();
            
            // Build Maven command
            List<String> command = buildMavenCommand();
            
            log.info("Executing: {}", String.join(" ", command));
            
            // Execute Maven
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); // This allows real-time output
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("✓ Test execution completed successfully!");
                
                // Generate Allure report
                log.info("Generating Allure report...");
                generateAllureReport();
                
            } else {
                log.error("✗ Test execution failed with exit code: {}", exitCode);
            }
            
        } catch (Exception e) {
            log.error("Error during test execution: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    private static void generateAllureReport() {
        try {
            // Generate Allure report
            ProcessBuilder pb = new ProcessBuilder(getMavenCommand(), "allure:report");
            pb.inheritIO();
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("✓ Allure report generated successfully!");
                log.info("✓ Report location: target/allure-report/index.html");
                log.info("✓ To view report, run: mvn allure:serve");
            } else {
                log.warn("⚠ Failed to generate Allure report (exit code: {})", exitCode);
            }
        } catch (Exception e) {
            log.warn("⚠ Failed to generate Allure report: {}", e.getMessage());
        }
    }
    
    private static void checkAndStartSeleniumGrid() {
        try {
            // Check if Docker is available
            if (!isDockerAvailable()) {
                log.warn("Warning: Docker is not available. Grid functionality may be limited.");
                return;
            }
            
            // Check if Docker is running
            if (!isDockerRunning()) {
                log.warn("Warning: Docker is not running. Grid functionality may be limited.");
                return;
            }
            
            // Check Selenium Grid status
            log.info("Checking Selenium Grid status...");
            if (!isSeleniumGridRunning()) {
                log.info("Selenium Grid is not running. Starting Grid services...");
                startSeleniumGrid();
                
                // Wait for Grid to be ready
                log.info("Waiting for Selenium Grid to be ready...");
                Thread.sleep(30000);
                
                if (isSeleniumGridRunning()) {
                    log.info("✓ Selenium Grid started successfully!");
                    logGridUrls();
                } else {
                    log.warn("Warning: Failed to start Selenium Grid");
                }
            } else {
                log.info("✓ Selenium Grid is already running");
                logGridUrls();
            }
            
        } catch (Exception e) {
            log.warn("Warning: Could not check/start Selenium Grid: {}", e.getMessage());
        }
    }
    
    private static void logGridUrls() {
        log.info("✓ Selenium Grid Hub: http://localhost:4444/wd/hub (for test connections)");
        log.info("✓ Grid Console UI: http://localhost:4444/ui#/ (for monitoring)");
        log.info("✓ Grid Status API: http://localhost:4444/status (for health checks)");
        log.info("✓ VNC Viewers (watch browser sessions):");
        log.info("  - Chrome Node: http://localhost:7900/");
        log.info("  - Firefox Node: http://localhost:7901/");
        log.info("  - Edge Node: http://localhost:7902/");
    }
    
    private static boolean isDockerAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean isDockerRunning() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "info");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean isSeleniumGridRunning() {
        try {
            URL url = new URL("http://localhost:4444/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void startSeleniumGrid() throws Exception {
        // Stop any existing containers first
        try {
            ProcessBuilder pb = new ProcessBuilder("docker-compose", "down");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            // Ignore errors when stopping
        }
        
        // Start new containers
        ProcessBuilder pb = new ProcessBuilder("docker-compose", "up", "-d");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Failed to start Docker Compose services");
        }
    }
    
    private static boolean isMavenAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(getMavenCommand(), "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static String getMavenCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "mvn.cmd"; // Windows
        } else {
            return "mvn"; // Unix/Mac
        }
    }
    
    private static List<String> buildMavenCommand() {
        List<String> command = new ArrayList<>();
        command.add(getMavenCommand());
        
        // Check if user wants clean build
        String cleanReports = System.getProperty("clean.reports", "true");
        if ("true".equals(cleanReports)) {
            command.add("clean");
            log.info("Clean reports enabled - removing previous test results");
        } else {
            log.info("Clean reports disabled - keeping previous test results");
        }
        
        command.add("test");
        
        // Xử lý single browser mode đặc biệt
        String singleBrowser = System.getProperty("single.browser");
        if ("true".equals(singleBrowser)) {
            String browser = System.getProperty("browser");
            if (browser != null) {
                command.add("-Dsingle.browser=true");
                command.add("-Dbrowser=" + browser);
                log.info("Single browser mode enabled for: " + browser);
            }
        }
        
        // Add other system properties
        addPropertyIfSet(command, "env.file");
        addPropertyIfSet(command, "headless");
        addPropertyIfSet(command, "remote.enabled");
        addPropertyIfSet(command, "remote.url");
        addPropertyIfSet(command, "grid.enabled");
        addPropertyIfSet(command, "w3c.enabled");
        addPropertyIfSet(command, "clean.reports");
        
        return command;
    }
    
    private static void addPropertyIfSet(List<String> command, String propertyName) {
        String value = System.getProperty(propertyName);
        if (value != null) {
            command.add("-D" + propertyName + "=" + value);
        }
    }
}