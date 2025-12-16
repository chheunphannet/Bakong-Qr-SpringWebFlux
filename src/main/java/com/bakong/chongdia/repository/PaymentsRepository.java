package com.bakong.chongdia.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bakong.chongdia.enity.Payments;

import jakarta.transaction.Transactional;

public interface PaymentsRepository extends JpaRepository<Payments, String>{
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE payments SET status = :status WHERE md5 = :md5", nativeQuery = true)
	int updateStatus(@Param("md5") String md5, @Param("status") String status);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE payments SET paid_at = :paidAt WHERE md5 = :md5", nativeQuery = true)
	int updatePaidDate(@Param("md5") String md5, @Param("paidAt") Instant paidAt);
	
	@Query("SELECT p.expiresAt FROM Payments p WHERE p.md5 = :md5")
	Optional<Instant> findExpiresAtByMd5(@Param("md5") String md5);

	
	@Query(value = """
			SELECT 
				SUM(
					CASE
					WHEN p.status = 'PAID' THEN
						CASE
							WHEN p.currency = 'KHR'
								THEN p.amount / :exchangeRate
							ELSE p.amount
						END
					ELSE 0
					END
				) AS total_convert_amount
			FROM payments AS p
			"""
	,nativeQuery = true)
	Optional<Double> totalAmountInCurrency(Double exchangeRate);
}
