package com.bakong.chongdia.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bakong.chongdia.KHQR.CheckTransactionByMd5Request;
import com.bakong.chongdia.KHQR.CheckTransactionResponse;
import com.bakong.chongdia.KHQR.TransactionService;
import com.bakong.chongdia.dto.IndividualInfoDTO;
import com.bakong.chongdia.dto.PaidUserResponse;
import com.bakong.chongdia.dto.TotalAmountResponse;
import com.bakong.chongdia.repository.UserRepository;
import com.bakong.chongdia.service.ExchangeRateService;
import com.bakong.chongdia.service.ExchangeResponse;
import com.bakong.chongdia.service.PaymentService;
import com.bakong.chongdia.service.QrGeneratedService;
import com.bakong.chongdia.service.UserService;
import com.bakong.chongdia.serviceImpl.FluxSinkService;
import com.bakong.chongdia.serviceImpl.FluxSinkServiceImpl;

import jakarta.validation.Valid;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class KHQRController {
	
	private final QrGeneratedService generatedService;
	private final TransactionService transactionService;
	private final ExchangeRateService exchangeRateService;
	private final PaymentService paymentService;
	private final UserRepository userRepository;
	private final FluxSinkService fluxSinkService;
	private final UserService userService;
	private final FluxSinkServiceImpl fluxSinkServiceImpl;
	@PostMapping("/test/qr")
	public ResponseEntity<KHQRResponse<KHQRData>> getQrData(@RequestBody IndividualInfoDTO dto){
		KHQRResponse<KHQRData> qrData = generatedService.createQr(dto);
		if(qrData.getKHQRStatus().getCode() == 1) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(qrData);
		}
		return ResponseEntity.status(HttpStatus.OK).body(qrData);
	}
	
    @PostMapping("/check")
    public ResponseEntity<CheckTransactionResponse> checkTransactionByMd5(
            @Valid @RequestBody CheckTransactionByMd5Request request) {	
        
        CheckTransactionResponse response = transactionService.checkTransactionByMd5(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/exchange")
    public ResponseEntity<ExchangeResponse> getExchange(){
    	ExchangeResponse exchangeResponse = exchangeRateService.getExchangeRateInKhr().block();
    	if(exchangeResponse.getResult().equals("success")) {
    		return ResponseEntity.ok(exchangeResponse);
    	}
    	return ResponseEntity.ok(null);
    }
    
    @GetMapping("/paid")
    public List<PaidUserResponse> getAllPaidUser(){
    	return userRepository.getAllPaidUsers().orElse(null);
    }
    
    @GetMapping("/totalReactive/{currency}")
    public Mono<TotalAmountResponse> getTotalAmountReactive(@PathVariable KHQRCurrency currency){
		return paymentService.getTotalAmountInCurrencyAsync();
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PaidUserResponse> streamPayments() {
    	List<PaidUserResponse> users = userService.getAllPaidUers();
    	
    	for(PaidUserResponse user : users) {
    		fluxSinkService.publish(user);
    	}
    	
        return fluxSinkService.getFlux();
    }
    
    
    @GetMapping(value = "/new-paid-users", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<PaidUserResponse>> streamPaidUsers(
            @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
        
        // Generate client ID if not provided
        if (clientId == null || clientId.isBlank()) {
            clientId = UUID.randomUUID().toString();
        }
        
        final String finalClientId = clientId;
        log.info("New SSE connection from client: {}", finalClientId);
        
        return fluxSinkServiceImpl.createFlux(finalClientId)
                .map(response -> ServerSentEvent.<PaidUserResponse>builder()
                        .id(UUID.randomUUID().toString())
                        .event("paid-user")
                        .data(response)
                        .build())
                .doOnSubscribe(sub -> log.info("Client {} subscribed", finalClientId))
                .doOnComplete(() -> log.info("Stream completed for client {}", finalClientId))
                .doOnCancel(() -> log.info("Client {} cancelled", finalClientId));
    }
    
    @GetMapping("/connection-count")
    public int getConnectionCount() {
        return fluxSinkServiceImpl.getActiveConnectionCount();
    }
	
}
