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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings({"unused", "DataFlowIssue", "unchecked", "UnusedReturnValue"})
@Getter
public final class JsonManager {

    public static JsonManager createNew() {
        return parse("{}");
    }

    public static JsonManager copy(final @NotNull JsonManager json) {
        return new JsonManager(json.content);
    }

    public static JsonManager copyOriginal(final @NotNull JsonManager json) {
        return new JsonManager(json.originalContent);
    }

    public static JsonManager writeNew(final @NotNull String sourcePath, final Object object) {
        final JsonManager manager = createNew();
        manager.write(sourcePath, object);
        return manager;
    }

    public static @Nullable JsonManager parse(final @NotNull String source) {
        try {
            new JSONParser().parse(source);
        } catch (final Exception e) {
            return null;
        }
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
        return new JSONArray();
    }

    public ArrayList<Object> asList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Object>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<String> asStringList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<String>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Short> asShortList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Short>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Integer> asIntegerList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Integer>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Long> asLongList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Long>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Float> asFloatList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Float>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Double> asDoubleList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Double>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    public ArrayList<Boolean> asBooleanList(final @NotNull String sourcePath) {
        try {
            return new ArrayList<>((ArrayList<Boolean>) asObject(sourcePath));
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
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
            return Integer.parseInt(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Long asLong(final @NotNull String sourcePath) {
        try {
            return Long.parseLong(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Float asFloat(final @NotNull String sourcePath) {
        try {
            return Float.parseFloat(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Double asDouble(final @NotNull String sourcePath) {
        try {
            return Double.parseDouble(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public Boolean asBoolean(final @NotNull String sourcePath) {
        try {
            return Boolean.parseBoolean(asString(sourcePath));
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean copy(final @NotNull String sourcePath, final @NotNull String newPath) {
        return this.copy(sourcePath, newPath, true);
    }

    public boolean copy(final @NotNull String sourcePath, final @NotNull String newPath, final boolean override) {
        if (!this.contains(sourcePath) && !override)
            return false;
        if (this.contains(newPath) && !override)
            return false;

        return this.write(newPath, this.asObject(sourcePath));
    }

    public boolean move(final @NotNull String sourcePath, final @NotNull String newPath) {
        return this.move(sourcePath, newPath, true);
    }

    public boolean move(final @NotNull String sourcePath, final @NotNull String newPath, final boolean override) {
        if (!this.contains(sourcePath) && !override)
            return false;
        if (this.contains(newPath) && !override)
            return false;

        if (!this.write(newPath, this.asObject(sourcePath)))
            return false;
        return this.remove(sourcePath);
    }

    public boolean remove(final @NotNull String sourcePath) {
        if (!this.contains(sourcePath))
            return true;
        final String[] splitSourcePath = sourcePath.split("\\.");
        final String pathPart = splitSourcePath[splitSourcePath.length - 1];

        final StringBuilder newPath = new StringBuilder();
        for (int part = 0; part < splitSourcePath.length - 1; part++) {
            if (part != 0)
                newPath.append(".");
            newPath.append(splitSourcePath[part]);
        }

        final JSONObject jsonObject = sourcePath.contains(".") ? asJsonObject(newPath.toString()) : asJsonObject();
        if (jsonObject == null)
            return true;
        jsonObject.remove(pathPart);
        content = jsonObject.toJSONString();
        checkRemoveEmpty();

        return false;
    }

    public boolean writeIfAbsent(final @NotNull String sourcePath, final @Nullable Object object) {
        if (this.contains(sourcePath))
            return true;
        return write(sourcePath, object);
    }

    public boolean write(final @NotNull String sourcePath, final @Nullable Object object) {
        return this.write(sourcePath, asJsonObject(), object, true) != null;
    }

    private JSONObject write(final @NotNull String sourcePath, final @Nullable JSONObject jsonObject, final @Nullable Object object, final boolean source) {
        final JSONObject manager = jsonObject == null ? asJsonObject() : jsonObject;
        if (manager == null)
            return null;

        if (object == null) {
            this.remove(sourcePath);
            return manager;
        }

        if (!sourcePath.contains(".")) {
            manager.put(sourcePath, object);
            if (source)
                this.content = manager.toJSONString();
            return manager;
        }
        if (sourcePath.endsWith(".")) {
            return null;
        }

        final String[] splitSourcePath = sourcePath.split("\\.");
        final String pathPart = splitSourcePath[0];

        if (manager.containsKey(pathPart) && !(manager.get(pathPart) instanceof JSONObject)) {
            return null;
        }

        final StringBuilder newPath = new StringBuilder();
        for (int part = 1; part < splitSourcePath.length; part++) {
            newPath.append(splitSourcePath[part]);
            if (part != splitSourcePath.length - 1)
                newPath.append(".");
        }

        final JSONObject newObject = this.write(newPath.toString(), (JSONObject) manager.get(pathPart), object, false);
        manager.put(pathPart, newObject);
        if (source)
            this.content = manager.toString();
        return newObject;
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

    public boolean contains(String sourcePath) {
        return asObject(sourcePath) != null;
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

    private Object get(final @NotNull JSONObject jsonObject, final @NotNull String sourcePath) {
        if (!jsonObject.containsKey(sourcePath))
            return null;
        return jsonObject.get(sourcePath);
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
