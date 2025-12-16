package com.bakong.chongdia.service;

import com.bakong.chongdia.dto.TotalAmountResponse;

import reactor.core.publisher.Mono;

public interface PaymentService {
	Mono<TotalAmountResponse> getTotalAmountInCurrencyAsync();
}
