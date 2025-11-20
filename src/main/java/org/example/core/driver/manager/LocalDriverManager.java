package org.example.core.driver.manager;

import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;

/**
 * Base class that encapsulates local driver creation.
 */
public abstract class LocalDriverManager extends AbstractDriverManager {

    protected LocalDriverManager(BrowserType browserType) {
        super(browserType);
    }

    @Override
    public void initLocalDriver() {
        driver = createLocalDriver();
    }

    protected abstract WebDriver createLocalDriver();
}

