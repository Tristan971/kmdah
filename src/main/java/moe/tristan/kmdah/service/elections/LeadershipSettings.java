package moe.tristan.kmdah.service.elections;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("kmdah.leadership")
@ConstructorBinding
public record LeadershipSettings(

    CandidateIdGenerationMethod idGenerationMethod,

    String staticId

) {

    public enum CandidateIdGenerationMethod {
        STATIC,
        HOSTNAME,
        RANDOM_UUID
    }

}
