package org.example.core.reporting;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.enums.ReportType;
import org.example.utils.NormalizeUtils;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Core reporting manager utility.
 * Provides methods to discover and access reporting plugins.
 * Users are responsible for choosing which plugin type to use.
 */
@Slf4j
public final class ReportManager {

    // Cache of discovered plugins by type
    private static final Map<String, IReportType> PLUGINS = new ConcurrentHashMap<>();
    
    // Cache of created Reporter instances by type
    private static final Map<String, Reporter> REPORTERS = new ConcurrentHashMap<>();
    
    // Cache of created ReportingListener instances by type
    private static final Map<String, ReportingListener> LISTENERS = new ConcurrentHashMap<>();

    private static final String NO_OP_TYPE = org.example.enums.ReportType.NOOP.key();
    private static final ReportingListener NO_OP_LISTENER = new ReportingListener() {};
    private static volatile String defaultType = null;
    
    static {
        // Discover and register plugins on class load
        ServiceLoader<IReportType> loader = ServiceLoader.load(IReportType.class);
        loader.forEach(plugin -> {
            String type = plugin.getType();
            if (type == null || type.trim().isEmpty()) {
                log.warn("Skipping plugin with null or empty type: {}", plugin.getClass().getName());
                return;
            }
            PLUGINS.put(type.toLowerCase(), plugin);
            log.info("Registered report IReportType for type: {}", type);
        });
        defaultType = resolveDefaultType();
    }

    private ReportManager() {}

    public static Reporter getReporter(String type) {
        String normalizedType = NormalizeUtils.normalize(type);
        if (normalizedType.isEmpty()) {
            normalizedType = getDefaultType();
        }
        
        return REPORTERS.computeIfAbsent(normalizedType, t -> {
            IReportType IReportType = getPluginOrNoOp(t);
            Reporter reporter = IReportType.createReporter();
            log.info("Initialized {} reporter for type: {}", reporter.getClass().getSimpleName(), t);
            return reporter;
        });
    }

    public static Reporter getReporter() {
        return getReporter(getDefaultType());
    }

    public static Reporter getReporter(ReportType type) {
        Objects.requireNonNull(type, "IReportType enum cannot be null");
        return getReporter(type.key());
    }

    /**
     * Gets a Reporter instance from a IReportType instance directly.
     * Useful when user already has a IReportType instance.
     * 
     * @param plugin the IReportType instance
     * @return Reporter instance
     */
    public static Reporter getReporter(IReportType plugin) {
        Objects.requireNonNull(plugin, "IReportType cannot be null");
        String type = plugin.getType();
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("IReportType type cannot be null or empty");
        }
        return getReporter(type);
    }

    /**
     * Gets a ReportingListener instance for the specified plugin type.
     * Creates and caches the instance on first call.
     * 
     * @param type the plugin type identifier (case-insensitive, e.g., "extent", "allure")
     * @return ReportingListener instance
     * @throws IllegalStateException if no plugin is registered for the given type
     */
    public static ReportingListener getLifecycleListener(String type) {
        String normalizedType = NormalizeUtils.normalize(type);
        if (normalizedType.isEmpty()) {
            normalizedType = getDefaultType();
        }
        
        return LISTENERS.computeIfAbsent(normalizedType, t -> {
            IReportType plugin = getPluginOrNoOp(t);
            ReportingListener listener = plugin.createLifecycleListener();
            log.info("Initialized {} lifecycle listener for type: {}", listener.getClass().getSimpleName(), t);
            return listener;
        });
    }

    public static ReportingListener getLifecycleListener() {
        return getLifecycleListener(getDefaultType());
    }

    public static ReportingListener getLifecycleListener(org.example.enums.ReportType type) {
        Objects.requireNonNull(type, "IReportType enum cannot be null");
        return getLifecycleListener(type.key());
    }

    public static ReportingListener getLifecycleListener(IReportType plugin) {
        Objects.requireNonNull(plugin, "IReportType cannot be null");
        String type = plugin.getType();
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("IReportType type cannot be null or empty");
        }
        return getLifecycleListener(type);
    }


    public static IReportType getPlugin(String type) {
        String normalizedType = NormalizeUtils.normalize(type);
        if (normalizedType.isEmpty()) {
            normalizedType = getDefaultType();
        }
        
        IReportType plugin = PLUGINS.get(normalizedType);
        if (plugin == null) {
            throw new IllegalStateException("No report IReportType registered for type: " + type +
                    ". Available types: " + PLUGINS.keySet());
        }
        return plugin;
    }

    /**
     * Gets all available plugin types.
     * 
     * @return set of available plugin type identifiers
     */
    public static java.util.Set<String> getAvailablePluginTypes() {
        return java.util.Collections.unmodifiableSet(PLUGINS.keySet());
    }

    /**
     * Resets all cached instances for a specific plugin type.
     * Useful for testing or switching plugins.
     * 
     * @param type the plugin type to reset
     */
    public static void reset(String type) {
        if (type != null) {
            String normalizedType = type.trim().toLowerCase();
            REPORTERS.remove(normalizedType);
            LISTENERS.remove(normalizedType);
        }
    }

    /**
     * Resets all cached instances.
     * Useful for testing or switching plugins.
     */
    public static void reset() {
        REPORTERS.clear();
        LISTENERS.clear();
    }

    public static String getDefaultType() {
        return defaultType;
    }

    public static void setDefaultType(String type) {
        Objects.requireNonNull(type, "IReportType type cannot be null");
        String normalizedType = type.trim().toLowerCase();
        if (normalizedType.isEmpty()) {
            throw new IllegalArgumentException("IReportType type cannot be empty");
        }
        if (!PLUGINS.containsKey(normalizedType) && !NO_OP_TYPE.equals(normalizedType)) {
            throw new IllegalArgumentException("Unknown report type: " + type + ". Available: " + PLUGINS.keySet());
        }
        defaultType = normalizedType;
    }

    public static void setDefaultType(org.example.enums.ReportType type) {
        Objects.requireNonNull(type, "IReportType enum cannot be null");
        setDefaultType(type.key());
    }

    private static String resolveDefaultType() {
        String normalizedConfigured = NormalizeUtils.normalize(Constants.DEFAULT_REPORT);
        boolean supportedBuiltIn = normalizedConfigured.equals(org.example.enums.ReportType.EXTENT.key())
                || normalizedConfigured.equals(org.example.enums.ReportType.ALLURE.key())
                || normalizedConfigured.equals(org.example.enums.ReportType.NOOP.key());
        if (!supportedBuiltIn) {
            normalizedConfigured = org.example.enums.ReportType.EXTENT.key();
        }
        if (!normalizedConfigured.isEmpty() && PLUGINS.containsKey(normalizedConfigured)) {
            log.info("Using configured report type: {}", normalizedConfigured);
            return normalizedConfigured;
        }

        if (!normalizedConfigured.isEmpty()) {
            log.warn("Configured report type '{}' is not registered. Available: {}", normalizedConfigured, PLUGINS.keySet());
        }

        if (!PLUGINS.isEmpty()) {
            String fallback = PLUGINS.keySet().iterator().next();
            log.info("Fallback to discovered report type: {}", fallback);
            return fallback;
        }

        log.warn("No report plugins discovered. Fallback to NoOp reporting.");
        return NO_OP_TYPE;
    }

    private static IReportType getPluginOrNoOp(String type) {
        if (NO_OP_TYPE.equals(type)) {
            return new NoOpIReportType();
        }
        return getPlugin(type);
    }

    private static final class NoOpIReportType implements IReportType {

        @Override
        public String getType() {
            return NO_OP_TYPE;
        }

        @Override
        public Reporter createReporter() {
            return new NoOpReporter();
        }

        @Override
        public ReportingListener createLifecycleListener() {
            return NO_OP_LISTENER;
        }
    }

    private static final class NoOpReporter implements Reporter {

        @Override
        public void logStep(String message) {
            log.debug("[NOOP-REPORTER] STEP {}", message);
        }

        @Override
        public void info(String message) {
            log.debug("[NOOP-REPORTER] INFO {}", message);
        }

        @Override
        public void logFail(String message, Throwable error) {
            log.error("[NOOP-REPORTER] FAIL {}", message, error);
        }

        @Override
        public void attachScreenshot(String name) {
            log.debug("[NOOP-REPORTER] Screenshot skipped: {}", name);
        }

        @Override
        public void childStep(String name, Runnable runnable) {
            runnable.run();
        }

        @Override
        public <T> T childStep(String name, Supplier<T> supplier) {
            return supplier.get();
        }
    }

}


