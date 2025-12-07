package org.example.core.assertion.retry;

import org.openqa.selenium.By;

public abstract class ElementAssertions<T extends ElementAssertions<T>> {

    protected abstract By getLocator();

    protected abstract boolean isVisible();

    protected abstract boolean isEnabled();

    protected abstract String getText();

    protected abstract String getAttribute(String attribute);

    protected abstract String getValue();

    protected abstract T self();

    public T shouldBeVisible() {
        String message = "Element should be visible: " + getLocator();
        FunctionAssertions.assertTrue(this::isVisible, message);
        return self();
    }

    public T shouldNotBeVisible() {
        String message = "Element should NOT be visible: " + getLocator();
        FunctionAssertions.assertFalse(this::isVisible, message);
        return self();
    }

    public T shouldBeEnabled() {
        String message = "Element should be enabled: " + getLocator();
        FunctionAssertions.assertTrue(this::isEnabled, message);
        return self();
    }

    public T shouldBeDisabled() {
        String message = "Element should be disabled: " + getLocator();
        FunctionAssertions.assertFalse(this::isEnabled, message);
        return self();
    }

    public T shouldHaveText(String expected) {
        String message = "Element should have exact text '" + expected + "': " + getLocator();
        FunctionAssertions.assertEquals(this::getText, expected, message);
        return self();
    }

    public T shouldContainText(String substring) {
        String message = "Element text should contain '" + substring + "': " + getLocator();
        FunctionAssertions.assertTrue(() -> {
            String actual = getText();
            return actual != null && actual.contains(substring);
        }, message);
        return self();
    }

    public T shouldHaveAttribute(String attribute, String expected) {
        String message = "Element attribute '" + attribute + "' should equal '" + expected + "': " + getLocator();
        FunctionAssertions.assertEquals(() -> getAttribute(attribute), expected, message);
        return self();
    }

    public T shouldHaveValue(String expected) {
        String message = "Element value should equal '" + expected + "': " + getLocator();
        FunctionAssertions.assertEquals(this::getValue, expected, message);
        return self();
    }
}


