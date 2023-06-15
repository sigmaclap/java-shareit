package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
