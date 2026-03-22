package com.lucas.lexcontrol.common;

public class ApiException extends RuntimeException {

    private final int status;
    private final ApiErrorCode errorCode;

    public ApiException(ApiErrorCode errorCode, String message) {
        super(message);
        this.status = errorCode.getStatusCode();
        this.errorCode = errorCode;
    }

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
        this.errorCode = ApiErrorCode.UNKNOWN_ERROR;
    }

    public int getStatus() {
        return status;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}
