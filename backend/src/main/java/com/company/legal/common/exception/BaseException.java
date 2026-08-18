package com.company.legal.common.exception;

/**
 * Base exception for all application exceptions.
 */
public class BaseException extends RuntimeException {

    private final String errorCode;
    private final String[] args;

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = new String[0];
    }

    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new String[0];
    }

    public BaseException(String errorCode, String message, String... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String[] getArgs() {
        return args;
    }
}