package at.flauschigesalex.defaultLibrary.database.mongo;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.database.DatabaseCredentials;
import at.flauschigesalex.defaultLibrary.database.mongo.annotations.MongoIgnore;
import at.flauschigesalex.defaultLibrary.database.mongo.annotations.MongoInformation;
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
import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Getter
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class MongoManager {
    private final static ArrayList<MongoManager> list = new ArrayList<>();

    public static MongoManager construct(FlauschigeLibrary api, DatabaseCredentials credentials) {
        return new MongoManager(api, credentials);
    }

    @Getter(AccessLevel.NONE) private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private final FlauschigeLibrary API;
    private final DatabaseCredentials credentials;

    public MongoCollection<?> getCollection(String name, Class<?> mongoCollectionClass) {
        if (!informationClasses.containsKey(mongoCollectionClass))
            throw new MongoInternalException("Class " + mongoCollectionClass + " cannot be accessed since it wasn't registered.");
        return getMongoDatabase().getCollection(name, mongoCollectionClass);
    }

    private MongoManager(FlauschigeLibrary api, DatabaseCredentials credentials) {
        this.API = api;
        this.credentials = credentials;
        list.add(this);
    }

    private final HashMap<Class<?>, MongoDatabaseRegisterInformationClass> informationClasses = new HashMap<>();
    private final ArrayList<Class<?>> ignoredInformationClasses = new ArrayList<>();

    @Getter
    public enum MongoDatabaseRegisterInformationClass {
        MANUAL(""),
        ANNOTATION("@Annotation"),
        SUPERCLASS("{0}");

        private final String registerTypeString;

        MongoDatabaseRegisterInformationClass(String registerTypeString) {
            this.registerTypeString = registerTypeString;
        }
    }

    @CheckReturnValue
    public MongoManager register(Class<?>... informationClass) {
        for (Class<?> infoClass : informationClass) {
            informationClasses.put(infoClass, MongoDatabaseRegisterInformationClass.MANUAL);
        }
        return this;
    }

    public void close() {
        mongoClient.close();
        list.remove(this);
    }

    @SuppressWarnings("unchecked")
    private String getMongoInformationClassPath(Class<?> clazz) {
        String text = "instanceof {1}";
        StringBuilder superClassBuilder = new StringBuilder();
        while (clazz.getGenericSuperclass() != null && !clazz.getGenericSuperclass().equals(MongoInformationClass.class)) {
            if (clazz.getGenericSuperclass().equals(Object.class))
                return superClassBuilder.append("implements @Annotation").toString();
            String[] className = clazz.getGenericSuperclass().getTypeName().split("\\.");
            superClassBuilder.append(text.replace("{1}", className[className.length - 1])).append(" ");
            clazz = (Class<? extends MongoInformationClass>) clazz.getGenericSuperclass();
        }
        return superClassBuilder.append("extends ").append(MongoInformationClass.class.getSimpleName()).toString();
    }

    @SuppressWarnings("unchecked")
    public MongoManager connect() {
        try {
            MongoCredential credential = MongoCredential.createCredential(getCredentials().getUsername(), getCredentials().getDatabase(), getCredentials().getAccessKey().toCharArray());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .credential(credential)
                    .applyToClusterSettings(builder -> builder.hosts(List.of(new ServerAddress(getCredentials().getHostname(), getCredentials().getPort()))))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();

            PojoCodecProvider.Builder pojoCodecProviderBuilder = PojoCodecProvider.builder();

            for (Class<?> annotatedClass : Reflector.getReflector().reflect(getAPI().getOwnDirectoryPath()).getAnnotatedClasses(MongoInformation.class)) {
                MongoDatabaseRegisterInformationClass registerInformation = (annotatedClass.getGenericSuperclass() != null && !annotatedClass.isAnnotationPresent(MongoInformation.class)) ? MongoDatabaseRegisterInformationClass.SUPERCLASS : MongoDatabaseRegisterInformationClass.ANNOTATION;
                if (informationClasses.containsKey(annotatedClass)) continue;
                informationClasses.put(annotatedClass, registerInformation);
            }
            for (String workingDirectory : getAPI().getWorkingDirectoryPath()) {
                for (Class<?> annotatedClass : Reflector.getReflector().reflect(workingDirectory).getAnnotatedClasses(MongoInformation.class)) {
                    MongoDatabaseRegisterInformationClass registerInformation = (annotatedClass.getGenericSuperclass() != null && !annotatedClass.isAnnotationPresent(MongoInformation.class)) ? MongoDatabaseRegisterInformationClass.SUPERCLASS : MongoDatabaseRegisterInformationClass.ANNOTATION;
                    if (informationClasses.containsKey(annotatedClass)) continue;
                    informationClasses.put(annotatedClass, registerInformation);
                }
            }

            for (Class<?> infoClass : new ArrayList<>(informationClasses.keySet())) {
                if (!infoClass.isAnnotationPresent(MongoIgnore.class)) continue;
                informationClasses.remove(infoClass);
                if (informationClasses.get(infoClass) == MongoDatabaseRegisterInformationClass.MANUAL)
                    System.err.println("WARNING! Class @MongoIgnore '" + infoClass.getName() + "' is registered manually, but was removed due to its @Annotation.");
                ignoredInformationClasses.add(infoClass);
            }
            if (!informationClasses.isEmpty()) {
                Class<?>[] classes = informationClasses.keySet().toArray(new Class[]{});
                pojoCodecProviderBuilder.register(classes);
                StringBuilder outBuilder = new StringBuilder("\n" + informationClasses.size() + " class" + (informationClasses.size() > 1 ? "es are" : " is") + " now available in your MongoDatabase.");
                ArrayList<Class<?>> informationClasses = new ArrayList<>(this.informationClasses.keySet());
                informationClasses.sort(new MongoInformationClassComparator());
                for (Class<?> informationClass : informationClasses) {
                    outBuilder.append("\n - ").append(informationClass.getName()).append(" ");
                    MongoDatabaseRegisterInformationClass registerInformation = this.informationClasses.get(informationClass);
                    if (registerInformation == MongoDatabaseRegisterInformationClass.MANUAL) continue;
                    String registerTypeString = registerInformation.getRegisterTypeString();
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
                StringBuilder outBuilder = new StringBuilder("WARNING! " + (ignoredInformationClasses.size() > 1 ? "These " : "This ") + (ignoredInformationClasses.size() + " class" + (ignoredInformationClasses.size() > 1 ? "es" : "") + " will not be ignored from your MongoDatabase:"));
                for (Class<?> aClass : ignoredInformationClasses) {
                    outBuilder.append("\n - ").append(aClass.getName()).append(" ");
                }
                System.err.println(outBuilder.append("\n"));
            }
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProviderBuilder.build()));

            mongoClient = MongoClients.create(settings);
            mongoDatabase = mongoClient.getDatabase(getCredentials().getDatabase()).withCodecRegistry(pojoCodecRegistry);

            return this;
        } catch (IllegalStateException exception) {
            throw new MongoClientException(exception.getMessage());
        }
    }
}
