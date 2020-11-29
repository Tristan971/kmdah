package moe.tristan.kmdah.service.images.cache.mongodb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache.mongodb")
public record MongodbSettings(

    String host,

    int port,

    String authenticationDatabase,

    String username,

    String password

) {}
