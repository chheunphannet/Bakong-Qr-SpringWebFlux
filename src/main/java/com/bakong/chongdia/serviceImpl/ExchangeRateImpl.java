package com.bakong.chongdia.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bakong.chongdia.KHQR.BakongApiException;
import com.bakong.chongdia.KHQR.ResponseCode;
import com.bakong.chongdia.service.ExchangeRateService;
import com.bakong.chongdia.service.ExchangeResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
@Service
@RequiredArgsConstructor
public class ExchangeRateImpl implements ExchangeRateService{
	
	private final WebClient webClient;
	
	@Value("${spring.exchangeRate}")
	private String ExchangeUrl;

	@Override
	public Mono<ExchangeResponse> getExchangeRateInKhr() {
		return webClient.get()
				.uri(ExchangeUrl)
				.retrieve()
				.bodyToMono(ExchangeResponse.class)
				.map(response ->{
					Double khr = response.getConversion_rates().get("KHR");
					
					Map<String, Double> cr = new HashMap<>();
					cr.put("KHR", khr);
					
					ExchangeResponse small = new ExchangeResponse();
	                small.setResult(response.getResult());
	                small.setConversion_rates(cr);
	                
	                return small;
				}).doOnError(t -> new BakongApiException( 
						HttpStatus.BAD_REQUEST.value(),
						ResponseCode.FAIL.getCode(),
						"Fail to call exchangerate-api."));
	}

}
