package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class ItemAlreadyExistException extends RuntimeException {
    public ItemAlreadyExistException(String message) {
        super(message);
    }
}
