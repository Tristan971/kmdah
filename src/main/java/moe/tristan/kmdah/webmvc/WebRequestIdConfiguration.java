package moe.tristan.kmdah.webmvc;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(HIGHEST_PRECEDENCE) // ensure it is at the top of the filter list
public class WebRequestIdConfiguration extends OncePerRequestFilter implements ClientHttpRequestInterceptor, RestTemplateCustomizer {

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            RequestContext.setId(request.getRemoteAddr());
            filterChain.doFilter(request, response);
        } finally {
            RequestContext.reset();
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        return execution.execute(request, body);
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        // should ideally be the topmost interceptor
        restTemplate.getInterceptors().add(0, this);
    }

}
