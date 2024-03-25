package at.flauschigesalex.defaultLibrary.databases.mongo;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.databases.DatabaseCredentials;
import at.flauschigesalex.defaultLibrary.reflections.Reflector;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.AccessLevel;
import lombok.Getter;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Getter
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class MongoDatabaseManager {

    private final FlauschigeLibrary library;
    private final DatabaseCredentials credentials;
    private final HashSet<Class<?>> mongoClasses = new HashSet<>();
    @Getter(AccessLevel.NONE)
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDatabaseManager(final @NotNull FlauschigeLibrary library, final @NotNull DatabaseCredentials credentials) {
        this.library = library;
        this.credentials = credentials;
    }

    public <C> MongoCollection<C> getCollection(final @NotNull String name, final @NotNull Class<C> mongoCollectionClass) {
        if (!mongoClasses.contains(mongoCollectionClass))
            throw new MongoInternalException("Class " + mongoCollectionClass + " cannot be accessed since it wasn't registered.");
        return getMongoDatabase().getCollection(name, mongoCollectionClass);
    }

    public MongoCollection<Document> getCollection(final @NotNull String name) {
        return getMongoDatabase().getCollection(name);
    }

    @CheckReturnValue
    public MongoDatabaseManager register(final @NotNull Class<?>... informationClass) {
        mongoClasses.addAll(Arrays.asList(informationClass));
        return this;
    }

    public void close() {
        mongoClient.close();
    }

    public MongoDatabaseManager connect() {
        try {
            final ArrayList<ServerAddress> addresses = new ArrayList<>();
            for (int credential = 0; credential < getCredentials().getHostnames().size(); credential++) {
                addresses.add(new ServerAddress(getCredentials().getHostnames().get(credential), getCredentials().getPorts().get(credential)));
            }
            final MongoCredential credential = MongoCredential.createCredential(getCredentials().getUsername(), getCredentials().getDatabase(), getCredentials().getPassword().toCharArray());
            final MongoClientSettings settings = MongoClientSettings.builder()
                    .credential(credential)
                    .applyToClusterSettings(builder -> builder.hosts(addresses))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();

            final PojoCodecProvider.Builder pojoCodecProviderBuilder = PojoCodecProvider.builder();
            this.mongoClasses.addAll(Reflector.getReflector().reflect().getSubClasses(MongoInformation.class));

            if (!mongoClasses.isEmpty()) {
                pojoCodecProviderBuilder.register(mongoClasses.toArray(Class[]::new));

                final ArrayList<Class<?>> informationClasses = new ArrayList<>(this.mongoClasses);
                informationClasses.sort(new MongoComparator());

                final StringBuilder outBuilder = new StringBuilder();
                outBuilder.append("\n").append(mongoClasses.size()).append(" class").append(mongoClasses.size() > 1 ? "es are" : " is").append(" now available in your MongoDatabase.");

                for (final Class<?> informationClass : informationClasses) {
                    if (List.of(informationClass.getInterfaces()).contains(LibraryMongoInformation.class))
                        continue;
                    if (informationClass == LibraryMongoInformation.class)
                        continue;

                    outBuilder.append("\n - ").append(informationClass.getName());
                }

                System.out.println(outBuilder.append("\n"));
            }
            final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProviderBuilder.build()));

            mongoClient = MongoClients.create(settings);
            mongoDatabase = mongoClient.getDatabase(getCredentials().getDatabase()).withCodecRegistry(pojoCodecRegistry);

            return this;
        } catch (final IllegalStateException exception) {
            throw new MongoClientException(exception.getMessage());
        }
    }
}
