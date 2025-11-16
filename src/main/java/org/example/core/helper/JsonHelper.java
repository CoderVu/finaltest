package org.example.core.helper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openqa.selenium.remote.DesiredCapabilities;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonHelper {

	public static Map<String, String> convertJsonToMap(String json) {
		try {
			log.debug("JsonHelper: convertJsonToMap");
			Type mapType = new TypeToken<Map<String, String>>() {
			}.getType();
			Gson gson = new Gson();
			return gson.fromJson(json, mapType);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public static Map<String, Object> convertJsonToMapObject(String json) {
		try {
			log.debug("JsonHelper: convertJsonToMap");
			Type mapType = new TypeToken<Map<String, Object>>() {
			}.getType();
			Gson gson = new Gson();
			return gson.fromJson(json, mapType);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public static <T> List<T> getListData(String jsonPath, Type type) {
		try {
			log.debug("JsonHelper: getListData");
			JsonReader reader = getJsonReader(jsonPath);
			List<T> lst = new ArrayList<T>();
			Gson gson = new Gson();
			lst = gson.fromJson(reader, type);
			return lst;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public static <T> List<T> getListData(String jsonPath, Class<?> clazz) {
		try {
			log.debug("JsonHelper: getListData");
			JsonReader reader = getJsonReader(jsonPath);
			List<T> lst = new ArrayList<T>();
			Gson gson = new Gson();
			lst = gson.fromJson(reader, clazz);
			return lst;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public static <T> T getData(String jsonPath, Type type) {
		try {
			log.debug("JsonHelper: getData");
			Gson gson = new Gson();
			JsonReader reader = getJsonReader(jsonPath);
			return gson.fromJson(reader, type);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}

	}

	public static <T> T getData(String jsonPath, Class<?> clazz) {
		try {
			log.debug("JsonHelper: getData");
			Gson gson = new Gson();
			JsonReader reader = getJsonReader(jsonPath);
			return gson.fromJson(reader, clazz);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public static JsonObject getJsonObject(String jsonPath) {
		try {
			log.debug("JsonHelper: getJsonObject");
			JsonObject obj = new JsonObject();
			Gson gson = new Gson();
			JsonReader reader = getJsonReader(jsonPath);
			obj = gson.fromJson(reader, JsonObject.class);
			return obj;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public static JsonElement getJsonObjectFromString(String jsonString) {
		try {
			log.debug("JsonHelper: getJsonObjectFromString");
			JsonElement obj = null;
			Gson gson = new Gson();
			obj = gson.fromJson(jsonString, JsonElement.class);
			return obj;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public static DesiredCapabilities convertJsonToCapabilities(String json) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		Map<String, Object> caps = JsonHelper.convertJsonToMapObject(json);
		if (caps != null) {
			Set<String> keys = caps.keySet();
			for (String key : keys) {
				capabilities.setCapability(key, caps.get(key));
			}
		}
		return capabilities;
	}

	public static List<String> convertJsonToArguments(String json) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            log.error("Error converting JSON to arguments: {}", e.getMessage());
            return new ArrayList<>();
        }
	}

	private static JsonReader getJsonReader(String jsonPath) {
		try {
			JsonReader reader;
			reader = new JsonReader(new FileReader(jsonPath));
			return reader;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static JsonElement getJsonByPath(JsonElement jsonObject, String jsonPath) {
		JsonElement jObj = jsonObject;
		String[] split = jsonPath.split("\\.");
		JsonElement el = null;

		for (String e : split) {
			char lastChar = e.charAt(e.length() - 1);

			if (']' == lastChar) {
				while (']' == lastChar) {
					String index = e.substring(e.lastIndexOf('[') + 1, e.length() - 1);
					Integer iindex = Integer.valueOf(index);
					e = e.substring(0, e.lastIndexOf('['));
					if ("".equals(e)) {
						if (el == null) {
							el = jObj.getAsJsonArray().get(iindex);
							lastChar = (char) 0;
						} else {
							el = el.getAsJsonArray().get(iindex);
							lastChar = (char) 0;
						}
					} else {
						lastChar = e.charAt(e.length() - 1); // new last char

						// if next is object
						if (lastChar != ']') {
							if (el == null) {
								el = jObj.getAsJsonObject().get(e).getAsJsonArray().get(iindex);
							} else {
								if (el.isJsonObject())
									el = el.getAsJsonObject().get(e).getAsJsonArray().get(iindex);
								else if (el.isJsonArray())
									el = el.getAsJsonArray().get(iindex);
							}
						} else { // next is array
							if (el == null) {
								if (jObj.isJsonObject()) {
									String locale = e.substring(0, e.indexOf('['));
									el = jObj.getAsJsonObject().get(locale).getAsJsonArray().get(iindex);
								} else if (jObj.isJsonArray())
									el = jObj.getAsJsonArray().get(iindex);
							} else {
								if (e.indexOf('[') > -1) {
									String locale = e.substring(0, e.indexOf('['));
									el = el.getAsJsonObject().get(locale).getAsJsonArray().get(iindex);
								} else
									el = el.getAsJsonArray().get(iindex);
							}
						}
					}
				}
			} else {
				// plain obj
				if (el == null)
					el = jObj.getAsJsonObject().get(e);
				else
					el = el.getAsJsonObject().get(e);
			}
		}
		return el;
	}

	public static String getStringValue(JsonObject jsonObject, String path) {
        try {
            String[] keys = path.split("\\.");
            JsonElement element = jsonObject;
            
            for (String key : keys) {
                if (element.isJsonObject()) {
                    element = element.getAsJsonObject().get(key);
                }
            }
            
            return element != null ? element.getAsString() : null;
        } catch (Exception e) {
            log.error("Error getting string value for path {}: {}", path, e.getMessage());
            return null;
        }
    }

    public static int getIntValue(JsonObject jsonObject, String path) {
        try {
            String[] keys = path.split("\\.");
            JsonElement element = jsonObject;
            
            for (String key : keys) {
                if (element.isJsonObject()) {
                    element = element.getAsJsonObject().get(key);
                }
            }
            
            return element != null ? element.getAsInt() : 0;
        } catch (Exception e) {
            log.error("Error getting int value for path {}: {}", path, e.getMessage());
            return 0;
        }
    }

    public static JsonObject getNestedObject(JsonObject jsonObject, String path) {
        try {
            String[] keys = path.split("\\.");
            JsonElement element = jsonObject;
            
            for (String key : keys) {
                if (element.isJsonObject()) {
                    element = element.getAsJsonObject().get(key);
                }
            }
            
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (Exception e) {
            log.error("Error getting nested object for path {}: {}", path, e.getMessage());
            return null;
        }
    }
}
