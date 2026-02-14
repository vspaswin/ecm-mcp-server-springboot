package com.jpmc.ecm.exception;

import lombok.Getter;

/**
 * Exception thrown when ECM API operations fail.
 */
@Getter
public class EcmApiException extends RuntimeException {

    private final int statusCode;

    public EcmApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public EcmApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
