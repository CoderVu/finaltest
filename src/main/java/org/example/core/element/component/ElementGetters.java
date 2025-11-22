package org.example.core.element.component;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.IElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

@Slf4j
public class ElementGetters {
    
    private final IElement element;
    
    public ElementGetters(IElement element) {
        this.element = element;
    }
    
    public String getText() {
        try {
        //    log.debug("Get text of element {}", element.getLocator().toString());
            return element.getElement().getText();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public String getValue() {
        try {
       //     log.debug("Get value of element {}", element.getLocator().toString());
            return element.getElement().getAttribute("value");
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public String getAttribute(String attributeName) {
        try {
        //    log.debug("Get attribute '{}' of element {}", attributeName, element.getLocator().toString());
            return element.getElement().getAttribute(attributeName);
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public String getClassName() {
        return getAttribute("class");
    }
    
    public String getTagName() {
        try {
            return element.getElement().getTagName();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public WebElement getChildElement(String xpath) {
        try {
            return element.getElement().findElement(By.xpath(xpath));
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }
    
    public List<WebElement> getChildElements(String xpath) {
        try {
            // Đã có parent rồi, chỉ cần dùng getElement() và findElements
            return element.getElement().findElements(By.xpath(xpath));
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", element.getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
}

