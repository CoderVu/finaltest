package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

/**
 * CustomAction - Override actions of IBaseElement as needed.
 * 
 * Way to use:
 * IBaseElement button = new CustomAction("//button[@id='test']");
 * button.click(); // Will use overridden click() logic
 */
@Slf4j
public class CustomAction extends BaseElement implements IBaseElement {
    
    public CustomAction(By byLocator) {
        super(byLocator);
    }

    public CustomAction(String locator, Object... args) {
        super(locator, args);
    }
    
    /**
     * Override click()s
     */
    @Override
    public void click() {
        // TODO: Write your custom click logic here
    }
    
    // Can override other actions as needed

}

