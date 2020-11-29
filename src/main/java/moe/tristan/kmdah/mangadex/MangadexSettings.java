package moe.tristan.kmdah.mangadex;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.mangadex")
public record MangadexSettings(

    String clientSecret

) {}
