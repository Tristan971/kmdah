package moe.tristan.kmdah.service.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class KmdahInfoContributor implements InfoContributor {

    private final String version;

    public KmdahInfoContributor(@Value("${spring.application.version}") String version) {
        this.version = version;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("version", version);
    }

}
