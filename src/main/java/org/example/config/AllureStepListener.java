package org.example.config;

import io.qameta.allure.Allure;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

import static org.example.utils.DateUtils.getCurrentDate;

public class AllureStepListener implements StepLifecycleListener {

    private static volatile long lastScreenshotTime = 0;
    private static final long SCREENSHOT_COOLDOWN = 15000;

    @Override
    public void afterStepStop(StepResult result) {
        if (result.getStatus() == Status.FAILED) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastScreenshotTime > SCREENSHOT_COOLDOWN) {
                lastScreenshotTime = currentTime;
                attachScreenshotToCurrentStep();
            }
        }
    }
    
    private void attachScreenshotToCurrentStep() {
        try {
            WebDriver driver = com.codeborne.selenide.WebDriverRunner.getWebDriver();
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            if (bytes != null && bytes.length > 0) {
                String timestamp = getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS");
                Allure.getLifecycle().addAttachment("Step Failure Attachment [" + timestamp + "]",
                                                   "image/png", "png", new ByteArrayInputStream(bytes));
            }
        } catch (Exception ignored) {
            // Ignore screenshot errors
        }
    }
}
