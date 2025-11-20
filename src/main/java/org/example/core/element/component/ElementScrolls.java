package org.example.core.element.component;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.IElement;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class ElementScrolls {
    
    private final IElement element;
    
    public ElementScrolls(IElement element) {
        this.element = element;
    }
    
    public void scrollElementToCenterScreen() {
        try {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void scrollToView() {
        try {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView(true);", element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void scrollToView(int offsetX, int offsetY) {
        try {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) getWebDriver();
            String script = String.format(
                    "arguments[0].scrollIntoView(true); window.scrollBy(%d, %d);", 
                    offsetX, offsetY);
            js.executeScript(script, element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
}

