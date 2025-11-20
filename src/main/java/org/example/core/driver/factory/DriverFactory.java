package org.example.core.driver.factory;

import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.Chrome;
import org.example.core.driver.Edge;
import org.example.core.driver.Firefox;
import org.example.core.driver.manager.AbstractDriverManager;
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

    public static void createDriver(BrowserType type) {
        log.warn("Creating Driver for type {}", type);
        AbstractDriverManager manager = THREAD_LOCAL.get();
        if (manager == null || manager.getBrowserType() != type) {
            manager = DRIVER_MAP.getOrDefault(type, DRIVER_MAP.get(BrowserType.CHROME)).get();
            manager.initDriver();
            THREAD_LOCAL.set(manager);
        }
        log.debug("Driver initialized for browser: {}", type);
    }

    public static AbstractDriverManager getCurrentDriverManager() {
        return THREAD_LOCAL.get();
    }

    public static void quitDriver() {
        AbstractDriverManager manager = THREAD_LOCAL.get();
        if (manager != null) {
            manager.quitDriver();
            THREAD_LOCAL.remove();
        }
    }
}
