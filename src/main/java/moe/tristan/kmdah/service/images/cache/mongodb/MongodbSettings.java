package moe.tristan.kmdah.service.images.cache.mongodb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.cache.mongodb")
public record MongodbSettings(

    String host,

    int port,

    String authenticationDatabase,

    String username,

    String password

) {}
