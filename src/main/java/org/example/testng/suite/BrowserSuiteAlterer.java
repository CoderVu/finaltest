package org.example.testng.suite;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alters TestNG suites to run tests across multiple browsers.
 */
public class BrowserSuiteAlterer implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        List<BrowserType> browsers = Config.getBrowserTypes();

        // For each suite, duplicate its tests per browser within the SAME suite.
        // This allows TestNG parallel="tests" to run browsers and test cases in parallel.
        for (XmlSuite suite : suites) {
            List<XmlTest> originalTests = new ArrayList<>(suite.getTests());
            suite.getTests().clear();

            for (BrowserType browser : browsers) {
                for (XmlTest t : originalTests) {
                    XmlTest nt = new XmlTest(suite);
                    nt.setName(t.getName() + "-" + browser.toString());

                    // copy classes
                    List<XmlClass> classes = new ArrayList<>();
                    if (t.getXmlClasses() != null) {
                        classes.addAll(t.getXmlClasses());
                    }
                    nt.setXmlClasses(classes);

                    // merge test-level params and add browser param
                    Map<String, String> testParams = new HashMap<>();
                    if (t.getLocalParameters() != null) {
                        testParams.putAll(t.getLocalParameters());
                    }
                    testParams.put("browser", browser.toString());
                    nt.setParameters(testParams);
                }
            }
        }
    }
}

