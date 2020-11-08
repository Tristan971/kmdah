package moe.tristan.kmdah.operator.monitoring.info;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.common.model.settings.UserSettings;

@Component
public class KmdahOperatorInfo implements InfoContributor {

    private final Environment environment;
    private final Map<String, UserSettings> userSettings;

    public KmdahOperatorInfo(Environment environment, Map<String, UserSettings> userSettings) {
        this.environment = environment;
        this.userSettings = userSettings;
    }

    @Override
    public void contribute(Builder builder) {
        builder.withDetail("name", environment.getRequiredProperty("spring.application.name"));
        builder.withDetail("version", environment.getRequiredProperty("spring.application.version"));

        Map<String, Object> settings = new HashMap<>(userSettings);
        builder.withDetails(settings);

        builder.build();
    }

}
