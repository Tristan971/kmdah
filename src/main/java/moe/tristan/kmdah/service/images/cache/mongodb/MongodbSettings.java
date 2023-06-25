package moe.tristan.kmdah.service.images.cache.mongodb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.cache.mongodb")
public record MongodbSettings(

    @NotBlank
    String host,

    @Range(min = 0, max = 65535)
    int port,

    @NotBlank
    String authenticationDatabase,

    @NotBlank
    String database,

    @NotBlank
    String username,

    @NotBlank
    String password

) { }
