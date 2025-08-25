package dev.pollywag.nimbusdrop.exception;

public class ExceededQuotaException extends RuntimeException {
    public ExceededQuotaException(String message) {
        super(message);
    }
}
