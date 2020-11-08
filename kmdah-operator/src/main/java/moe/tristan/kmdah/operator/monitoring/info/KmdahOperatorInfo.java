package moe.tristan.kmdah.operator.monitoring.info;

import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class KmdahOperatorInfo implements InfoContributor {

    private final Environment environment;

    public KmdahOperatorInfo(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void contribute(Builder builder) {
        builder.withDetail("name", environment.getRequiredProperty("spring.application.name"));
        builder.withDetail("version", environment.getRequiredProperty("spring.application.version"));

        builder.build();
    }

}
