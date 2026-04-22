package org.example.pages;

import org.example.core.assertion.AwaitAssert;
import org.example.core.element.SelElement;
import org.example.utils.CookieUtils;
import org.openqa.selenium.By;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.example.core.element.SelElement.$;
import static org.example.utils.DriverUtils.getWindowHandles;
import static org.example.utils.DriverUtils.navigateTo;
import static org.example.utils.DriverUtils.refresh;
import static org.example.utils.DriverUtils.switchTo;

public class HomePage extends BasePage {
    private static final Duration GOOGLE_POPUP_TIMEOUT = Duration.ofSeconds(20);
    private static final Path TIKTOK_COOKIE_FILE = Paths.get("target", "auth", "tiktok.cookies");

    private final SelElement loginTab = $(By.xpath("//button[.//div[normalize-space()='Log in'] or .//span[normalize-space()='Log in']]"));
    private final SelElement loginWithGoogleButton = $(By.xpath("//div[contains(text(),'Continue with Google')]"));

    public void navigateToHomePage() {
        step("Navigate to Home Page", () -> {
            navigateTo("https://www.tiktok.com");
        });
    }

    public boolean isHomePageLoaded() {
        return AwaitAssert.expect(loginTab).isVisible();
    }

    public void loginWithGoogleAccount(String accountIdentifier) {
         step("Login with Google account: " + accountIdentifier, () -> {
            loginTab.click();
            AwaitAssert.expect(loginWithGoogleButton).withTimeout(20).toBeVisible();
            String parentWindow = getWindowHandles().get(0);
            List<String> windowsBefore = getWindowHandles();

            loginWithGoogleButton.click();
            String popupWindow = waitForGooglePopup(windowsBefore);
            switchTo(popupWindow);

            selectGoogleAccount(accountIdentifier);
            switchTo(parentWindow);
        });
    }

    private String waitForGooglePopup(List<String> windowsBefore) {
        AwaitAssert.assertTrue(
                () -> getWindowHandles().size() > windowsBefore.size(),
                "Google login popup should open",
                GOOGLE_POPUP_TIMEOUT
        );

        return getWindowHandles().stream()
                .filter(handle -> !windowsBefore.contains(handle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to detect Google popup window"));
    }

    private void selectGoogleAccount(String accountIdentifier) {
        SelElement accountOption = $(By.xpath(
                "//div[@role='link' and (.//*[contains(normalize-space(),\"" + accountIdentifier + "\")]"
                        + " or contains(normalize-space(),\"" + accountIdentifier.toLowerCase() + "\")"
                        + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                        + "\"" + accountIdentifier.toLowerCase() + "\"))]"));

        SelElement useAnotherAccount = $(By.xpath("//div[@role='link' and .//*[contains(normalize-space(),'Use another account')]]"));
        SelElement emailInput = $(By.id("identifierId"));

        if (AwaitAssert.expect(accountOption).withTimeout(5).isVisible()) {
            accountOption.click();
            return;
        }

        // Some Google flows open email input directly, no account tile and no "Use another account".
        // This flow is usually blocked by Google for automated browsers.
        if (AwaitAssert.expect(emailInput).withTimeout(5).isVisible()) {
            throwGoogleAutomationBlocked(accountIdentifier);
        }

        if (AwaitAssert.expect(useAnotherAccount).withTimeout(5).isVisible()) {
            useAnotherAccount.click();
            throwGoogleAutomationBlocked(accountIdentifier);
        }

        throw new IllegalStateException("No Google account tile, email input, or 'Use another account' was found.");
    }

    private void throwGoogleAutomationBlocked(String accountIdentifier) {
        throw new IllegalStateException(
                "Google blocked automated credential sign-in for account '" + accountIdentifier + "'. "
                        + "Use an already signed-in account tile in the chooser, or complete login manually."
        );
    }
}