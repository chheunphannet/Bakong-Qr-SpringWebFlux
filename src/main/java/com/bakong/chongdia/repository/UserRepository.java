package com.bakong.chongdia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bakong.chongdia.dto.PaidUserResponse;
import com.bakong.chongdia.enity.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	
	@Query("""
	        SELECT p.user,p.amount,p.currency,p.paidAt FROM Payments p
	        WHERE p.status = 'PAID'
	        ORDER BY p.paidAt DESC
	        """)
	Optional<List<PaidUserResponse>> getAllPaidUsers();
	
	@Query("""
	        SELECT p.user,p.amount,p.currency,p.paidAt FROM Payments p
	        WHERE p.md5 = :md5 AND p.status = 'PAID'
	        """)
	Optional<PaidUserResponse> getLastPaidUserByMd5(String md5);
}
