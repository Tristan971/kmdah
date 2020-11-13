package moe.tristan.kmdah.operator.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.operator.model.OperatorSettings;

@RestController
public class ForwardingController {

    private final OperatorSettings operatorSettings;

    public ForwardingController(OperatorSettings operatorSettings) {
        this.operatorSettings = operatorSettings;
    }

    @GetMapping("/favicon.ico")
    public void handleFavicon(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://mangadex.org/favicon.ico");
    }

    @GetMapping("**")
    public void handleImageRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirection = UriComponentsBuilder
            .fromUri(operatorSettings.getRedirectUri())
            .path(request.getServletPath())
            .build()
            .toUriString();

        response.sendRedirect(redirection);
    }

}
