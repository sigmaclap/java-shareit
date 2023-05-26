package ru.practicum.shareit.exceptions;

import org.springframework.http.HttpStatus;

public class ItemAlreadyExistException extends RuntimeException {
    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public ItemAlreadyExistException() {
    }

    public ItemAlreadyExistException(String message) {
        super(message);
    }

    public ItemAlreadyExistException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
