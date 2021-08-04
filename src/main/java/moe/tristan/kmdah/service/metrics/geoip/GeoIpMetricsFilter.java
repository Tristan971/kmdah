package moe.tristan.kmdah.service.metrics.geoip;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class GeoIpMetricsFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIpMetricsFilter.class);

    private final GeoIpMetrics geoIpMetrics;

    public GeoIpMetricsFilter(GeoIpMetrics geoIpMetrics) {
        LOGGER.info("GeoIP metrics enabled");
        this.geoIpMetrics = geoIpMetrics;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!request.getServletPath().startsWith("/__")) {
            try {
                String remoteAddr = request.getRemoteAddr();
                geoIpMetrics.recordCountrySource(remoteAddr);
            } catch (Exception e) {
                LOGGER.error("Cannot resolve country.", e);
            }
        }
        filterChain.doFilter(request, response);
    }

}
