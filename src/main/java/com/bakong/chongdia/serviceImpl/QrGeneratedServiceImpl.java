package com.bakong.chongdia.serviceImpl;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bakong.chongdia.KHQR.BakongApiException;
import com.bakong.chongdia.KHQR.ErrorCode;
import com.bakong.chongdia.KHQR.TransactionService;
import com.bakong.chongdia.dto.IndividualInfoDTO;
import com.bakong.chongdia.dto.PaidUserResponse;
import com.bakong.chongdia.dto.PaymentStatus;
import com.bakong.chongdia.repository.PaymentsRepository;
import com.bakong.chongdia.service.QrGeneratedService;
import com.bakong.chongdia.service.UserService;
import com.bakong.chongdia.staticValues.StaticValues;

import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;
@Service
@Slf4j
public class QrGeneratedServiceImpl implements QrGeneratedService{
	
	@Value("${spring.bakongAccountId}")
	private String bakongAccountId;
	
	@Value("${bakong.expirationTime}")
	private Long expirationTime;
	
	@Value("${bakong.api.token}")
    private String authorization;
	
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private PaymentsRepository paymentRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private FluxSinkServiceImpl fluxSinkServiceImpl;
	@Override
	public KHQRResponse<KHQRData> createQr(IndividualInfoDTO dto) {
		return BakongKHQR.generateIndividual(toIndividualInfo(dto));
	}
	
	private IndividualInfo toIndividualInfo(IndividualInfoDTO dto) {
		if(dto == null) return null;
		IndividualInfo qr = new IndividualInfo();
		
		qr.setBakongAccountId(bakongAccountId);
		if(dto.getCurrency() != null) qr.setCurrency(dto.getCurrency());
		qr.setAmount(dto.getAmount());
		qr.setMerchantName(StaticValues.merchantName);
		qr.setMerchantCity(StaticValues.merchantCity);
		qr.setAcquiringBank(StaticValues.acquiringBank);
		qr.setAccountInformation(StaticValues.mobileNumber);
		qr.setBillNumber(StaticValues.billNumber);
		qr.setStoreLabel(StaticValues.purposeOfTransaction);
		qr.setTerminalLabel(StaticValues.TerminalLabel);
		qr.setMobileNumber(StaticValues.mobileNumber);
		qr.setPurposeOfTransaction(StaticValues.purposeOfTransaction);
		qr.setMerchantNameAlternateLanguage(StaticValues.merchantName);
		qr.setMerchantCityAlternateLanguage(StaticValues.merchantCity);
		qr.setExpirationTimestamp(StaticValues.expirationTimestamp(expirationTime)); // expires in 2min
		qr.setMerchantAlternateLanguagePreference("km");
		return qr;
	}
	
//	private KHQRCurrency toKHQRCurrency(String value) {
//		value.trim();
//		KHQRCurrency currency = KHQRCurrency.KHR;
//		
//		if(value.equals("USD")) {
//			currency = KHQRCurrency.USD;
//		}
//		return currency;
//	}

	@Override
	public void checkPaymentStatusAsync(String md5) {
		Instant expiresAt = getExpiresAtByMd5(md5);
		
		Instant now = Instant.now();
        if (expiresAt.isBefore(now)) {
            log.info("Transaction {} already expired.", md5);
            paymentRepository.updateStatus(md5, PaymentStatus.EXPIRED.getValue());
            return;
        }
        Duration timeout = Duration.between(now, expiresAt);
        
        transactionService.checkIfPaid(md5, authorization, timeout)
        .publishOn(Schedulers.boundedElastic()) // ប្តូរទៅ Thread សម្រាប់ Database operations
        .subscribe(isPaid -> {
            if (isPaid) {
                int updateStatus = paymentRepository.updateStatus(md5, PaymentStatus.PAID.getValue());
                int setPaidAt = paymentRepository.updatePaidDate(md5, Instant.now());
                
                PaidUserResponse lastPaidUser = userService.getLastPaidUserByMd5(md5);
                
                fluxSinkServiceImpl.publishToAll(lastPaidUser);
                
                log.info("Transaction PAID: {} updated.", updateStatus);
                log.info("Paid date: {} updated.", setPaidAt);
            } else {
                log.info("Transaction EXPIRED or Failed for MD5: {}", md5);
                paymentRepository.updateStatus(md5, PaymentStatus.EXPIRED.getValue());
            }
        }, error -> {
            log.error("Unexpected error for MD5: {}", md5, error);
        });
		    
	}

	@Override
	public Instant getExpiresAtByMd5(String md5) {
		return paymentRepository.findExpiresAtByMd5(md5).orElseThrow(
				() -> new BakongApiException(
						HttpStatus.NOT_FOUND.value(), 
						ErrorCode.MD5_NOT_FOUND.getCode(),
						ErrorCode.MD5_NOT_FOUND.getDescription()));
	}

}
