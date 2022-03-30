package moe.tristan.kmdah.service.workers;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.worker")
public record WorkerSettings(

    int port

) {}
