package moe.tristan.kmdah.service.metrics.geoip;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GeoIpMetricsFilter extends OncePerRequestFilter {

    private final GeoIpMetrics geoIpMetrics;

    public GeoIpMetricsFilter(GeoIpMetrics geoIpMetrics) {
        this.geoIpMetrics = geoIpMetrics;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!request.getServletPath().startsWith("/__")) {
            String remoteAddr = request.getRemoteAddr();
            geoIpMetrics.recordCountrySource(remoteAddr);
        }
        filterChain.doFilter(request, response);
    }

}
