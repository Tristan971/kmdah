package moe.tristan.kmdah.service.metrics.geoip;

import java.net.InetSocketAddress;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class GeoIpMetricsFilter implements WebFilter {

    private final GeoIpMetrics geoIpMetrics;

    public GeoIpMetricsFilter(GeoIpMetrics geoIpMetrics) {
        this.geoIpMetrics = geoIpMetrics;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().value().startsWith("/__")) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange).transformDeferred(
            call -> call.doOnSubscribe(__ -> Optional
                .ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getHostString)
                .ifPresent(geoIpMetrics::recordCountrySource))
        );
    }

}
