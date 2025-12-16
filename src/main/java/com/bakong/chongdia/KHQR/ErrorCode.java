package com.bakong.chongdia.KHQR;

import lombok.Getter;

@Getter
public enum ErrorCode {
    TRANSACTION_NOT_FOUND(1, "Transaction could not be found. Please try again."),
    STATIC_QR_NOT_SUPPORTED(2, "Sorry, the system does not support static QR code."),
    TRANSACTION_FAILED(3, "Transaction failed."),
    DEEPLINK_ERROR(4, "Error occurred on requesting deeplink from provider."),
    MISSING_REQUIRED_FIELDS(5, "Missing required fields."),
    UNAUTHORIZED(6, "Unauthorized."),
    EMAIL_SERVER_DOWN(7, "Email server has been down."),
    EMAIL_ALREADY_REGISTERED(8, "Email has been registered already."),
    CANNOT_CONNECT_SERVER(9, "Cannot connect to server. Please try again later."),
    NOT_REGISTERED(10, "Not registered yet."),
    ACCOUNT_ID_NOT_FOUND(11, "Account ID not found."),
    MD5_NOT_FOUND(12, "MD5 is not exist."),
    FAIL_TO_GET_TOTAL_AMOUNT(13, "somthing went wrong or fail when get total amount."),
	NOT_FOUND_PAID_USERS(14, "Paid user not found.");
    private final int code;
    private final String description;

    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }
}
