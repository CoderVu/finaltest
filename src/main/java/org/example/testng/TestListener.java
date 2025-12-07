package org.example.testng;

import lombok.extern.slf4j.Slf4j;
import org.example.testng.listeners.TestNGReporter;
import org.example.testng.suite.BrowserSuiteAlterer;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * Composite TestNG listener that wires:
 * - reporting (via {@link TestNGReporter})
 * - multi-browser suite alteration (via {@link BrowserSuiteAlterer})
 */
@Slf4j
public class TestListener extends TestNGReporter implements IAlterSuiteListener {

    private final BrowserSuiteAlterer suiteAlterer = new BrowserSuiteAlterer();

    @Override
    public void alter(List<XmlSuite> suites) {
        suiteAlterer.alter(suites);
    }
}

