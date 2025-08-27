package org.example.config;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.io.ByteArrayInputStream;
 
import java.util.UUID;
import static org.example.utils.DateUtils.getCurrentDate;
import static org.example.config.AllureConfig.takeScreenshot;



public class SoftAssertConfig extends SoftAssert {

    private static final ThreadLocal<SoftAssertConfig> SOFT = ThreadLocal.withInitial(SoftAssertConfig::new);
    private int failureCount = 0;

    public static SoftAssertConfig get() {
        return SOFT.get();
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void resetFailureCount() {
        failureCount = 0;
    }

    @Override
    public void onBeforeAssert(IAssert<?> assertCommand) {
    }

    @Override
    public void onAfterAssert(IAssert<?> assertCommand) {
    }

    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {

    }

    /**
     * Purpose:
     * - Create a separate step in the Allure Report to log failure details without breaking the flow of other steps.
     * - Attach a screenshot to the FAILED step for easier debugging and analysis.
     * - Find parent step and update it to FAILED if sub-steps fail.
     */
    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        String message = normalizeMessage(assertCommand.getMessage());
        if (message.isEmpty()) message = ex.getMessage();

        String expected = stringify(assertCommand.getExpected());
        String actual = stringify(assertCommand.getActual());
        String stepName = "FAIL [" +  getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "]: " + message + " | expected=" + expected + " actual=" + actual;

        String stepUuid = UUID.randomUUID().toString();

        StepResult stepResult = new StepResult()
                .setName(stepName)
                .setStatus(Status.FAILED);

        Allure.getLifecycle().startStep(stepUuid, stepResult);

        try {
            WebDriver driver = com.codeborne.selenide.WebDriverRunner.getWebDriver();
            if (driver != null) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    Allure.getLifecycle().addAttachment("Attachment", "image/png", "png", new ByteArrayInputStream(bytes));
                }
            }
        } catch (Throwable ignored) {

        }
        Allure.getLifecycle().updateStep(stepUuid, step -> {
            step.setStatus(Status.FAILED);
        });

        Allure.getLifecycle().stopStep(stepUuid);

        super.onAssertFailure(assertCommand, ex);
    }

    private String normalizeMessage(String message) {
        return message == null ? "" : message;
    }

    private String stringify(Object obj) {
        try {
            return String.valueOf(obj);
        } catch (Exception e) {
            return "";
        }
    }

    public static void recordFailure(String message) {
        takeScreenshot();
        Allure.step("FAIL [" + getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "]: " + message, Status.FAILED);
        get().fail(message);
    }

    public static void reset() {
        SOFT.remove();
        SOFT.set(new SoftAssertConfig());
    }

}



