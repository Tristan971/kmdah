package moe.tristan.kmdah.service.images.cache.filesystem;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache.filesystem")
public record FilesystemSettings(

    Path rootDir,

    boolean useAltDir,

    Path altDir,

    boolean readOnly

) {}
