package moe.tristan.kmdah.service.images.cache.filesystem;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.cache.filesystem")
public record FilesystemSettings(

    Path rootDir,

    boolean readOnly

) {}
