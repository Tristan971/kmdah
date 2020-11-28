package moe.tristan.kmdah.service.elections;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("kmdah.candidacy")
@ConstructorBinding
public record CandidacySettings(

    CandidacyIdGenerationMethod idGenerationMethod,

    String staticId

) {

    public enum CandidacyIdGenerationMethod {
        STATIC,
        HOSTNAME,
        RANDOM_UUID
    }

}
