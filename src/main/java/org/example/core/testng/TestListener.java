package org.example.core.testng;

import lombok.extern.slf4j.Slf4j;
import org.example.core.testng.listeners.TestNgReportingListener;
import org.example.core.testng.retryTCs.RetryAnnotationTransformer;
import org.example.core.testng.suite.BrowserSuiteAlterer;
import org.testng.IAnnotationTransformer;
import org.testng.IAlterSuiteListener;
import org.testng.annotations.ITestAnnotation;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Composite TestNG listener that wires:
 * - reporting (via {@link TestNgReportingListener})
 * - retry injection (via {@link RetryAnnotationTransformer})
 * - multi-browser suite alteration (via {@link BrowserSuiteAlterer})
 */
@Slf4j
public class TestListener extends TestNgReportingListener implements IAnnotationTransformer, IAlterSuiteListener {

    private final RetryAnnotationTransformer retryTransformer = new RetryAnnotationTransformer();
    private final BrowserSuiteAlterer suiteAlterer = new BrowserSuiteAlterer();

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        retryTransformer.transform(annotation, testClass, testConstructor, testMethod);
    }

    @Override
    public void alter(List<XmlSuite> suites) {
        suiteAlterer.alter(suites);
    }
}
