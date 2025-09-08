package ru.practicum.shareit.exception;

public class UserEmailConflictException extends RuntimeException {
    public UserEmailConflictException(String message) {
        super(message);
    }
}
