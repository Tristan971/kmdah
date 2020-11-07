package moe.tristan.kmdah.operator.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.model.configuration.LoadBalancerSettings;

@RestController
public class ForwardingController {

    private final LoadBalancerSettings loadBalancerSettings;

    public ForwardingController(LoadBalancerSettings loadBalancerSettings) {
        this.loadBalancerSettings = loadBalancerSettings;
    }

    @GetMapping("**")
    public void handleImageRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirection = UriComponentsBuilder
            .fromUri(loadBalancerSettings.getUri())
            .path(request.getServletPath())
            .build()
            .toUriString();

        response.sendRedirect(redirection);
    }

}
