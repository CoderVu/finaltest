package org.example.core.driver;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.BrowserType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class DriverFactory {

    private static final Map<BrowserType, Supplier<AbstractDriverManager>> DRIVER_MAP = new EnumMap<>(BrowserType.class);
    private static final ThreadLocal<AbstractDriverManager> THREAD_LOCAL = new ThreadLocal<>();

    static {
        DRIVER_MAP.put(BrowserType.CHROME, Chrome::new);
        DRIVER_MAP.put(BrowserType.FIREFOX, Firefox::new);
        DRIVER_MAP.put(BrowserType.EDGE, Edge::new);
    }

    public static AbstractDriverManager getDriverManager(BrowserType type) {
        AbstractDriverManager manager = THREAD_LOCAL.get();
        if (manager == null || manager.getBrowserType() != type) {
            manager = DRIVER_MAP.getOrDefault(type, Chrome::new).get();
            manager.initDriver();
            THREAD_LOCAL.set(manager);
        }
        log.info("Initialized WebDriver for browser: {}", type);
        log.info("WebDriver browser type: {}", manager.browserType);
        return manager;
    }

    public static void quitDriver() {
        AbstractDriverManager manager = THREAD_LOCAL.get();
        if (manager != null) {
            manager.quitDriver();
            THREAD_LOCAL.remove();
        }
    }
}
