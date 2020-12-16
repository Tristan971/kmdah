package moe.tristan.kmdah.api;

import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.service.metrics.geoip.GeoIpService;

@RestController
@RequestMapping("/request-info")
public class RequestInfoController {

    private final GeoIpService geoIpService;

    public RequestInfoController(GeoIpService geoIpService) {
        this.geoIpService = geoIpService;
    }

    @GetMapping("/headers")
    public Map<String, List<String>> getRequestInfo(ServerHttpRequest request) {
        return request.getHeaders();
    }

    @GetMapping("/geoip/country/{header}")
    public Optional<String> getRequestCountry(@PathVariable String header, ServerHttpRequest request) throws UnknownHostException {
        List<String> headers = request.getHeaders().get(header);
        String firstOfHeaders = requireNonNull(headers, "Header is empty!").get(0);
        InetAddress address = InetAddress.getByAddress(firstOfHeaders.getBytes(StandardCharsets.UTF_8));
        return geoIpService.resolveCountryCode(address);
    }

}
