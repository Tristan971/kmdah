package moe.tristan.kmdah.service.images.cache.filesystem;

import java.nio.file.Path;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.cache.filesystem")
public record FilesystemSettings(

    @NotNull
    Path rootDir,

    boolean readOnly

) {}
