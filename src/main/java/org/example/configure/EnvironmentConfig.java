package org.example.configure;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class EnvironmentConfig {
    private static Map<String, Object> config;

    public static void load(String filePath) {
        Yaml yaml = new Yaml();
        try (InputStream in = EnvironmentConfig.class.getClassLoader().getResourceAsStream(filePath)) {
            if (in == null) {
                throw new RuntimeException("YAML file not found in classpath: " + filePath);
            }
            config = yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Could not load yaml file: " + filePath, e);
        }
    }

    public static String get(String key) {
        if (config == null) {
            throw new IllegalStateException("YAML not loaded");
        }

        String[] parts = key.split("\\.");
        Object value = config;

        for (String part : parts) {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                value = map.get(part);
            } else {
                return null;
            }
        }

        return value != null ? value.toString() : null;
    }
}
