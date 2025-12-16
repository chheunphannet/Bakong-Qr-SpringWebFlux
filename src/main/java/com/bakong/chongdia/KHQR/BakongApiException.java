package com.bakong.chongdia.KHQR;

import lombok.Getter;

@Getter
public class BakongApiException extends RuntimeException {
    private final int httpStatusCode;
    private final Integer errorCode;
    private final String errorMessage;
    
    public BakongApiException(int httpStatusCode, Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
