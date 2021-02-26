package moe.tristan.kmdah.api;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(value = "/", produces = MediaType.IMAGE_GIF_VALUE)
    public Resource hello() {
        return new ClassPathResource("hello.gif");
    }

    @GetMapping(value = "/favicon.ico", produces = "image/x-icon")
    public Resource favicon() {
        return new ClassPathResource("favicon.ico");
    }

}
