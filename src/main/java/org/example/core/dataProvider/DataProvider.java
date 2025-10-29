package org.example.core.dataProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.core.helper.JsonHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple DataProvider for end-users - Easiest to use!
 * 
 * How to use:
 * 1. Just add @Test(dataProvider = "auto") and the method will automatically get data
 * 2. Method parameter should be JsonObject - that's it!
 * 
 * Example:
 * @Test(dataProvider = "auto")
 * public void myTest(JsonObject data) {
 *     String city = dataProvider.DataProvider.getString(data, "destination");
 *     int rooms = dataProvider.DataProvider.getInt(data, "occupancy.rooms");
 * }
 */
@Slf4j
public class DataProvider {

    private static final String DEFAULT_DATA_FILE = "src/test/resources/data/tc01.json";

    /**
     * Auto DataProvider - reads data automatically based on class name
     * Usage: @Test(dataProvider = "auto", dataProviderClass = dataProvider.DataProvider.class)
     * 
     * You can specify data file using @DataFile annotation:
     * @Test(dataProvider = "auto", dataProviderClass = DataProvider.class)
     * @DataFile("src/test/resources/data/tc01.json")
     * public void myTest(JsonObject data) { ... }
     */
    @org.testng.annotations.DataProvider(name = "auto")
    public static Iterator<Object[]> auto(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String dataFile = DEFAULT_DATA_FILE;
        
        // Check if @DataFile annotation is present on the test method
        DataFile dataFileAnnotation = method.getAnnotation(DataFile.class);
        if (dataFileAnnotation != null && !dataFileAnnotation.value().isEmpty()) {
            dataFile = dataFileAnnotation.value();
            log.info("Using data file from @DataFile annotation: {}", dataFile);
        } else {
            log.info("Using default data file: {}", dataFile);
        }
        
        log.info("Auto-loading data for class: {} from file: {}", className, dataFile);
        Iterator<Object[]> raw = loadData(className, dataFile);

        // If method expects exactly one JsonObject and has no @DataPath, keep legacy behavior
        Class<?>[] paramTypes = method.getParameterTypes();
        boolean hasDataPath = false;
        java.lang.annotation.Annotation[][] paramAnns = method.getParameterAnnotations();
        for (java.lang.annotation.Annotation[] anns : paramAnns) {
            for (java.lang.annotation.Annotation a : anns) {
                if (a instanceof DataPath) {
                    hasDataPath = true;
                    break;
                }
            }
            if (hasDataPath) break;
        }

        if (!hasDataPath && paramTypes.length == 1 && JsonObject.class.isAssignableFrom(paramTypes[0])) {
            return raw;
        }

        // Otherwise, bind parameters by @DataPath
        List<Object[]> bound = new ArrayList<>();
        while (raw.hasNext()) {
            Object[] next = raw.next();
            JsonObject dataset = (JsonObject) next[0];
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> p = paramTypes[i];
                DataPath ann = null;
                for (java.lang.annotation.Annotation a : paramAnns[i]) {
                    if (a instanceof DataPath) {
                        ann = (DataPath) a;
                        break;
                    }
                }

                if (ann == null) {
                    // Fallbacks
                    if (JsonObject.class.isAssignableFrom(p)) {
                        params[i] = dataset;
                    } else if (String.class.equals(p)) {
                        params[i] = null; // no binding without @DataPath
                    } else if (p.equals(int.class) || p.equals(Integer.class)) {
                        params[i] = 0;
                    } else if (p.equals(boolean.class) || p.equals(Boolean.class)) {
                        params[i] = false;
                    } else {
                        params[i] = null;
                    }
                    continue;
                }

                String path = ann.value();
                if (String.class.equals(p)) {
                    params[i] = getString(dataset, path, null);
                } else if (p.equals(int.class) || p.equals(Integer.class)) {
                    params[i] = getInt(dataset, path, 0);
                } else if (p.equals(boolean.class) || p.equals(Boolean.class)) {
                    params[i] = getBoolean(dataset, path, false);
                } else if (JsonObject.class.isAssignableFrom(p)) {
                    params[i] = getObject(dataset, path);
                } else {
                    // Unsupported type -> leave null
                    params[i] = null;
                }
            }

            bound.add(params);
        }

        return bound.iterator();
    }

    /**
     * Load data from JSON file with test case key
     * @param testCaseKey Key in JSON (usually class name)
     * @param dataFile Path to data file
     */
    public static Iterator<Object[]> loadData(String testCaseKey, String dataFile) {
        JsonObject root = JsonHelper.getJsonObject(dataFile);
        
        if (root == null) {
            log.error("Cannot read test data from: {}", dataFile);
            return new ArrayList<Object[]>().iterator();
        }

        // 1) Exact key
        JsonElement node = root.get(testCaseKey);

        // 2) Case-insensitive match if exact not found
        if (node == null || node.isJsonNull()) {
            for (java.util.Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(testCaseKey)) {
                    node = entry.getValue();
                    log.info("Matched test data key case-insensitively: '{}' -> '{}'", testCaseKey, entry.getKey());
                    break;
                }
            }
        }

        // 3) Relaxed match: strip non-alphanumeric characters and compare
        if (node == null || node.isJsonNull()) {
            String normalizedKey = testCaseKey.replaceAll("[^A-Za-z0-9]", "");
            for (java.util.Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String normalizedEntry = entry.getKey().replaceAll("[^A-Za-z0-9]", "");
                if (normalizedEntry.equalsIgnoreCase(normalizedKey)) {
                    node = entry.getValue();
                    log.info("Matched test data key by relaxed compare: '{}' -> '{}'", testCaseKey, entry.getKey());
                    break;
                }
            }
        }

        // 4) Single top-level key fallback
        if (node == null || node.isJsonNull()) {
            java.util.Set<String> keys = root.keySet();
            if (keys.size() == 1) {
                String onlyKey = keys.iterator().next();
                node = root.get(onlyKey);
                log.info("Using the only top-level key '{}' in {} as dataset", onlyKey, dataFile);
            }
        }

        if (node == null || node.isJsonNull()) {
            log.warn("Test case key '{}' not found in {}. Using root object as dataset.", testCaseKey, dataFile);
            return loadFromElement(root).iterator();
        }

        return loadFromElement(node).iterator();
    }

    private static List<Object[]> loadFromElement(JsonElement element) {
        List<Object[]> dataSets = new ArrayList<>();

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                if (item != null && item.isJsonObject()) {
                    dataSets.add(new Object[] { item.getAsJsonObject() });
                }
            }
        } else if (element.isJsonObject()) {
            dataSets.add(new Object[] { element.getAsJsonObject() });
        }

        return dataSets;
    }

    public static String getString(JsonObject data, String path) {
        return getString(data, path, "");
    }

    /**
     * Get string value with default
     */
    public static String getString(JsonObject data, String path, String defaultValue) {
        if (data == null || path == null) return defaultValue;
        
        String[] keys = path.split("\\.");
        JsonElement element = data;
        
        for (String key : keys) {
            if (element == null || !element.isJsonObject()) {
                return defaultValue;
            }
            element = element.getAsJsonObject().get(key);
            if (element == null || element.isJsonNull()) {
                return defaultValue;
            }
        }
        
        return element.getAsString();
    }

    public static int getInt(JsonObject data, String path) {
        return getInt(data, path, 0);
    }

    /**
     * Get int value with default
     */
    public static int getInt(JsonObject data, String path, int defaultValue) {
        if (data == null || path == null) return defaultValue;
        
        String[] keys = path.split("\\.");
        JsonElement element = data;
        
        for (String key : keys) {
            if (element == null || !element.isJsonObject()) {
                return defaultValue;
            }
            element = element.getAsJsonObject().get(key);
            if (element == null || element.isJsonNull()) {
                return defaultValue;
            }
        }
        
        try {
            return element.getAsInt();
        } catch (Exception e) {
            log.warn("Cannot parse int from path {}: {}", path, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Get boolean value
     */
    public static boolean getBoolean(JsonObject data, String path) {
        return getBoolean(data, path, false);
    }

    public static boolean getBoolean(JsonObject data, String path, boolean defaultValue) {
        if (data == null || path == null) return defaultValue;
        
        String[] keys = path.split("\\.");
        JsonElement element = data;
        
        for (String key : keys) {
            if (element == null || !element.isJsonObject()) {
                return defaultValue;
            }
            element = element.getAsJsonObject().get(key);
            if (element == null || element.isJsonNull()) {
                return defaultValue;
            }
        }
        
        try {
            return element.getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get nested JsonObject
     * Example: JsonObject occupancy = getObject(data, "occupancy");
     */
    public static JsonObject getObject(JsonObject data, String path) {
        if (data == null || path == null) return null;
        
        String[] keys = path.split("\\.");
        JsonElement element = data;
        
        for (String key : keys) {
            if (element == null || !element.isJsonObject()) {
                return null;
            }
            element = element.getAsJsonObject().get(key);
            if (element == null || element.isJsonNull()) {
                return null;
            }
        }
        
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}

