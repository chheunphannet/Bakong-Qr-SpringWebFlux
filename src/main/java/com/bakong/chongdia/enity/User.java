package com.bakong.chongdia.enity;

import com.bakong.chongdia.dto.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "users",
		indexes = {
					@Index(name = "idx_name", columnList = "name")
				}
		)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 100, nullable = false)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 6	, nullable = false)
	private Gender gender;
	
//	@Column(nullable = false)
//	private Double total;
//	
//	@Column(length = 3, nullable = false)
//	private String currency;
	
	@Column(length = 200, nullable = true)
	private String note;
}

//CREATE TABLE `user`(
//		 id INT PRIMARY KEY,
//		 `name` VARCHAR(100) NOT NULL,
//		 total FLOAT NOT NULL,
//		 currency VARCHAR(3) NOT NULL,
//		 note VARCHAR(200) NULL
//);