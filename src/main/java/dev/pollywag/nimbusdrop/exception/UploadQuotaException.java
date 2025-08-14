package dev.pollywag.nimbusdrop.exception;

public class UploadQuotaException extends RuntimeException {
    public UploadQuotaException(String message) {
        super(message);
    }
}
