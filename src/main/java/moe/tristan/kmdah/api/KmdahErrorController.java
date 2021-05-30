package moe.tristan.kmdah.api;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import moe.tristan.kmdah.service.gossip.InstanceId;

@Controller
public class KmdahErrorController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KmdahErrorController.class);
    public static final String ERROR_TEMPLATE = """
        <html lang="en">
        <head>
        <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
            <meta http-equiv="X-UA-Compatible" content="ie=edge">
            <title>Uh oh</title>
        </head>
        <body>
            Hi, something went wrong. Apologies.<br />

            If it persists, ask on discord with the following information:<br />
            <ul>
                <li>uri: %s</li>
                <li>instance: %s</li>
            </ul>
        </body>
        </html>
        """;

    private final InstanceId instanceId;

    public KmdahErrorController(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object failureUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        LOGGER.error("IP {} encountered error {} for uri {}", request.getRemoteAddr(), statusCode, failureUri, exception);

        String message = ERROR_TEMPLATE.formatted(valueOf(failureUri), instanceId.id());

        return ResponseEntity
            .status(parseInt(valueOf(statusCode)))
            .body(message);
    }

}
