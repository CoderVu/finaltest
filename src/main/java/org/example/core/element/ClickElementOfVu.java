package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.util.DriverUtils;
import org.openqa.selenium.By;

import java.time.Duration;

/**
 * Cách dùng:
 * IElement button = new ClickElementOfVu("//button[@id='problematic']");
 * button.click();
 */
@Slf4j
public class ClickElementOfVu extends WebElementWrapper {
    
    public ClickElementOfVu(String locator) {
        super(locator);
    }
    
    public ClickElementOfVu(By byLocator) {
        super(byLocator);
    }
    
    public ClickElementOfVu(String locator, Object... args) {
        super(locator, args);
    }
    
    public ClickElementOfVu(IElement parent, String locator) {
        super(parent, locator);
    }
    
    public ClickElementOfVu(IElement parent, By byLocator) {
        super(parent, byLocator);
    }
    
    public ClickElementOfVu(IElement parent, String locator, Object... args) {
        super(parent, locator, args);
    }

    @Override
    public void click() {
        log.info("Click element of vu");
    }
}

