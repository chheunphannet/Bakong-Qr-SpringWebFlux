package com.bakong.chongdia.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bakong.chongdia.dto.IndividualInfoDTO;
import com.bakong.chongdia.dto.PaidUserResponse;
import com.bakong.chongdia.dto.TotalAmountResponse;
import com.bakong.chongdia.service.PaymentService;
import com.bakong.chongdia.service.QrGeneratedService;
import com.bakong.chongdia.service.UserService;
import com.bakong.chongdia.serviceImpl.FluxSinkServiceImpl;

import jakarta.validation.Valid;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChongdiaController {

	private final QrGeneratedService generatedService;
	private final UserService userSerive;
	private final PaymentService paymentService;
	private final FluxSinkServiceImpl fluxSinkService;
	
	@PostMapping("/qr")
	public ResponseEntity<KHQRResponse<KHQRData>> createQr(@Valid @RequestBody IndividualInfoDTO dto) {
		KHQRResponse<KHQRData> qrData = generatedService.createQr(dto);
		if (qrData.getKHQRStatus().getCode() == 1)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(qrData);
		userSerive.createUser(dto, qrData.getData().getMd5());
		generatedService.checkPaymentStatusAsync(qrData.getData().getMd5());
		return ResponseEntity.status(HttpStatus.OK).body(qrData);
	}
	
	@GetMapping("/total")
	public Mono<TotalAmountResponse> getTotalAmountReactive(){
		return paymentService.getTotalAmountInCurrencyAsync();
    }
	
	@GetMapping("/all/paid")
	public ResponseEntity<List<PaidUserResponse>> getAllPaidUser(){
		return ResponseEntity.ok(userSerive.getAllPaidUers());
	}
	
	@GetMapping(value = "/new-paid-users", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<PaidUserResponse>> streamPaidUsers(
            @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
        
        if (clientId == null || clientId.isBlank()) {
            clientId = UUID.randomUUID().toString();
        }
        
        final String finalClientId = clientId;
        log.info("New SSE connection from client: {}", finalClientId);
        
        return fluxSinkService.createFlux(finalClientId)
                .map(response -> ServerSentEvent.<PaidUserResponse>builder()
                        .id(UUID.randomUUID().toString())
                        .event("paid-user")
                        .data(response)
                        .build())
                .doOnSubscribe(sub -> log.info("Client {} subscribed", finalClientId))
                .doOnComplete(() -> log.info("Stream completed for client {}", finalClientId))
                .doOnCancel(() -> log.info("Client {} cancelled", finalClientId));
    }
	
	
}
