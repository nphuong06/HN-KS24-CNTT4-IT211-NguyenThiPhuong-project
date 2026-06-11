package org.example.project.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateException extends ApiException {

    public InvalidStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
