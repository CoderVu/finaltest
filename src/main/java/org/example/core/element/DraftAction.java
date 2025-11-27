package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

/**
 * DraftAction - Override actions of IElementWrapper as needed.
 * 
 * Way to use:
 * IElementWrapper button = new DraftAction("//button[@id='test']");
 * button.click(); // Will use overridden click() logic
 */
@Slf4j
public class DraftAction extends ElementWrapper implements IElementWrapper {
    
    public DraftAction(By byLocator) {
        super(byLocator);
    }

    public DraftAction(String locator, Object... args) {
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

