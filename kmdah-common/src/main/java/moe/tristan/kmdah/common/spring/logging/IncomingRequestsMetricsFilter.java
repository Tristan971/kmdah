package moe.tristan.kmdah.common.spring.logging;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class IncomingRequestsMetricsFilter extends OncePerRequestFilter {

    private final RequestsLogger requestsLogger;

    public IncomingRequestsMetricsFilter() {
        requestsLogger = new RequestsLogger(getClass());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = requestsLogger.logStarted(request);

        filterChain.doFilter(request, response);

        requestsLogger.logFinished(request, response, start);
    }

}
