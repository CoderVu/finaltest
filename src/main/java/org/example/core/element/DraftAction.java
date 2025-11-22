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
public class DraftAction extends ElementWrapperWrapper {
    
    public DraftAction(By byLocator) {
        super(byLocator);
    }
    
    public DraftAction(By byLocator, Object... args) {
        super(byLocator, args);
    }
    
    /**
     * Override click() - Viết lại logic click theo nhu cầu
     */
    @Override
    public void click() {
        // TODO: Write your custom click logic here
        // Ví dụ:
        // scrollToView();
        // clickByJs();
        super.click();
    }
    
    // Can override other actions as needed

}

