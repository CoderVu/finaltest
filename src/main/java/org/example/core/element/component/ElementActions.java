package org.example.core.element.component;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.IElement;
import org.example.core.element.util.DriverUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class ElementActions {
    
    private final IElement element;
    
    public ElementActions(IElement element) {
        this.element = element;
    }
    
    public void click() {
        click(1);
    }
    
    public void click(int times) {
        if (times <= 0) return;
        
        int attemptsLeft = times;
        Exception lastException = null;
        
        while (attemptsLeft > 0) {
            try {
            //    log.debug("Click on {}", element.getLocator());
                
                if (!element.isVisible()) {
                    element.waitForDisplay(DriverUtils.getTimeOut());
                }
                
                element.scrollElementToCenterScreen();
                element.waitForElementClickable(DriverUtils.getTimeOut());
                
                new Actions(getWebDriver())
                        .moveToElement(element.getElement())
                        .pause(Duration.ofMillis(100))
                        .click()
                        .build()
                        .perform();
                return;
            } catch (Exception e) {
                lastException = e;
                String msg = e.getMessage() == null ? "" : e.getMessage().split("\n")[0];
                
                boolean intercepted = msg.contains("Other element would receive the click")
                        || msg.contains("Element is not clickable at point")
                        || msg.contains("element click intercepted");
                
                attemptsLeft--;
                
                if (!intercepted) {
                    // log.info("Non-intercepted click error on '{}': {}. Using JS click.", element.getLocator(), msg);
                    clickByJs();
                    return;
                }
                
                if (attemptsLeft == 0) {
                    // log.info("Final click attempt failed on '{}': {}. Trying JS click.", element.getLocator(), msg);
                    try {
                        clickByJs();
                        return;
                    } catch (Exception jsEx) {
                        throw new RuntimeException("Click failed after retries on: " + element.getLocator(), lastException);
                    }
                }
                
                DriverUtils.delay(0.5);
                // log.info("Retry click on '{}': {} ({} attempts left)", element.getLocator(), msg, attemptsLeft);
            }
        }
        
        throw new RuntimeException("Click failed after retries on: " + element.getLocator(), lastException);
    }
    
    public void click(int x, int y) {
        try {
        //    log.debug("Click on {}", element.getLocator().toString());
            new Actions(getWebDriver()).moveToElement(element.getElement(), x, y).click().build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void clickByJs() {
        try {
         //   log.debug("Click by js on {}", element.getLocator().toString());
            ((org.openqa.selenium.JavascriptExecutor) getWebDriver())
                    .executeScript("arguments[0].click();", element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void doubleClick() {
        try {
            log.debug("Double click on {}", element.getLocator().toString());
            new Actions(getWebDriver()).doubleClick(element.getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void setText(String text) {
        try {
       //     log.debug("Set text '{}' for {}", text, element.getLocator().toString());
            element.getElement().sendKeys(text);
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void clear() {
        try {
          //  log.debug("Clean text for {}", element.getLocator().toString());
            element.getElement().clear();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void enter(CharSequence... value) {
        try {
          //  log.debug("Enter '{}' for {}", value, element.getLocator().toString());
            element.getElement().sendKeys(value);
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void sendKeys(Keys key) {
        try {
          //  log.debug("Sending key {} to element {}", key, element.getLocator().toString());
            element.getElement().sendKeys(key);
        } catch (Exception e) {
            log.error("Has error sending key to control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void submit() {
        try {
         //   log.debug("Submit form for {}", element.getLocator().toString());
            element.getElement().submit();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void focus() {
        try {
            DriverUtils.execJavaScript("arguments[0].focus();", element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void dragAndDrop(int xOffset, int yOffset) {
        try {
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDropBy(element.getElement(), xOffset, yOffset).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void dragAndDrop(IElement target) {
        try {
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDrop(element.getElement(), target.getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void moveTo() {
        try {
            new Actions(getWebDriver()).moveToElement(element.getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void moveTo(int x, int y) {
        try {
            new Actions(getWebDriver()).moveToElement(element.getElement(), x, y).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void moveToCenter() {
        try {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element.getElement());
            new Actions(getWebDriver()).moveToElement(element.getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void mouseHoverJScript() {
        try {
            String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            ((org.openqa.selenium.JavascriptExecutor) getWebDriver()).executeScript(mouseOverScript, element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void setValue(String value) {
        try {
            String js = String.format("arguments[0].value='%s';", value);
            log.debug("Set value '{}' for {}", value, element.getLocator().toString());
            ((org.openqa.selenium.JavascriptExecutor) getWebDriver()).executeScript(js, element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void setAttributeJS(String attributeName, String value) {
        try {
            log.debug("Set attribute for {}", element.getLocator().toString());
            ((org.openqa.selenium.JavascriptExecutor) getWebDriver())
                    .executeScript(String.format("arguments[0].setAttribute('%s','%s');", attributeName, value),
                            element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public void checkCheckBoxByJs() {
        try {
            log.debug("Check checkbox by JS for {}", element.getLocator().toString());
            ((org.openqa.selenium.JavascriptExecutor) getWebDriver())
                    .executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));", 
                            element.getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
}

