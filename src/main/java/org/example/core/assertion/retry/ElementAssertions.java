package org.example.core.assertion.retry;

import org.openqa.selenium.By;

/**
 * Base class providing assertion-with-retry helpers for elements.
 * Subclasses must provide locator and basic query methods.
 */
public abstract class ElementAssertions {

    protected abstract By getLocator();

    protected abstract boolean isVisible();

    protected abstract boolean isEnabled();

    protected abstract String getText();

    protected abstract String getAttribute(String attribute);

    protected abstract String getValue();

    public void shouldBeVisible() {
        String message = "Element should be visible: " + getLocator();
        FunctionAssertions.assertTrue(this::isVisible, message);
    }

    public void shouldNotBeVisible() {
        String message = "Element should NOT be visible: " + getLocator();
        FunctionAssertions.assertFalse(this::isVisible, message);
    }

    public void shouldBeEnabled() {
        String message = "Element should be enabled: " + getLocator();
        FunctionAssertions.assertTrue(this::isEnabled, message);
    }

    public void shouldBeDisabled() {
        String message = "Element should be disabled: " + getLocator();
        FunctionAssertions.assertFalse(this::isEnabled, message);
    }

    public void shouldHaveText(String expected) {
        String message = "Element should have exact text '" + expected + "': " + getLocator();
        FunctionAssertions.assertEquals(this::getText, expected, message);
    }

    public void shouldContainText(String substring) {
        String message = "Element text should contain '" + substring + "': " + getLocator();
        FunctionAssertions.assertTrue(() -> {
            String actual = getText();
            return actual != null && actual.contains(substring);
        }, message);
    }

    public void shouldHaveAttribute(String attribute, String expected) {
        String message = "Element attribute '" + attribute + "' should equal '" + expected + "': " + getLocator();
        FunctionAssertions.assertEquals(() -> getAttribute(attribute), expected, message);
    }

    public void shouldHaveValue(String expected) {
        String message = "Element value should equal '" + expected + "': " + getLocator();
        FunctionAssertions.assertEquals(this::getValue, expected, message);
    }
}


