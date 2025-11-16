package org.example.core.dataProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.core.helper.JsonHelper;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple DataProvider for end-users - Easiest to use!
 * <p>
 * How to use:
 * 1. Just add @Test(dataProvider = "auto") and the method will automatically get data
 * 2. Method parameter should be JsonObject - that's it!
 * <p>
 * Example:
 *
 * @Test(dataProvider = "auto")
 * public void myTest(JsonObject data) {
 * String city = dataProvider.DataProvider.getString(data, "destination");
 * int rooms = dataProvider.DataProvider.getInt(data, "occupancy.rooms");
 * }
 */
@Slf4j
public class DataProvider {

    @org.testng.annotations.DataProvider(name = "auto")
    public static Iterator<Object[]> auto(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String dataFile = resolveDataFile(method, className);

        log.info("Auto-loading data for class: {} from file: {}", className, dataFile);
        Iterator<Object[]> raw = loadData(className, dataFile);

        Class<?>[] paramTypes = method.getParameterTypes();
        boolean hasDataPath = false;
        Annotation[][] paramAnns = method.getParameterAnnotations();
        for (Annotation[] anns : paramAnns) {
            for (Annotation a : anns) {
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
                for (Annotation a : paramAnns[i]) {
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
                    params[i] = null;
                }
            }

            bound.add(params);
        }

        return bound.iterator();
    }

    private static final String DATA_DIR = "src/test/resources/data/";

    private static String resolveDataFile(Method method, String className) {
        DataFile dataFileAnnotation = method.getAnnotation(DataFile.class);
        if (dataFileAnnotation == null || dataFileAnnotation.value().isEmpty()) {
            String errorMsg = String.format(
                    "Test method '%s' in class '%s' must specify @DataFile annotation with a valid file path.",
                    method.getName(), className
            );
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        String filePath = dataFileAnnotation.value();
        
        // If path is just a filename (no directory), prepend default data directory
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            filePath = DATA_DIR + filePath;
            log.debug("Prepending data directory to filename: {}", filePath);
        }
        
        if (!fileExists(filePath)) {
            String errorMsg = String.format(
                    "Data file specified in @DataFile annotation not found: %s. Please check the file path.",
                    filePath
            );
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        log.info("Using data file from @DataFile annotation: {}", filePath);
        return filePath;
    }

    private static boolean fileExists(String filePath) {
        // Check as file system path
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return true;
        }

        try {
            InputStream in = DataProvider.class.getClassLoader().getResourceAsStream(
                    filePath.startsWith("src/") ? filePath.replace("src/test/resources/", "") : filePath
            );
            if (in != null) {
                in.close();
                return true;
            }
        } catch (Exception e) {
            // Ignore
        }

        return false;
    }

    public static Iterator<Object[]> loadData(String testCaseKey, String dataFile) {
        JsonObject root = JsonHelper.getJsonObject(dataFile);

        if (root == null) {
            log.error("Cannot read test data from: {}", dataFile);
            return new ArrayList<Object[]>().iterator();
        }

        JsonElement node = root.get(testCaseKey);

        if (node == null || node.isJsonNull()) {
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(testCaseKey)) {
                    node = entry.getValue();
                    log.info("Matched test data key case-insensitively: '{}' -> '{}'", testCaseKey, entry.getKey());
                    break;
                }
            }
        }

        if (node == null || node.isJsonNull()) {
            String normalizedKey = testCaseKey.replaceAll("[^A-Za-z0-9]", "");
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String normalizedEntry = entry.getKey().replaceAll("[^A-Za-z0-9]", "");
                if (normalizedEntry.equalsIgnoreCase(normalizedKey)) {
                    node = entry.getValue();
                    log.info("Matched test data key by relaxed compare: '{}' -> '{}'", testCaseKey, entry.getKey());
                    break;
                }
            }
        }

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
                    dataSets.add(new Object[]{item.getAsJsonObject()});
                }
            }
        } else if (element.isJsonObject()) {
            dataSets.add(new Object[]{element.getAsJsonObject()});
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

