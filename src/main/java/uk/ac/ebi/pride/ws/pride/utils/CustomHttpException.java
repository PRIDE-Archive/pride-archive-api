package uk.ac.ebi.pride.ws.pride.utils;

import org.springframework.http.HttpStatus;

public class CustomHttpException extends RuntimeException {
    private final HttpStatus httpStatus;

    public CustomHttpException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
