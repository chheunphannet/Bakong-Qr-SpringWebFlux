package com.bakong.chongdia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TotalAmountResponse {
	private Double rate;
	private Double totalInKHR;
	private Double totalInUSD;
}
