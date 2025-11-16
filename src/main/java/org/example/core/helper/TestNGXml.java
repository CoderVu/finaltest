package org.example.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.utils.EnvUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TestNGXml {

    private static final String TESTNG_XML_PATH = "src/test/resources/testng.xml";
    private static final String TEST_CLASS = "testCase.TC01AgodaHotelSearch";

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

            try {
                browsers = Constants.getBrowsers();
                if (browsers != null && !browsers.isEmpty()) {
                    log.debug("Using browsers from already initialized Constants");
                } else {
                    throw new IllegalStateException("Constants not initialized or no browsers");
                }
            } catch (Exception e) {
                log.debug("Constants not initialized, loading environment using EnvUtils: {}", envFile);
                if (envFile == null || envFile.trim().isEmpty()) {
                    envFile = Config.getEnvFile();
                }

                // Use EnvUtils to load environment instead of duplicating logic
                EnvUtils.loadEnv(envFile);
                browsers = EnvUtils.getBrowsers();
                
                if (browsers == null || browsers.isEmpty()) {
                    log.warn("No browsers found after loading environment. Using default: chrome");
                    browsers = List.of("chrome");
                }
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
     * Generate và ghi XML file
     */
    private static void generateXmlFile(List<String> browsers) {
        try {
            List<String> cleaned = browsers.stream()
                    .filter(b -> b != null)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .distinct()
                    .collect(Collectors.toList());

            if (cleaned.isEmpty()) {
                log.warn("No valid browsers after cleaning. Defaulting to chrome.");
                cleaned = List.of("chrome");
            }

            String xmlContent = generateXmlContent(cleaned);
            File xmlFile = new File(TESTNG_XML_PATH);
            xmlFile.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(xmlFile)) {
                writer.write(xmlContent);
            }
            
            log.info("Generated testng.xml with {} browsers: {}", cleaned.size(), cleaned);
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
            log.info("Generating testng.xml with PARALLEL execution: {} browsers, thread-count={}", browsers.size(), browsers.size());
        } else {
            log.info("Generating testng.xml with SEQUENTIAL execution: {} browser", browsers.size());
        }
        suiteStart += ">";
        xml.append(suiteStart).append("\n");

        xml.append("    <listeners>\n");
        xml.append("        <listener class-name=\"org.example.core.report.listener.ReportListener\"/>\n");
        // Try to add AllureTestNg listener - it will auto-create test cases in Allure lifecycle
        // If it fails to instantiate, AllureStrategyI will handle test case creation manually
        List<String> reportTypes = Constants.getReportTypes();
        String reportTypeStr = (reportTypes != null && !reportTypes.isEmpty()) 
            ? reportTypes.get(0) 
            : Constants.DEFAULT_REPORT;
        if ("allure".equalsIgnoreCase(reportTypeStr.trim())) {
            xml.append("        <listener class-name=\"io.qameta.allure.testng.AllureTestNg\"/>\n");
        }
        xml.append("    </listeners>\n");

        for (String browser : browsers) {
            String testName = capitalize(browser) + " Test";
            xml.append("    <test name=\"").append(testName).append("\"");
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


    public static void main(String[] args) {
        String envFile = args.length > 0 ? args[0] : null;
        generate(envFile);
    }
}

