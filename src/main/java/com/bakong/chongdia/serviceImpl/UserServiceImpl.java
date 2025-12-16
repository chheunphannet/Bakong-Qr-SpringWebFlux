package com.bakong.chongdia.serviceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bakong.chongdia.KHQR.BakongApiException;
import com.bakong.chongdia.KHQR.ErrorCode;
import com.bakong.chongdia.dto.IndividualInfoDTO;
import com.bakong.chongdia.dto.PaidUserResponse;
import com.bakong.chongdia.dto.PaymentStatus;
import com.bakong.chongdia.enity.Payments;
import com.bakong.chongdia.enity.User;
import com.bakong.chongdia.repository.PaymentsRepository;
import com.bakong.chongdia.repository.UserRepository;
import com.bakong.chongdia.service.UserService;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PaymentsRepository paymentsRepository;
	
	@Value("${bakong.expirationTime}")
	private long expirationTime;
	
	@Override
	public void createUser(IndividualInfoDTO dto, String md5) {
		User user = new User();
			user.setName(dto.getName());
			user.setGender(dto.getGender());
			user.setNote(dto.getNote());
		userRepository.save(user);
		
		Payments payment = new Payments();
		Instant now = Instant.now();
			payment.setCurrency(dto.getCurrency().name());
			payment.setExpiresAt(now.plus(expirationTime, ChronoUnit.MINUTES));
			payment.setStatus(PaymentStatus.PENDING);
			payment.setCreatedAt(now);
			payment.setUser(user);
			payment.setMd5(md5);
			payment.setAmount(dto.getAmount());
		paymentsRepository.save(payment);
	}

	@Override
	public List<PaidUserResponse> getAllPaidUers() {
		return userRepository.getAllPaidUsers().orElseThrow(() -> 
			new BakongApiException(
					HttpStatus.NOT_FOUND.value(),
					ErrorCode.NOT_FOUND_PAID_USERS.getCode(),
					ErrorCode.NOT_FOUND_PAID_USERS.getDescription())).stream()
		 	.map(response -> {
		 		if (response.getPaidAt() == null) {
		 	        return response;
		 	    }
		 		
		 		LocalDateTime localTime = toLocalTime(response.getPaidAt());
		 		response.setLocalPaidAt(localTime);
		 		
		 		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
		 		response.setFormattedPaidAt(localTime.format(formatter));
		 		
		 		return response;
		 	}).toList();
	}
	
	private LocalDateTime toLocalTime(Instant paidAt) {
		ZoneId cambodia = ZoneId.of("Asia/Phnom_Penh");
		return paidAt.atZone(ZoneId.of("UTC"))
	 		       .withZoneSameInstant(cambodia)
	 		       .toLocalDateTime();
	}

	@Override
	public PaidUserResponse getLastPaidUserByMd5(String md5) {
		PaidUserResponse lastPaidUser = userRepository.getLastPaidUserByMd5(md5).orElseThrow(() -> 
		new BakongApiException(
				HttpStatus.NOT_FOUND.value(),
				ErrorCode.NOT_FOUND_PAID_USERS.getCode(),
				ErrorCode.NOT_FOUND_PAID_USERS.getDescription()));
		
		LocalDateTime localTime = toLocalTime(lastPaidUser.getPaidAt());
		lastPaidUser.setLocalPaidAt(localTime);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
		lastPaidUser.setFormattedPaidAt(localTime.format(formatter));
		
		return lastPaidUser;
	}
	
}
