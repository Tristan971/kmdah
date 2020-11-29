package moe.tristan.kmdah.service.images.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Invalid referrer header.")
public final class InvalidImageRequestReferrerException extends IllegalArgumentException {

    public InvalidImageRequestReferrerException(String s) {
        super(s);
    }

    public InvalidImageRequestReferrerException(String message, Throwable cause) {
        super(message, cause);
    }

}
