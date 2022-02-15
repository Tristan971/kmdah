package moe.tristan.kmdah.webmvc;

import java.io.IOException;
import java.time.Clock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestsLogger extends OncePerRequestFilter implements ClientHttpRequestInterceptor, RestTemplateCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestsLogger.class);
    private static final Clock CLOCK = Clock.systemUTC();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = CLOCK.instant().toEpochMilli();

        String requestMethodAndPath = request.getMethod() + " " + request.getRequestURL();

        try {
            if (LOGGER.isDebugEnabled()) {
                logStart(requestMethodAndPath);
            }
            filterChain.doFilter(request, response);
        } finally {
            logEnd(requestMethodAndPath, response.getStatus(), startTime);
        }
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = CLOCK.instant().toEpochMilli();

        String requestMethodAndPath = "%s %s".formatted(request.getMethod(), request.getURI());

        ClientHttpResponse response = execution.execute(request, body);
        logEnd(requestMethodAndPath, response.getRawStatusCode(), startTime);

        return response;
    }

    private void logStart(String requestMethodAndPath) {
        LOGGER.debug("{} begin", requestMethodAndPath);
    }

    private void logEnd(String requestMethodAndPath, int statusCode, long startTimeMillis) {
        long endTime = CLOCK.instant().toEpochMilli();
        long durationMillis = endTime - startTimeMillis;

        int statusRange = statusCode / 100;

        if (statusRange > 3) {
            LOGGER.error("{} {} ({}ms)", requestMethodAndPath, statusCode, durationMillis);
        } else {
            LOGGER.info("{} {} ({}ms)", requestMethodAndPath, statusCode, durationMillis);
        }
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(this);
    }

}
