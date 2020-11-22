package moe.tristan.kmdah.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Invalid request token!")
final class InvalidImageRequestTokenException extends RuntimeException {

    public InvalidImageRequestTokenException(String message) {
        super(message);
    }

    public InvalidImageRequestTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
