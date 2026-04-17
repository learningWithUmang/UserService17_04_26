package dev.umang.userauthservice_17_04_2026.exceptions;

public class UserNotExistException extends RuntimeException{
    public UserNotExistException(String message) {
        super(message);
    }
}
