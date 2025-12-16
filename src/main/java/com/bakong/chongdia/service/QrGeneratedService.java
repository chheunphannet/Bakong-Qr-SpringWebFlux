package com.bakong.chongdia.service;

import java.time.Instant;

import com.bakong.chongdia.dto.IndividualInfoDTO;

import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;

public interface QrGeneratedService {
	KHQRResponse<KHQRData> createQr(IndividualInfoDTO dto);
	void checkPaymentStatusAsync(String md5);
//	boolean checkIfPaid(String md5);
	Instant getExpiresAtByMd5(String md5);
}
