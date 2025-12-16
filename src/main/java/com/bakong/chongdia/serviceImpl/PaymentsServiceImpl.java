package com.bakong.chongdia.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bakong.chongdia.KHQR.BakongApiException;
import com.bakong.chongdia.KHQR.ErrorCode;
import com.bakong.chongdia.dto.TotalAmountResponse;
import com.bakong.chongdia.repository.PaymentsRepository;
import com.bakong.chongdia.service.ExchangeRateService;
import com.bakong.chongdia.service.PaymentService;

import reactor.core.publisher.Mono;

@Service
public class PaymentsServiceImpl implements PaymentService{

	@Autowired
	private ExchangeRateService exchangeRateService;
	
	@Autowired
	private PaymentsRepository paymentRepository;


	public Mono<TotalAmountResponse> getTotalAmountInCurrencyAsync() {
		return exchangeRateService.getExchangeRateInKhr()
					.map(r -> {
						Double rate = 4000.00;
						
						if(r.getResult().equals("success")) {
							rate = (double) Math.round(r.getConversion_rates().get("KHR"));
						}
						
						Double totalInUSD = paymentRepository.totalAmountInCurrency(rate)
								.orElseThrow(() -> new BakongApiException(
										HttpStatus.BAD_REQUEST.value(),
										ErrorCode.FAIL_TO_GET_TOTAL_AMOUNT.getCode(),
										ErrorCode.FAIL_TO_GET_TOTAL_AMOUNT.getDescription()));
						
						Double totalInKHR = rate * totalInUSD;
						return new TotalAmountResponse(
								rate,
								totalInKHR,
								totalInUSD
								);
					});
	}
	
}
