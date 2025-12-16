package com.bakong.chongdia.service;

import reactor.core.publisher.Mono;

public interface ExchangeRateService {
	Mono<ExchangeResponse> getExchangeRateInKhr();
}
