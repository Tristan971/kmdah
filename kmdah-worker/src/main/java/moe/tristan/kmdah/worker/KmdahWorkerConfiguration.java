package moe.tristan.kmdah.worker;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import moe.tristan.kmdah.common.KmdahCommonConfiguration;

@Configuration
@Import(KmdahCommonConfiguration.class)
public class KmdahWorkerConfiguration {

}
