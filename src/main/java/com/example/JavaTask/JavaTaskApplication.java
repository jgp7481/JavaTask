package com.example.JavaTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.JavaTask")
public class JavaTaskApplication {

	private static Logger logger = LoggerFactory.getLogger(JavaTaskApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(JavaTaskApplication.class, args);
		logger.info("Spring application started successfully...!!\n");
	}

}
