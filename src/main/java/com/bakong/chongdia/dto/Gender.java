package com.bakong.chongdia.dto;

import lombok.Getter;

@Getter
public enum Gender {
	MALE("MALE"),
	FEMALE("FEMALE");
	
	private String value;

	private Gender(String value) {
		this.value = value;
	}
	
	
}
