package moe.tristan.kmdah.common.model.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.util.TimingInfo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Component
public class S3RequestMetricsCollector extends RequestMetricCollector implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3RequestMetricsCollector.class);

    private final Map<HttpMethodName, Counter> timeSums = new ConcurrentHashMap<>(HttpMethodName.values().length);
    private final Map<HttpMethodName, Counter> timeCounters = new ConcurrentHashMap<>(HttpMethodName.values().length);

    @Override
    public void collectMetrics(Request<?> request, Response<?> response) {
        HttpMethodName method = request.getHttpMethod();
        TimingInfo timingInfo = request.getAWSRequestMetrics().getTimingInfo();

        LOGGER.info("{} {} ({}ms)", method, request.getResourcePath(), timingInfo.getTimeTakenMillisIfKnown());
        if (timingInfo.getTimeTakenMillisIfKnown() != null) {
            timeSums.get(method).increment(timingInfo.getTimeTakenMillisIfKnown());
            timeCounters.get(method).increment();
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (HttpMethodName method : HttpMethodName.values()) {
            timeCounters.put(method, registry.counter(
                "kmdah_s3_metrics_time_count",
                "method", method.name()
            ));

            timeSums.put(method, registry.counter(
                "kmdah_s3_metics_time_sum",
                "method", method.name()
            ));
        }
    }

}
