package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.control.element.Element;
import org.example.core.control.util.DriverUtils;
import org.example.core.report.ReportManager;
import org.example.core.report.IReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

@Slf4j
public class BasePage {

    protected IReporter reporter = ReportManager.getReporter();

}
