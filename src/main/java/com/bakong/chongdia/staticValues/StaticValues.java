package com.bakong.chongdia.staticValues;

import java.util.UUID;

public class StaticValues {
	
	public static String billNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
	public static final String purposeOfTransaction = "ចងដៃ";
	public static final String mobileNumber = "85587248907";
	public static final String merchantName = "មង្គលការផានិតនិងស្រីម៉ុច";
	public static final String acquiringBank = "WING";
	public static final String merchantCity = "Phnom Penh";
	public static final String TerminalLabel = "ខាងប្រុស";
	
	public static Long expirationTimestamp(Long minutes) {
		return System.currentTimeMillis() + (minutes * 60 * 1000L);
	}

}
