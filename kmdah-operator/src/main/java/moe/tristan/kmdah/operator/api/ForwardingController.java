package moe.tristan.kmdah.operator.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class ForwardingController {

    @GetMapping
    public void handleImageRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirection = UriComponentsBuilder
            .fromHttpUrl(request.getRequestURI())
            .host("mdah.tristan.moe")
            .build()
            .toUriString();
        response.sendRedirect(redirection);
    }

}
