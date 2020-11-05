package moe.tristan.kmdah.worker.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Invalid referrer header.")
final class InvalidReferrerException extends IllegalArgumentException {

    public InvalidReferrerException(String s) {
        super(s);
    }

    public InvalidReferrerException(String message, Throwable cause) {
        super(message, cause);
    }

}
