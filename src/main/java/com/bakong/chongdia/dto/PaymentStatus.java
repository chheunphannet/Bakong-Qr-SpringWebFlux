package com.bakong.chongdia.dto;

import lombok.Getter;

@Getter
public enum PaymentStatus {
	PAID("PAID"),
	EXPIRED("EXPIRED"),
	PENDING("PENDING");
	
	private String value;

	private PaymentStatus(String value) {
		this.value = value;
	}
	
}
