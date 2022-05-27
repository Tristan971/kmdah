package moe.tristan.kmdah.service.tls.file;

import java.nio.file.Path;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.tls.file")
public record FileTlsConfigurationSettings(

    @NotNull
    Path certificateOutputFile,

    @NotNull
    Path privateKeyOutputFile

) { }
