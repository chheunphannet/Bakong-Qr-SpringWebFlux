package com.bakong.chongdia.KHQR;

import java.time.Duration;

import reactor.core.publisher.Mono;

public interface TransactionService {
    CheckTransactionResponse checkTransactionByMd5(
    		CheckTransactionByMd5Request request
//    		,String authorization
    		);
    
    Mono<Boolean> checkIfPaid(String md5, String authorization, Duration timeOut);
}
