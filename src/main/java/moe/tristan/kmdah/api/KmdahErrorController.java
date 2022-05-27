package moe.tristan.kmdah.api;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import moe.tristan.kmdah.service.images.validation.InvalidImageRequestReferrerException;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestTokenException;

@RestControllerAdvice
public class KmdahErrorController extends ResponseEntityExceptionHandler implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KmdahErrorController.class);

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object failureUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        LOGGER.error("Exception raised for {}", failureUri, exception);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidImageRequestReferrerException.class)
    public ResponseEntity<String> handleBadReferer(InvalidImageRequestReferrerException e, HttpServletRequest request) {
        LOGGER.info("Bad referer for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(InvalidImageRequestTokenException.class)
    public ResponseEntity<String> handleInvalidToken(InvalidImageRequestTokenException e, HttpServletRequest request) {
        LOGGER.warn("Bad token for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleViolation(ConstraintViolationException e, HttpServletRequest request) {
        LOGGER.warn("Constraint violation for {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception e, HttpServletRequest request) {
        LOGGER.error("Unexpected exception for {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
