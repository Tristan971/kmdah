package moe.tristan.kmdah.service.images.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Invalid request token!")
public final class InvalidImageRequestTokenException extends RuntimeException {

    public InvalidImageRequestTokenException(String message) {
        super(message);
    }

    public InvalidImageRequestTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
