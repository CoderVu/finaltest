package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.ReportType;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public abstract class AbstractReporter implements IReporter {

    protected final ReportType reportType;

    protected AbstractReporter(ReportType reportType) {
        this.reportType = reportType;
    }

    @Override
    public ReportType getReportType() {
        return reportType;
    }

    protected byte[] getScreenshotBytes() {

        WebDriver driver = getWebDriver();
        if (driver != null && driver instanceof TakesScreenshot) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return null;
    }
}

