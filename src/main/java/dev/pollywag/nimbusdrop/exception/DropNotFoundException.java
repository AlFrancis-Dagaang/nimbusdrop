package dev.pollywag.nimbusdrop.exception;

public class DropNotFoundException extends RuntimeException {
    public DropNotFoundException(String message) {
        super(message);
    }
}
