package org.example.core.reporting;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.plugin.ReportPlugin;
import org.example.enums.ReportType;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class ReportPluginRegistry {

    private static final Map<ReportType, ReportPlugin> PLUGINS = new ConcurrentHashMap<>();

    static {
        ServiceLoader<ReportPlugin> loader = ServiceLoader.load(ReportPlugin.class);
        loader.forEach(ReportPluginRegistry::register);
        if (PLUGINS.isEmpty()) {
            log.warn("No report plugins discovered. Ensure META-INF/services is configured.");
        }
    }

    private ReportPluginRegistry() {}

    public static void register(ReportPlugin plugin) {
        Objects.requireNonNull(plugin, "report plugin");
        PLUGINS.put(plugin.getType(), plugin);
        log.info("Registered report plugin for type {}", plugin.getType());
    }

    public static ReportPlugin getPlugin(ReportType type) {
        ReportPlugin plugin = PLUGINS.get(type);
        if (plugin == null) {
            throw new IllegalStateException("No report plugin registered for type: " + type);
        }
        return plugin;
    }
}


