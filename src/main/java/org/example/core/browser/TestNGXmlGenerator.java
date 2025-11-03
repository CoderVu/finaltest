package org.example.core.browser;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class để tự động generate testng.xml dựa trên browsers list từ properties
 * 
 * Usage: Chạy class này trước khi run tests để generate testng.xml động
 */
@Slf4j
public class TestNGXmlGenerator {

    private static final String TESTNG_XML_PATH = "src/test/resources/testng.xml";
    private static final String TEST_CLASS = "testCase.TC01AgodaHotelSearch";

    /**
     * Generate testng.xml dựa trên browsers list từ properties (uses default env file)
     */
    public static void generate() {
        generate(null);
    }

    /**
     * Generate testng.xml từ browsers list (internal use - called from Constants)
     * @param browsers List of browsers to generate tests for
     */
    public static void generateFromBrowsers(List<String> browsers) {
        if (browsers == null || browsers.isEmpty()) {
            log.warn("No browsers provided. Using default: chrome");
            browsers = List.of("chrome");
        }
        generateXmlFile(browsers);
    }

    /**
     * Generate testng.xml dựa trên browsers list từ properties
     * @param envFile Optional environment file. If null, uses Config.getEnvFile()
     */
    public static void generate(String envFile) {
        try {
            List<String> browsers;
            
            // Nếu Constants đã được initialized, sử dụng browsers hiện có
            // (tránh vòng lặp khi được gọi từ Constants.loadEnvironment)
            try {
                browsers = Constants.getBrowsers();
                if (browsers != null && !browsers.isEmpty()) {
                    log.debug("Using browsers from already initialized Constants");
                } else {
                    throw new IllegalStateException("Constants not initialized or no browsers");
                }
            } catch (Exception e) {
                // Nếu Constants chưa initialized, load environment từ filesystem
                log.debug("Constants not initialized, loading environment from filesystem: {}", envFile);
                if (envFile == null || envFile.trim().isEmpty()) {
                    envFile = Config.getEnvFile();
                }
                
                // Load từ filesystem (cho Maven plugin execution) và generate trực tiếp
                loadEnvironmentFromFilesystem(envFile);
                return; // Đã generate xong trong loadEnvironmentFromFilesystem
            }
            
            if (browsers == null || browsers.isEmpty()) {
                log.warn("No browsers configured in properties. Using default: chrome");
                browsers = List.of("chrome");
            }

            generateXmlFile(browsers);
            
        } catch (Exception e) {
            log.error("Failed to generate testng.xml: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate testng.xml", e);
        }
    }

    /**
     * Load environment từ filesystem (cho Maven plugin execution)
     * Load trực tiếp từ src/test/resources/ mà không cần qua Config/Constants
     */
    private static void loadEnvironmentFromFilesystem(String envFile) {
        try {
            // Load trực tiếp từ filesystem
            String filePath = "src/test/resources/" + envFile;
            File file = new File(filePath);
            
            if (!file.exists()) {
                throw new RuntimeException("Properties file not found: " + file.getAbsolutePath());
            }
            
            log.info("Loading properties from filesystem: {}", file.getAbsolutePath());
            java.util.Properties props = new java.util.Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                props.load(fis);
            }
            
            // Parse browsers trực tiếp
            String browsersStr = props.getProperty("browsers", props.getProperty("browser", "chrome"));
            List<String> browsers = Arrays.stream(browsersStr.split("\\s*,\\s*"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("Loaded browsers from filesystem: {}", browsers);
            
            // Generate XML trực tiếp mà không cần Constants initialization
            generateXmlFile(browsers);
            
        } catch (Exception e) {
            log.error("Failed to load environment from filesystem: {}", e.getMessage(), e);
            throw new RuntimeException("Could not load environment file: " + envFile, e);
        }
    }

    /**
     * Generate và ghi XML file
     */
    private static void generateXmlFile(List<String> browsers) {
        try {
            // normalize: trim, lower-case, remove empties and duplicates
            List<String> cleaned = browsers.stream()
                    .filter(b -> b != null)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .distinct()
                    .collect(Collectors.toList());

            if (cleaned.isEmpty()) {
                // default to chrome nếu không có browser nào hợp lệ
                log.warn("No valid browsers after cleaning. Defaulting to chrome.");
                cleaned = List.of("chrome");
            }

            String xmlContent = generateXmlContent(cleaned);
            
            File xmlFile = new File(TESTNG_XML_PATH);
            xmlFile.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(xmlFile)) {
                writer.write(xmlContent);
            }
            
            log.info("✅ Generated testng.xml with {} browsers: {}", cleaned.size(), cleaned);
        } catch (Exception e) {
            log.error("Failed to write testng.xml file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to write testng.xml", e);
        }
    }

    private static String generateXmlContent(List<String> browsers) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n");

        boolean isParallel = browsers.size() > 1;
        String suiteStart = "<suite name=\"Selenium Test Suite\"";
        if (isParallel) {
            suiteStart += " parallel=\"tests\" thread-count=\"" + browsers.size() + "\"";
        }
        suiteStart += ">";
        xml.append(suiteStart).append("\n");

        xml.append("    <listeners>\n");
        // AllureTestNg MUST come first to create test cases before ReportHook processes them
        xml.append("        <listener class-name=\"io.qameta.allure.testng.AllureTestNg\"/>\n");
        xml.append("        <listener class-name=\"org.example.core.report.hook.ReportHook\"/>\n");
        xml.append("    </listeners>\n");

        for (String browser : browsers) {
            String testName = capitalize(browser) + " Test";
            xml.append("    <test name=\"").append(testName).append("\"");
            // leave test-level parallel to methods (independent of suite parallelization)
            xml.append(" parallel=\"methods\" thread-count=\"1\">\n");
            xml.append("        <parameter name=\"browser\" value=\"").append(browser.toLowerCase()).append("\"/>\n");
            xml.append("        <classes>\n");
            xml.append("            <class name=\"").append(TEST_CLASS).append("\"/>\n");
            xml.append("        </classes>\n");
            xml.append("    </test>\n");
        }

        xml.append("</suite>\n");
        return xml.toString();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Main method để chạy standalone
     * Usage: java TestNGXmlGenerator [env-file]
     * Example: java TestNGXmlGenerator dev-env.properties
     */
    public static void main(String[] args) {
        String envFile = args.length > 0 ? args[0] : null;
        generate(envFile);
    }
}

