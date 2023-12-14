package at.flauschigesalex.defaultLibrary.utils.file;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

@Getter
@SuppressWarnings({"unused", "DataFlowIssue", "DeprecatedIsStillUsed", "unchecked"})
public final class JsonManager {

    public static JsonManager parse(@NotNull String source) {
        return new JsonManager(source);
    }
    public static JsonManager parse(@NotNull StringBuilder source) {
        return parse(source.toString());
    }
    public static JsonManager parse(@NotNull FileManager fileManager) {
        final String read = fileManager.read();
        if (read == null) return null;
        return parse(read);
    }
    public static JsonManager parse(@NotNull ResourceManager resourceManager) {
        final String read = resourceManager.read();
        if (read == null) return null;
        return parse(read);
    }
    public static JsonManager parse(@NotNull File file) {
        return parse(FileManager.getFile(file));
    }
    public static JsonManager parse(@NotNull InputStream fileInputStream) {
        StringBuilder builder = new StringBuilder(); int read;
        while (true) {
            try {
                if ((read = fileInputStream.read()) == -1) break;
                builder.append((char) read);
            } catch (IOException ignore) {
                            }
        }
        if (builder.isEmpty())
            return null;
        return parse(builder);
    }
    public static JsonManager parse(@NotNull URL resource) {
        try {
            return parse(resource.openStream());
        } catch (IOException ignore) {
                    }
        return null;
    }

    private String source;
    private FileManager fileManager;

    JsonManager(String source) {
        this.source = source;
    }
    JsonManager file(FileManager fileManager) {
        this.fileManager = fileManager;
        return this;
    }

    /**
     * @deprecated check {@link #asJsonObject(String)}
     * @return {@link JsonManager} as a {@link JSONObject}
     */
    public JSONObject asJsonObject() {
        try {
            return (JSONObject) asObject();
        } catch (Exception ignore) {
        }
        return null;
    }
    /**
     * @deprecated check {@link #asObject(String)}
     * @return {@link JsonManager} as an {@link Object}
     */
    public Object asObject() {
        try {
            return new JSONParser().parse(getSource());
        } catch (ParseException ignore) {
        }
        return null;
    }
    /**
     * @deprecated check {@link #asObject(String)}
     * @return {@link JsonManager} as an {@link Object}
     */
    public JSONArray asJsonArray() {
        try {
            return (JSONArray) asObject();
        } catch (Exception ignore) {
        }
        return null;
    }
    public String asJsonString() {
        JSONObject jsonObject = asJsonObject();
        if (asJsonObject() == null) return null;
        return asJsonObject().toJSONString();
    }
    private Object get(JSONObject jsonObject, String sourcePath) {
        if (!jsonObject.containsKey(sourcePath))
            return null;
        return jsonObject.get(sourcePath);
    }

    public Object asObject(String sourcePath) {
        JSONObject jsonObject = asJsonObject();
        if (jsonObject == null)
            return null;

        if (!sourcePath.contains(".")) {
            return get(jsonObject, sourcePath);
        }
        JSONObject current = jsonObject;
        String[] splitSourcePath = sourcePath.split("\\.");
        for (String splitSource : splitSourcePath) {
            Object object = get(current, splitSource);
            if (object == null)
                return null;

            if (sourcePath.endsWith(splitSource))
                return current.get(splitSource);
            try {
                current = (JSONObject) object;
            } catch (Exception ignore) {
                break;
            }
        }
        return null;
    }
    public JSONObject asJsonObject(String sourcePath) {
        try {
            return (JSONObject) asObject(sourcePath);
        } catch (Exception ignore) {
        }
        return null;
    }
    public JSONArray asJsonArray(String sourcePath) {
        try {
            return (JSONArray) asObject(sourcePath);
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Object> asList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Object>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<String> asStringList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<String>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Short> asShortList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Short>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Integer> asIntegerList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Integer>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Long> asLongList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Long>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Float> asFloatList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Float>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Double> asDoubleList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Double>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public ArrayList<Boolean> asBooleanList(String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Boolean>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public String asString(String sourcePath) {
        Object object = asObject(sourcePath);
        if (object == null)
            return null;
        return object.toString();
    }
    public Short asShort(String sourcePath) {
        try {
            return Short.parseShort(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Integer asInteger(String sourcePath) {
        try {
            return Integer.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Long asLong(String sourcePath) {
        try {
            return Long.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Float asFloat(String sourcePath) {
        try {
            return Float.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Double asDouble(String sourcePath) {
        try {
            return Double.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Boolean asBoolean(String sourcePath) {
        try {
            return Boolean.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean write(String sourcePath, Object object) {
        JSONObject jsonObject = asJsonObject();
        if (jsonObject == null)
            return false;

        if (sourcePath.contains(".")) {
            return jsonObject.put(sourcePath, object) != null;
        }
        JSONObject current = jsonObject;
        String currentPath;
        String[] splitSourcePath = sourcePath.split("\\.");
        for (String splitSource : splitSourcePath) {
            currentPath = splitSource;
            if (currentPath.equals(splitSourcePath[splitSourcePath.length-1])) {
                boolean value = current.put(currentPath, object) != null;
                this.source = jsonObject.toJSONString();
                return value;
            }
            if (!(current.get(currentPath) instanceof JSONObject tempObject))
                return false;
            current = tempObject;
        }
        return false;
    }

    /**
     * Requires this {@link JsonManager} to be created with a {@link FileManager}.
     * @return If the file was successfully updated
     */
    public boolean updateFile() {
        if (this.fileManager == null || !this.fileManager.isWritable())
            return false;
        return this.fileManager.write(this.source);
    }

    public boolean contains(String sourcePath) {
        return asObject(sourcePath) != null;
    }
    public boolean instanceOf(String sourcePath, Class<? super Object> aClass) {
        return aClass.isInstance(asObject(sourcePath));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"@ {"
                +"\ncontent: "+getSource()
                +"\n}";
    }
}
