package com.bakong.chongdia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChongdiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChongdiaApplication.class, args);
	}
	
}
