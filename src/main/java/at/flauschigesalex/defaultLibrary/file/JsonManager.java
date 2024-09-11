package at.flauschigesalex.defaultLibrary.file;

import lombok.Getter;
import org.bson.BsonDocument;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "DataFlowIssue", "unchecked", "UnusedReturnValue"})
@Getter
public class JsonManager {

    public static JsonManager copy(final @NotNull JsonManager json) {
        return new JsonManager(json.content);
    }

    public static JsonManager copyOriginal(final @NotNull JsonManager json) {
        return new JsonManager(json.originalContent);
    }

    private final String originalContent;
    private String content;
    private FileHandler fileHandler;

    public JsonManager() {
        this("{}");
    }

    public JsonManager(final @NotNull Object content) {
        this(content.toString());
    }

    public JsonManager(final @NotNull Map<String, Object> map) {
        this();
        this.writeMany(map);
    }

    public JsonManager(final @NotNull Document document) {
        this(document.toJson());
    }

    public JsonManager(final @NotNull HttpResponse<String> response) {
        this(response.body());
    }

    public JsonManager(final @NotNull File file) {
        this(new FileHandler(file).readString());
    }

    public JsonManager(final @NotNull ResourceHandler resource) {
        this(resource.readString());
    }

    JsonManager(final @NotNull String content) {
        try {
            new JSONParser().parse(content);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to read json from string:\n"+content);
        }

        this.originalContent = content;
        this.content = content;
    }

    public Object asObject() {
        try {
            return new JSONParser().parse(getContent());
        } catch (ParseException ignore) {
        }
        return null;
    }
    public JSONObject asJsonObject() {
        try {
            return (JSONObject) asObject();
        } catch (Exception ignore) {
        }
        return null;
    }
    public JSONArray asJsonArray() {
        try {
            return (JSONArray) asObject();
        } catch (Exception ignore) {
        }
        return new JSONArray();
    }
    @Deprecated(forRemoval = true) public String asJsonString() {
        return content;
    }

    private Object get(final @NotNull JSONObject jsonObject, final @NotNull String path) {
        if (!jsonObject.containsKey(path))
            return null;
        return jsonObject.get(path);
    }

    public Object getObject(final @NotNull String path) {
        if (path.isEmpty())
            return asObject();

        JSONObject jsonObject = asJsonObject();
        if (jsonObject == null)
            return null;

        if (!path.contains(".")) {
            return get(jsonObject, path);
        }
        JSONObject current = jsonObject;
        String[] splitSourcePath = path.split("\\.");
        for (String splitSource : splitSourcePath) {
            Object object = get(current, splitSource);
            if (object == null)
                return null;

            if (path.endsWith(splitSource))
                return current.get(splitSource);
            try {
                current = (JSONObject) object;
            } catch (Exception ignore) {
                break;
            }
        }
        return null;
    }
    public JSONObject getJsonObject(final @NotNull String path) {
        if (path.isEmpty())
            return asJsonObject();

        try {
            return (JSONObject) getObject(path);
        } catch (Exception ignore) {
        }
        return null;
    }
    public JsonManager getJsonManager(final @NotNull String path) {
        try {
            return new JsonManager(getJsonObject(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public JSONArray getJsonArray(final @NotNull String path) {
        try {
            return (JSONArray) getObject(path);
        } catch (Exception ignore) {
        }
        return new JSONArray();
    }
    public ArrayList<Object> getObjectList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Object>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<String> getStringList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<String>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<Integer> getIntegerList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Integer>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<Long> getLongList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Long>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<Short> getShortList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Short>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<Double> getDoubleList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Double>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<Float> getFloatList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Float>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }
    public ArrayList<Boolean> getBooleanList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Boolean>) getObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public String getString(final @NotNull String path) {
        Object object = getObject(path);
        if (object == null)
            return null;
        return object.toString();
    }
    public Integer getInteger(final @NotNull String path) {
        try {
            return Integer.parseInt(getString(path));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Long getLong(final @NotNull String path) {
        try {
            return Long.parseLong(getString(path));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Short getShort(final @NotNull String path) {
        try {
            return Short.parseShort(getString(path));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Double getDouble(final @NotNull String path) {
        try {
            return Double.parseDouble(getString(path));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Float getFloat(final @NotNull String path) {
        try {
            return Float.parseFloat(getString(path));
        } catch (Exception ignore) {
        }
        return null;
    }
    public Boolean getBoolean(final @NotNull String path) {
        try {
            return Boolean.parseBoolean(getString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean copyOf(final @NotNull String path, final @NotNull String newPath) {
        return this.copyOf(path, newPath, true);
    }
    public boolean copyOf(final @NotNull String path, final @NotNull String newPath, final boolean override) {
        if (!this.has(path) && !override)
            return false;
        if (this.has(newPath) && !override)
            return false;

        return this.write(newPath, this.getObject(path));
    }

    public boolean move(final @NotNull String path, final @NotNull String newPath) {
        return this.move(path, newPath, true);
    }
    public boolean move(final @NotNull String path, final @NotNull String newPath, final boolean override) {
        if (!this.has(path) && !override)
            return false;
        if (this.has(newPath) && !override)
            return false;

        if (!this.write(newPath, this.getObject(path)))
            return false;
        return this.remove(path);
    }

    public boolean removeMany(final @NotNull String... paths) {
        return this.removeMany(List.of(paths));
    }
    public boolean removeMany(final @NotNull Collection<String> paths) {
        final AtomicBoolean success = new AtomicBoolean(true);
        for (final String path : paths)
            if (!this.remove(path))
                success.set(false);

        return success.get();
    }
    public boolean remove(final @NotNull String path) {
        if (!this.has(path))
            return true;

        if (!path.contains(".")) {
            final var jsonObject = this.asJsonObject();
            jsonObject.remove(path);
            this.content = jsonObject.toString();

            this.checkRemoveEmpty();
            return true;
        }

        final var child = List.of(path.split("\\.")).getLast();
        final var parent = path.substring(0, path.length() - (child.length() + 1));

        final var jsonObject = this.getJsonObject(parent);
        jsonObject.remove(child);
        this.write(parent, jsonObject);

        this.checkRemoveEmpty();

        return true;
    }

    public boolean writeIfAbsent(final @NotNull String path, final @Nullable Object object) {
        if (this.has(path))
            return true;
        return write(path, object);
    }
    public boolean writeMany(final @NotNull Map<String, Object> map) {
        final AtomicBoolean success = new AtomicBoolean(true);
        map.forEach((string, object) -> {
            if (!this.write(string, object))
                success.set(false);
        });

        return success.get();
    }
    public boolean write(final @NotNull String path, final @Nullable Object object) {
        return this.write(path, asJsonObject(), object) != null;
    }
    private JSONObject write(final @NotNull String path, final @NotNull JSONObject jsonObject, @Nullable Object object) {
        if (object == null) {
            this.remove(path);
            return jsonObject;
        }

        final ArrayList<String> parts = new ArrayList<>(List.of(path.split("\\.")));
        final JSONObject original = asJsonObject();
        JSONObject current = original;

        for (int i = 0; i < parts.size(); i++) {
            final String part = parts.get(i);
            final @Nullable Object currentObject = current.get(part);

            if (i == parts.size() - 1) {
                if (object instanceof Enum<?> anEnum)
                    object = anEnum.toString();
                if (object instanceof JsonManager json)
                    object = json.asJsonObject();

                current.put(part, object);
                this.content = original.toJSONString();
                return current;
            }

            if (currentObject instanceof JSONObject jObject) {
                current = jObject;
                continue;
            }

            if (currentObject == null) {
                final JSONObject newObject = new JSONObject();
                current.put(part, newObject);
                current = newObject;
                continue;
            }
            return null;
        }
        return null;
    }

    private boolean checkRemoveEmpty() {
        return this.checkRemoveEmpty(asJsonObject(), new StringBuilder());
    }
    private boolean checkRemoveEmpty(final @NotNull JSONObject jsonObject, @NotNull StringBuilder path) {
        for (final Object object : jsonObject.keySet()) {
            if (!(object instanceof JSONObject newJsonObject))
                continue;
            if (!checkRemoveEmpty(newJsonObject, path.append(path.isEmpty() ? "" : ".").append(object)))
                continue;
            if (!newJsonObject.isEmpty())
                continue;
            jsonObject.remove(object);
        }
        return false;
    }

    public boolean has(String path) {
        return getObject(path) != null;
    }

    public boolean isOriginalContent() {
        return content.equals(originalContent);
    }
    public boolean isModifiedContent() {
        return !isOriginalContent();
    }

    public Document toDocument() {
        return Document.parse(content);
    }
    public BsonDocument toBsonDocument() {
        return toDocument().toBsonDocument();
    }
    public String toString() {
        return getContent();
    }
}
