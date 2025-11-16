package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ReportManager;
import org.example.core.report.IReporter;

@Slf4j
public class BasePage {

    protected IReporter reporter = ReportManager.getReporter();

}
