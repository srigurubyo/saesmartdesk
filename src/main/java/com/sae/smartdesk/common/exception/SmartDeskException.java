package com.sae.smartdesk.common.exception;

public class SmartDeskException extends RuntimeException {

    public SmartDeskException(String message) {
        super(message);
    }

    public SmartDeskException(String message, Throwable cause) {
        super(message, cause);
    }
}
