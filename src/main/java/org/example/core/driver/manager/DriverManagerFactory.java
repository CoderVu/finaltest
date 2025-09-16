package org.example.core.driver.manager;

import com.logigear.driver.DriverProperty;
import com.logigear.exception.DriverCreationException;
import com.logigear.helper.ReflectionUtils;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class DriverManagerFactory {
    protected static final ThreadLocal<DriverManager> DRIVERS = new ThreadLocal<>();

    private static ThreadLocal<String> driverKey = new ThreadLocal<>();

    private static int timeOut = 30;
    private static int shortTimeOut = 15;

    public static boolean isDriverExist() {
        return DRIVERS.get() != null;
    }

    protected static WebDriver getDriver() {
        return DRIVERS.get().getWebDriver(driverKey.get());
    }

    protected static void setWebDriver(DriverManager driverManager, String key, DriverProperty property) {
        if (DRIVERS.get() == null)
            DRIVERS.set(driverManager);
        else {
            DRIVERS.get().addWebDriver(driverManager.webDrivers);
        }

        // Add driverProperty
        DRIVERS.get().addDriverProperty(key, property);
        DRIVERS.get().setMainWindow(DRIVERS.get().getWebDriver(key).getWindowHandle());
    }

    protected static void createWebDriver(String key, DriverProperty property) throws DriverCreationException {
        Object obj = ReflectionUtils.initWebDriver(key, property);
        if (obj == null) {
            throw new DriverCreationException(String.format("Cannot create the %s driver", property.getDriverType()));
        }
        DriverManager driverManager = (DriverManager) obj;
        setWebDriver(driverManager, key, property);
    }

    protected static String getCurrentDriverType() {
        return DRIVERS.get().getDriverProperty(driverKey.get()).getDriverType().toString();
    }

    protected static void switchToDriver(String key) {
        driverKey.set(key);
    }

    /**
     * @return the timeOut
     */
    public static int getTimeOut() {
        return timeOut;
    }

    /**
     * @param timeOut the timeOut to set
     */
    public static void setTimeOut(int timeOut) {
        DriverManagerFactory.timeOut = timeOut;
    }

    /**
     * @return the shortTimeOut
     */
    public static int getShortTimeOut() {
        return shortTimeOut;
    }

    /**
     * @param timeOut the timeOut to set
     */
    public static void setShortTimeOut(int timeOut) {
        DriverManagerFactory.shortTimeOut = timeOut;
    }

    public static void setPageLoadTimeout(int timeOutInSecond) {
        getDriver().manage().timeouts().pageLoadTimeout(timeOutInSecond, TimeUnit.SECONDS);
    }

}
