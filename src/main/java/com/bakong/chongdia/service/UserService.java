package com.bakong.chongdia.service;

import java.util.List;

import com.bakong.chongdia.dto.IndividualInfoDTO;
import com.bakong.chongdia.dto.PaidUserResponse;

public interface UserService {
	void createUser(IndividualInfoDTO dto, String md5);
	List<PaidUserResponse> getAllPaidUers();
	PaidUserResponse getLastPaidUserByMd5(String md5);
}
