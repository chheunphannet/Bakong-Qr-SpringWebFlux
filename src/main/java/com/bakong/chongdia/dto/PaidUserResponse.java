package com.bakong.chongdia.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import com.bakong.chongdia.enity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
@Getter
public class PaidUserResponse {
	
	private final User user;
	private final Double amount;
	private final String currency;
	@JsonIgnore
	private final Instant paidAt;
	@JsonIgnore
	private LocalDateTime localPaidAt;
	private String formattedPaidAt;
	
	public PaidUserResponse(User user, Double amount, String currency, Instant paidAt) {
        this.user = user;
        this.amount = amount;
        this.currency = currency;
        this.paidAt = paidAt;
    }
	
	public void setLocalPaidAt(LocalDateTime localPaidAt) {
		this.localPaidAt = localPaidAt;
	}
	
	public void setFormattedPaidAt(String formattedPaidAt) {
	    this.formattedPaidAt = formattedPaidAt;
	}
}
