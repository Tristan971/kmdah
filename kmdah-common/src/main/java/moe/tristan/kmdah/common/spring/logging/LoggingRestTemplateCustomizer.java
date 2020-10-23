package moe.tristan.kmdah.common.spring.logging;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LoggingRestTemplateCustomizer implements RestTemplateCustomizer {

    private final OutgoingRequestsMetricsClientInterceptor outgoingRequestsMetricsClientInterceptor;

    public LoggingRestTemplateCustomizer(OutgoingRequestsMetricsClientInterceptor outgoingRequestsMetricsClientInterceptor) {
        this.outgoingRequestsMetricsClientInterceptor = outgoingRequestsMetricsClientInterceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(outgoingRequestsMetricsClientInterceptor);
    }

}
