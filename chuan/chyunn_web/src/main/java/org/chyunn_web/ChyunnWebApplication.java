package org.chyunn_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChyunnWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChyunnWebApplication.class, args);
	}

}
