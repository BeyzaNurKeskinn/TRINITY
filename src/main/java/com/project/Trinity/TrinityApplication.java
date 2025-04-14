package com.project.Trinity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrinityApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrinityApplication.class, args);
		System.out.println("TrinityApplication started successfully!");
	}
//Bu, Spring’in “her şeyi hallet” dediğimiz yerdir.
}
