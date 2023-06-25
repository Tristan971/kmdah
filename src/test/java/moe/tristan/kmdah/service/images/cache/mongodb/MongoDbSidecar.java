package moe.tristan.kmdah.service.images.cache.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class MongoDbSidecar implements Extension, BeforeAllCallback, AfterAllCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbSidecar.class);

    private static final int MONGODB_PORT = 27017;

    private final GenericContainer<?> mongodb = new GenericContainer<>("library/mongo:5")
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "kmdah")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "kmdah")
        .withEnv("MONGO_INITDB_DATABASE", "test")
        .withLogConsumer(frame -> LOGGER.info("[MONGODB]: " + frame.getUtf8String().trim()))
        .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Waiting for connections.*"))
        .withExposedPorts(MONGODB_PORT);

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        mongodb.start();

        String mongoHost = mongodb.getHost();
        System.setProperty("KMDAH_CACHE_MONGODB_HOST", mongoHost);

        Integer mongoPort = mongodb.getMappedPort(MONGODB_PORT);
        System.setProperty("KMDAH_CACHE_MONGODB_PORT", String.valueOf(mongoPort));

        System.setProperty("KMDAH_CACHE_MONGODB_AUTHENTICATION_DATABASE", "admin");
        System.setProperty("KMDAH_CACHE_MONGODB_DATABASE", "test");

        assertThat(mongodb.isRunning()).isTrue();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        mongodb.stop();
    }

}
