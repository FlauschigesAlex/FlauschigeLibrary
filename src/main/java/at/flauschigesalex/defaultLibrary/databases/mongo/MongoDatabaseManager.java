package at.flauschigesalex.defaultLibrary.databases.mongo;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.databases.DatabaseCredentials;
import at.flauschigesalex.defaultLibrary.databases.mongo.annotations.MongoClass;
import at.flauschigesalex.defaultLibrary.databases.mongo.annotations.MongoIgnore;
import at.flauschigesalex.defaultLibrary.utils.reflections.Reflector;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.AccessLevel;
import lombok.Getter;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Getter
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class MongoDatabaseManager {

    private final FlauschigeLibrary library;
    private final DatabaseCredentials credentials;
    private final HashMap<Class<?>, MongoDatabaseRegisterInformationClass> informationClasses = new HashMap<>();
    private final ArrayList<Class<?>> ignoredInformationClasses = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDatabaseManager(final @NotNull FlauschigeLibrary library, final @NotNull DatabaseCredentials credentials) {
        this.library = library;
        this.credentials = credentials;
    }

    public <C> MongoCollection<C> getCollection(final @NotNull String name, final @NotNull Class<C> mongoCollectionClass) {
        if (!informationClasses.containsKey(mongoCollectionClass))
            throw new MongoInternalException("Class " + mongoCollectionClass + " cannot be accessed since it wasn't registered.");
        return getMongoDatabase().getCollection(name, mongoCollectionClass);
    }

    public MongoCollection<?> getCollection(final @NotNull String name) {
        return getMongoDatabase().getCollection(name);
    }

    @CheckReturnValue
    public MongoDatabaseManager register(final @NotNull Class<?>... informationClass) {
        for (final Class<?> infoClass : informationClass) {
            informationClasses.put(infoClass, MongoDatabaseRegisterInformationClass.MANUAL);
        }
        return this;
    }

    @CheckReturnValue
    public MongoDatabaseManager registerSilent(final @NotNull Class<?>... informationClass) {
        for (final Class<?> infoClass : informationClass) {
            informationClasses.put(infoClass, MongoDatabaseRegisterInformationClass.SILENT);
        }
        return this;
    }

    public void close() {
        mongoClient.close();
    }

    @SuppressWarnings("unchecked")
    public MongoDatabaseManager connect() {
        try {
            final ArrayList<ServerAddress> addresses = new ArrayList<>();
            for (int credential = 0; credential < getCredentials().getHostnames().size(); credential++) {
                addresses.add(new ServerAddress(getCredentials().getHostnames().get(credential), getCredentials().getPorts().get(credential)));
            }
            final MongoCredential credential = MongoCredential.createCredential(getCredentials().getUsername(), getCredentials().getDatabase(), getCredentials().getAccessKey().toCharArray());
            final MongoClientSettings settings = MongoClientSettings.builder()
                    .credential(credential)
                    .applyToClusterSettings(builder -> builder.hosts(addresses))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();

            final PojoCodecProvider.Builder pojoCodecProviderBuilder = PojoCodecProvider.builder();

            for (final Class<?> annotatedClass : Reflector.getReflector().reflect(getLibrary().getOwnDirectoryPath()).getAnnotatedClasses(MongoClass.class)) {
                final MongoDatabaseRegisterInformationClass registerInformation = (annotatedClass.getGenericSuperclass() != null && !annotatedClass.isAnnotationPresent(MongoClass.class)) ? MongoDatabaseRegisterInformationClass.SUPERCLASS : MongoDatabaseRegisterInformationClass.ANNOTATION;
                if (informationClasses.containsKey(annotatedClass)) continue;
                informationClasses.put(annotatedClass, registerInformation);
            }
            for (final String workingDirectory : getLibrary().getWorkingDirectoryPath()) {
                for (final Class<?> annotatedClass : Reflector.getReflector().reflect(workingDirectory).getAnnotatedClasses(MongoClass.class)) {
                    final MongoDatabaseRegisterInformationClass registerInformation = (annotatedClass.getGenericSuperclass() != null && !annotatedClass.isAnnotationPresent(MongoClass.class)) ? MongoDatabaseRegisterInformationClass.SUPERCLASS : MongoDatabaseRegisterInformationClass.ANNOTATION;
                    if (informationClasses.containsKey(annotatedClass)) continue;
                    informationClasses.put(annotatedClass, registerInformation);
                }
            }

            for (final Class<?> infoClass : new ArrayList<>(informationClasses.keySet())) {
                if (!infoClass.isAnnotationPresent(MongoIgnore.class)) continue;
                informationClasses.remove(infoClass);
                if (informationClasses.get(infoClass) == MongoDatabaseRegisterInformationClass.MANUAL)
                    System.err.println("WARNING! Class @" + MongoIgnore.class.getSimpleName() + " '" + infoClass.getName() + "' is registered manually, but was removed due to its @Annotation.");
                ignoredInformationClasses.add(infoClass);
            }
            if (!informationClasses.isEmpty()) {
                final Class<?>[] classes = informationClasses.keySet().toArray(new Class[]{});
                pojoCodecProviderBuilder.register(classes);
                final StringBuilder outBuilder = new StringBuilder("\n" + informationClasses.size() + " class" + (informationClasses.size() > 1 ? "es are" : " is") + " now available in your MongoDatabase.");
                final ArrayList<Class<?>> informationClasses = new ArrayList<>(this.informationClasses.keySet());
                informationClasses.sort(new MongoInformationClassComparator());
                for (final Class<?> informationClass : informationClasses) {
                    final MongoDatabaseRegisterInformationClass registerInformation = this.informationClasses.get(informationClass);
                    if (registerInformation == MongoDatabaseRegisterInformationClass.SILENT) continue;

                    outBuilder.append("\n - ").append(informationClass.getName()).append(" ");
                    if (registerInformation == MongoDatabaseRegisterInformationClass.MANUAL) continue;
                    String registerTypeString = registerInformation.registerTypeString;
                    if (registerTypeString == null) continue;
                    if (registerInformation == MongoDatabaseRegisterInformationClass.SUPERCLASS) {
                        Class<? extends MongoInformationClass> mongoInformationClass = (Class<? extends MongoInformationClass>) informationClass;
                        registerTypeString = registerTypeString.replace("{0}", getMongoInformationClassPath(mongoInformationClass));
                    }
                    outBuilder.append("(").append(registerTypeString).append(")");
                }
                System.out.println(outBuilder.append("\n"));
            }
            ignoredInformationClasses.sort(new MongoInformationClassComparator());
            ignoredInformationClasses.remove(MongoInformationClass.class);
            if (!ignoredInformationClasses.isEmpty()) {
                final StringBuilder outBuilder = new StringBuilder("WARNING! " + (ignoredInformationClasses.size() > 1 ? "These " : "This ") + (ignoredInformationClasses.size() + " class" + (ignoredInformationClasses.size() > 1 ? "es" : "") + " will not be ignored from your MongoDatabase:"));
                for (final Class<?> aClass : ignoredInformationClasses) {
                    outBuilder.append("\n - ").append(aClass.getName()).append(" ");
                }
                System.err.println(outBuilder.append("\n"));
            }
            final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProviderBuilder.build()));

            mongoClient = MongoClients.create(settings);
            mongoDatabase = mongoClient.getDatabase(getCredentials().getDatabase()).withCodecRegistry(pojoCodecRegistry);

            return this;
        } catch (final IllegalStateException exception) {
            throw new MongoClientException(exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String getMongoInformationClassPath(@NotNull Class<?> clazz) {
        final String text = "instanceof {1}";
        final StringBuilder superClassBuilder = new StringBuilder();
        while (clazz.getGenericSuperclass() != null && !clazz.getGenericSuperclass().equals(MongoInformationClass.class)) {
            if (clazz.getGenericSuperclass().equals(Object.class))
                return superClassBuilder.append("implements @Annotation").toString();
            final String[] className = clazz.getGenericSuperclass().getTypeName().split("\\.");
            superClassBuilder.append(text.replace("{1}", className[className.length - 1])).append(" ");
            clazz = (Class<? extends MongoInformationClass>) clazz.getGenericSuperclass();
        }
        return superClassBuilder.append("extends ").append(MongoInformationClass.class.getSimpleName()).toString();
    }

    public enum MongoDatabaseRegisterInformationClass {
        MANUAL(""),
        ANNOTATION("@Annotation"),
        SUPERCLASS("{0}"),
        SILENT(null);

        private final String registerTypeString;

        MongoDatabaseRegisterInformationClass(final @Nullable String registerTypeString) {
            this.registerTypeString = registerTypeString;
        }
    }
}
