package com.bakong.chongdia.enity;

import java.time.Instant;

import com.bakong.chongdia.dto.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payments {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = 36)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(length = 3, nullable = false)
	private String currency;
	
	@Column(nullable = false)
	private String md5;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 7, nullable = false)
	private PaymentStatus status; 
	
	@Column(nullable = false)
	private Instant expiresAt;
	
	@Column(nullable = false)
	private Instant createdAt;
	
	@Column(nullable = false)
	private Double amount;
	
	@Column(nullable = true)
	private Instant paidAt;

}

//CREATE TABLE payments(
//		 id VARCHAR(32) PRIMARY KEY,
//		 user_id INT NOT NULL,
//		 amount FLOAT NOT NULL,
//		 currency VARCHAR(3),
//		 paid_date DATETIME,
//		 FOREIGN KEY (user_id) REFERENCES `user`(id)
//);