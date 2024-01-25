package at.flauschigesalex.defaultLibrary.fileUtils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
@SuppressWarnings({"unused", "DataFlowIssue", "DeprecatedIsStillUsed", "unchecked", "UnusedReturnValue"})
public final class JsonManager {

    private final String source;
    private FileManager fileManager;

    JsonManager(final @NotNull String source) {
        this.source = source;
    }

    public static @Nullable JsonManager createNew() {
        return parse("{}");
    }

    public static @Nullable JsonManager writeNew(final @NotNull String sourcePath, final Object object) {
        final JsonManager manager = createNew();
        manager.write(sourcePath, object);
        return manager;
    }

    public static @Nullable JsonManager parse(final @NotNull String source) {
        return new JsonManager(source);
    }

    public static @Nullable JsonManager parse(final @NotNull StringBuilder source) {
        return parse(source.toString());
    }

    public static @Nullable JsonManager parse(final @NotNull FileManager fileManager) {
        final String read = fileManager.read();
        if (read == null) return null;
        return parse(read);
    }

    public static @Nullable JsonManager parse(final @NotNull ResourceManager resourceManager) {
        final String read = resourceManager.read();
        if (read == null) return null;
        return parse(read);
    }

    public static @Nullable JsonManager parse(final @NotNull File file) {
        return parse(FileManager.getFile(file));
    }

    public static @Nullable JsonManager parse(final @NotNull InputStream fileInputStream) {
        StringBuilder builder = new StringBuilder();
        int read;
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

    public static @Nullable JsonManager parse(final @NotNull URL resource) {
        try {
            return parse(resource.openStream());
        } catch (IOException ignore) {
        }
        return null;
    }

    /**
     * @return {@link JsonManager} as a {@link JSONObject}
     * @deprecated check {@link #asJsonObject(String)}
     */
    public JSONObject asJsonObject() {
        try {
            return (JSONObject) asObject();
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * @return {@link JsonManager} as an {@link Object}
     * @deprecated check {@link #asObject(String)}
     */
    public Object asObject() {
        try {
            return new JSONParser().parse(getSource());
        } catch (ParseException ignore) {
        }
        return null;
    }

    /**
     * @return {@link JsonManager} as an {@link Object}
     * @deprecated check {@link #asObject(String)}
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

    public JSONObject asJsonObject(final @NotNull String sourcePath) {
        try {
            return (JSONObject) asObject(sourcePath);
        } catch (Exception ignore) {
        }
        return null;
    }

    public JsonManager asJsonManager(final @NotNull String sourcePath) {
        try {
            return JsonManager.parse(asJsonObject(sourcePath).toJSONString());
        } catch (Exception ignore) {
        }
        return null;
    }

    public JSONArray asJsonArray(final @NotNull String sourcePath) {
        try {
            return (JSONArray) asObject(sourcePath);
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Object> asList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Object>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<String> asStringList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<String>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Short> asShortList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Short>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Integer> asIntegerList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Integer>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Long> asLongList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Long>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Float> asFloatList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Float>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Double> asDoubleList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Double>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public ArrayList<Boolean> asBooleanList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Boolean>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public String asString(final @NotNull String sourcePath) {
        Object object = asObject(sourcePath);
        if (object == null)
            return null;
        return object.toString();
    }

    public Short asShort(final @NotNull String sourcePath) {
        try {
            return Short.parseShort(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Integer asInteger(final @NotNull String sourcePath) {
        try {
            return Integer.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Long asLong(final @NotNull String sourcePath) {
        try {
            return Long.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Float asFloat(final @NotNull String sourcePath) {
        try {
            return Float.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Double asDouble(final @NotNull String sourcePath) {
        try {
            return Double.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Boolean asBoolean(final @NotNull String sourcePath) {
        try {
            return Boolean.valueOf(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean write(final @NotNull String sourcePath, final @Nullable Object object) {
        return this.write(sourcePath, asJsonObject(), object);
    }

    private boolean write(final @NotNull String sourcePath, final @Nullable JSONObject jsonObject, final @Nullable Object object) {
        final JSONObject manager = jsonObject == null ? asJsonObject() : jsonObject;
        System.out.println(manager + " # " + sourcePath + " # " + object);
        if (manager == null)
            return false;

        if (!sourcePath.contains(".")) {
            return manager.put(sourcePath, object) != null;
        }
        if (sourcePath.endsWith("."))
            return false;

        final String[] splitSourcePath = sourcePath.split("\\.");
        final String pathPart = splitSourcePath[0];

        if (!manager.containsKey(pathPart) && object == null)
            return false;

        if (!manager.containsKey(pathPart))
            manager.put(pathPart, new JSONObject());

        if (!(manager.get(pathPart) instanceof JSONObject newJsonObject))
            return false;

        final String newSourcePath = sourcePath.replace(pathPart + ".", "");
        this.write(newSourcePath, newJsonObject, object);

        return false;
    }

    /**
     * Requires this {@link JsonManager} to be created with a {@link FileManager}.
     *
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
        return asJsonString();
    }

    JsonManager file(final @NotNull FileManager fileManager) {
        this.fileManager = fileManager;
        return this;
    }

    private Object get(final @NotNull JSONObject jsonObject, final @NotNull String sourcePath) {
        if (!jsonObject.containsKey(sourcePath))
            return null;
        return jsonObject.get(sourcePath);
    }
}
