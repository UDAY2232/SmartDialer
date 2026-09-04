package com.smartdialer.dialer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartdialerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartdialerApplication.class, args);
	}

}
