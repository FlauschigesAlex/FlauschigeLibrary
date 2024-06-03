package at.flauschigesalex.defaultLibrary.file;

import lombok.Getter;
import org.bson.BsonDocument;
import org.bson.Document;
import org.jetbrains.annotations.ApiStatus;
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
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "DataFlowIssue", "unchecked", "UnusedReturnValue"})
@Getter
public final class JsonManager {

    public static JsonManager create() {
        return of("{}");
    }

    public static JsonManager copy(final @NotNull JsonManager json) {
        return new JsonManager(json.content);
    }

    public static JsonManager copyOriginal(final @NotNull JsonManager json) {
        return new JsonManager(json.originalContent);
    }

    @ApiStatus.Experimental
    public static @Nullable JsonManager of(final @NotNull Object source) {
        return of(source.toString());
    }

    public static @Nullable JsonManager of(final @NotNull String source) {
        try {
            new JSONParser().parse(source);
        } catch (final Exception e) {
            return null;
        }
        return new JsonManager(source);
    }

    public static @Nullable JsonManager of(final @NotNull StringBuilder source) {
        return of(source.toString());
    }

    public static JsonManager of(final @NotNull Map<String, Object> map) {
        final JsonManager json = JsonManager.create();
        json.writeMany(map);
        return json;
    }

    public static @Nullable JsonManager of(final @NotNull HttpResponse<String> response) {
        return of(response.body());
    }

    public static @Nullable JsonManager of(final @NotNull FileManager fileManager) {
        final String read = fileManager.read();
        if (read == null) return null;
        return of(read);
    }

    public static @Nullable JsonManager of(final @NotNull ResourceManager resourceManager) {
        final String read = resourceManager.read();
        if (read == null) return null;
        return of(read);
    }

    public static @Nullable JsonManager of(final @NotNull File file) {
        return of(FileManager.of(file));
    }

    public static @Nullable JsonManager of(final @NotNull InputStream fileInputStream) {
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
        return of(builder);
    }

    public static @Nullable JsonManager of(final @NotNull URL resource) {
        try {
            return of(resource.openStream());
        } catch (IOException ignore) {
        }
        return null;
    }

    private final String originalContent;
    private String content;
    private FileManager fileManager;

    JsonManager(final @NotNull String content) {
        this.originalContent = content;
        this.content = content;
    }

    public JSONObject asJsonObject() {
        try {
            return (JSONObject) asObject();
        } catch (Exception ignore) {
        }
        return null;
    }

    public Object asObject() {
        try {
            return new JSONParser().parse(getContent());
        } catch (ParseException ignore) {
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

    public String asJsonString() {
        return content;
    }

    @ApiStatus.Experimental
    public <W> W as(final @NotNull String path) {
        return (W) asObject(path);
    }

    public Object asObject(final @NotNull String path) {
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

    public JSONObject asJsonObject(final @NotNull String path) {
        try {
            return (JSONObject) asObject(path);
        } catch (Exception ignore) {
        }
        return null;
    }

    public JsonManager asJsonManager(final @NotNull String path) {
        try {
            return JsonManager.of(asJsonObject(path).toJSONString());
        } catch (Exception ignore) {
        }
        return null;
    }

    public JSONArray asJsonArray(final @NotNull String path) {
        try {
            return (JSONArray) asObject(path);
        } catch (Exception ignore) {
        }
        return new JSONArray();
    }

    @ApiStatus.Experimental
    public <W> ArrayList<W> asList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<W>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Object> asObjectList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Object>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<String> asStringList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<String>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Short> asShortList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Short>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Integer> asIntegerList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Integer>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Long> asLongList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Long>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Float> asFloatList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Float>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Double> asDoubleList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Double>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Boolean> asBooleanList(final @NotNull String path) {
        try {
            return new ArrayList<>((ArrayList<Boolean>) asObject(path));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public String asString(final @NotNull String path) {
        Object object = asObject(path);
        if (object == null)
            return null;
        return object.toString();
    }

    public Short asShort(final @NotNull String path) {
        try {
            return Short.parseShort(asString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Integer asInteger(final @NotNull String path) {
        try {
            return Integer.parseInt(asString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Long asLong(final @NotNull String path) {
        try {
            return Long.parseLong(asString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Float asFloat(final @NotNull String path) {
        try {
            return Float.parseFloat(asString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Double asDouble(final @NotNull String path) {
        try {
            return Double.parseDouble(asString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Boolean asBoolean(final @NotNull String path) {
        try {
            return Boolean.parseBoolean(asString(path));
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean copy(final @NotNull String path, final @NotNull String newPath) {
        return this.copy(path, newPath, true);
    }

    public boolean copy(final @NotNull String path, final @NotNull String newPath, final boolean override) {
        if (!this.contains(path) && !override)
            return false;
        if (this.contains(newPath) && !override)
            return false;

        return this.write(newPath, this.asObject(path));
    }

    public boolean move(final @NotNull String path, final @NotNull String newPath) {
        return this.move(path, newPath, true);
    }

    public boolean move(final @NotNull String path, final @NotNull String newPath, final boolean override) {
        if (!this.contains(path) && !override)
            return false;
        if (this.contains(newPath) && !override)
            return false;

        if (!this.write(newPath, this.asObject(path)))
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
        if (!this.contains(path))
            return true;
        final String[] splitSourcePath = path.split("\\.");
        final String pathPart = splitSourcePath[splitSourcePath.length - 1];

        final StringBuilder newPath = new StringBuilder();
        for (int part = 0; part < splitSourcePath.length - 1; part++) {
            if (part != 0)
                newPath.append(".");
            newPath.append(splitSourcePath[part]);
        }

        final JSONObject jsonObject = path.contains(".") ? asJsonObject(newPath.toString()) : asJsonObject();
        if (jsonObject == null)
            return true;
        jsonObject.remove(pathPart);
        content = jsonObject.toJSONString();
        checkRemoveEmpty();

        return false;
    }

    public boolean writeIfAbsent(final @NotNull String path, final @Nullable Object object) {
        if (this.contains(path))
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

            if (i == parts.size()-1) {
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

    public boolean checkRemoveEmpty() {
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

    public boolean updateModifiedFile() {
        if (isOriginalContent())
            return false;
        return updateFile();
    }

    public boolean updateFile() {
        if (this.fileManager == null || !this.fileManager.isWritable())
            return false;
        return this.fileManager.write(this.content);
    }

    public boolean contains(String path) {
        return asObject(path) != null;
    }

    public boolean isOriginalContent() {
        return content.equals(originalContent);
    }

    public boolean isModifiedContent() {
        return !isOriginalContent();
    }

    JsonManager file(final @NotNull FileManager fileManager) {
        this.fileManager = fileManager;
        return this;
    }

    private Object get(final @NotNull JSONObject jsonObject, final @NotNull String path) {
        if (!jsonObject.containsKey(path))
            return null;
        return jsonObject.get(path);
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
