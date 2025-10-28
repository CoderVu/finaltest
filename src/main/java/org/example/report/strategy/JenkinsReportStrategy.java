package org.example.report.strategy;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;

@Slf4j
public class JenkinsReportStrategy implements ReportStrategy {
    @Override public void onTestStart(ITestResult result) { }
    @Override public void onTestSuccess(ITestResult result) { }
    @Override public void onTestFailure(ITestResult result) { }
    @Override public void onTestSkipped(ITestResult result) { }
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
    @Override public void onTestFailedWithTimeout(ITestResult result) { }
    @Override public void onStart(ITestContext context) { log.info("Jenkins report: rely on Surefire/JUnit XML at target/surefire-reports/"); }
    @Override public void onFinish(ITestContext context) { }
    @Override public void onConfigurationSuccess(ITestResult itr) { }
    @Override public void onConfigurationFailure(ITestResult itr) { }
    @Override public void onConfigurationSkip(ITestResult itr) { }
}


