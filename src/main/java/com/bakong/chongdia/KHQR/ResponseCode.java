package com.bakong.chongdia.KHQR;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(0, "Success"),
    FAIL(1, "Fail");

    private final int code;
    private final String description;

    ResponseCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
