package moe.tristan.kmdah.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/request-info")
public class RequestInfoController {

    @GetMapping("/headers")
    public Map<String, List<String>> getRequestInfo(ServerHttpRequest request) {
        return request.getHeaders();
    }

}
