package moe.tristan.kmdah.service.logging;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class OutgoingRequestsMetricsClientInterceptor implements ClientHttpRequestInterceptor {

    private final RequestsLogger requestsLogger;

    public OutgoingRequestsMetricsClientInterceptor() {
        requestsLogger = new RequestsLogger(getClass());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long start = requestsLogger.logStarted(request);

        ClientHttpResponse response = execution.execute(request, body);

        requestsLogger.logFinished(request, response, start);

        return response;
    }

}
