package moe.tristan.kmdah.worker;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import moe.tristan.kmdah.common.KmdahCommonConfiguration;

@Configuration
@Import(KmdahCommonConfiguration.class)
@ConfigurationPropertiesScan
public class KmdahWorkerConfiguration {

}
