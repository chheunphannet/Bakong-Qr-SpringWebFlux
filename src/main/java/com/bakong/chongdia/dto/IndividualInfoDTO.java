package com.bakong.chongdia.dto;

import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import lombok.Data;

@Data
public class IndividualInfoDTO {
	
	private String name;
	private Gender gender;
	private KHQRCurrency currency;
	private Double amount;
	private String note;
    
}
//input (name,gender,amount,currency)