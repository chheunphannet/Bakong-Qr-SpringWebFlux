package com.bakong.chongdia.KHQR;

@lombok.Data
@lombok.Builder
public class ErrorResponse {
    private int status;
    private String message;
    private String error;
}
