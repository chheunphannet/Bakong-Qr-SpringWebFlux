package com.bakong.chongdia.KHQR;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckTransactionResponse {
    private Integer responseCode;
    private String responseMessage;
    private Integer errorCode;
    private TransactionData data;
    
    public CheckTransactionResponse(Integer responseCode) {
    	this.responseCode = responseCode;
    }
}
