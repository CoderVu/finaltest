package org.example.core.reporting;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.ReportType;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class ReportPluginRegistry {

    private static final Map<ReportType, Plugin> PLUGINS = new ConcurrentHashMap<>();

    static {
        ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);
        loader.forEach(ReportPluginRegistry::register);
        if (PLUGINS.isEmpty()) {
            log.warn("No report plugins discovered. Ensure META-INF/services is configured.");
        }
    }

    private ReportPluginRegistry() {}

    public static void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "report Plugin");
        PLUGINS.put(plugin.getType(), plugin);
        log.info("Registered report Plugin for type {}", plugin.getType());
    }

    public static Plugin getPlugin(ReportType type) {
        Plugin plugin = PLUGINS.get(type);
        if (plugin == null) {
            throw new IllegalStateException("No report Plugin registered for type: " + type);
        }
        return plugin;
    }
}


